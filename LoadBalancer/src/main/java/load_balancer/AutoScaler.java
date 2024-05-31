package load_balancer;

import deployment_manager.AwsEc2Manager;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.*;

public class AutoScaler {

    private static final int autoscale_interval = 4000;
    private static final int MEMORY_THRESHOLD = 100; // 80% memory usage
    private static final int MAX_MEMORY = 970 - MEMORY_THRESHOLD; // 1GB
    private static final int MAX_CPU = 75; // 80%
    private static final int MIN_CPU = 20; // 20%
    public static final List<Instance> terminating_instances = new ArrayList<>();
    public static final int MAX_INSTANCES = 5; // Maximum number of instances to deploy
    private static final int MIN_INSTANCES = 1; // Minimum number of instances to keep running

    public AutoScaler() {
        // run some logic in a new thread to scale the system
        new Thread(() -> {
            // System.out.println("AutoScaler started up");
            while (true) {
                try {
                    Thread.sleep(autoscale_interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // System.out.println("Autoscaler iteration");
                // check if terminating instances are done
                for (Instance instance : terminating_instances) {
                    // if the instance is done, remove it from the list
                    synchronized (LoadBalancer.instanceRequests) {
                        if (LoadBalancer.instanceRequests.get(instance.instanceId()).isEmpty()) {
                            LoadBalancer.instanceRequests.remove(instance.instanceId());
                            AwsEc2Manager.terminateInstance(instance.instanceId());
                        }
                    }
                }

                // all instances have been terminated, clear the list
                terminating_instances.clear();

                // get the instance utilization and scale up or down
                Optional<Map<String, List<Double>>> instance_utilization = get_instance_utilization();
                if (instance_utilization.isEmpty()) {
                    System.out.println("Failed to get instance utilization, skipping autoscale iteration");
                    continue;
                }
                Optional<Instance> instanceToKill;
                if (must_scale_up(instance_utilization.get())) {
                    scaleUp();
                } else if ((instanceToKill = must_scale_down(instance_utilization.get())).isPresent()) {
                    scaleDown(instanceToKill.get());
                }
            }
        }).start();
    }

    // Check the instance utilization
    private Optional<Map<String, List<Double>>> get_instance_utilization() {
        Map<String, List<Double>> instance_utilization = new HashMap<>();
        synchronized (LoadBalancer.instances) {
            for (Instance instance : LoadBalancer.instances) {
                // System.out.println("Checking instance utilization");
                Optional<Double> cpuUsage = AwsEc2Manager.getCpuUtilization(instance.instanceId());
                if (cpuUsage.isPresent()) {
                    System.out.println("INSTANCE UTILIZATION: Instance " + instance.instanceId() + " has " + cpuUsage.get() + " CPU usage");
                    instance_utilization.put(instance.instanceId(), List.of(cpuUsage.get()));
                } else {
                    return Optional.empty();
                }
            }
        }

        return Optional.of(instance_utilization);
    }

    // Scale up
    private void scaleUp() {
        System.out.println("Scaling up");
        if (LoadBalancer.isCurrentlyDeploying.compareAndSet(false, true)) {
            deployNewInstance();
            LoadBalancer.isCurrentlyDeploying.set(false);
        } else {
            System.out.println("Another thread is already deploying an instance, so not scaling up more");
        }
    }

    // Scale down
    private void scaleDown(Instance instance){
        System.out.println("Scaling down " + instance.instanceId());
        LoadBalancer.instances.remove(instance);
        terminating_instances.add(instance);
    }

    // Check if we must scale up
    private boolean must_scale_up(Map<String, List<Double>> instance_utilization) {
        // if we have the maximum number of instances running, don't scale up
        if (LoadBalancer.instances.size() >= MAX_INSTANCES) {
            return false;
        }

        // if we have no instances, the load balancer will automatically start one
        // TODO; do that here instead
        if (LoadBalancer.instances.isEmpty()) {
            System.out.println("No instances running, loadbalancer will do this on first request");
            return false;
        }

        // scale up if all vms are above max cpu usage in percentage
        for (List<Double> utilizations : instance_utilization.values()) {
            if (utilizations.get(0) < MAX_CPU) {
                return false;
            }
        }

        return true;
    }

    // Check if we must scale down
    private Optional<Instance> must_scale_down(Map<String, List<Double>> instance_utilization) {
        // Don't scale down if we have the minimum number of instances running
        if (LoadBalancer.instances.size() <= MIN_INSTANCES) {
            return Optional.empty();
        }

        // scale down if one vm is below MIN_CPU
        for (Map.Entry<String, List<Double>> entry : instance_utilization.entrySet()) {
            if (entry.getValue().get(0) < MIN_CPU) {
                return Optional.of(LoadBalancer.instances.stream().filter(instance -> instance.instanceId().equals(entry.getKey())).findFirst().get());
            }
        }

        return Optional.empty();
    }

    public static void deployNewInstance() {
        Instance newInst = AwsEc2Manager.deployNewInstance();
        LoadBalancer.instances.add(newInst);
        LoadBalancer.instanceRequests.putIfAbsent(newInst.instanceId(), new HashMap<>());
        System.out.println("New instance deployed with id: " + newInst.instanceId() + " and IP: " + newInst.publicIpAddress());
    }


}
