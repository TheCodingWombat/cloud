package load_balancer;

import deployment_manager.AwsEc2Manager;
import metric_storage_system.RequestEstimation;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AutoScaler {

    private static final int autoscale_interval = 1000;
    private static final int MEMORY_THRESHOLD = 100; // 80% memory usage
    private static final int MAX_MEMORY = 970 - MEMORY_THRESHOLD; // 1GB
    private static final int MAX_CPU = 80; // 80%
    private static final int MIN_CPU = 20; // 20%

    public AutoScaler() {
        // run some logic in a new thread to scale the system
        new Thread(() -> {
            System.out.println("AutoScaler started up");
            while (true) {
                System.out.println("Autoscaler iteration");
                try {
                    // get the instance utilization
                    Map<String, List<Double>> instance_utilization = get_instance_utilization();
                    Optional<Instance> instanceToKill;
                    if (must_scale_up(instance_utilization)) {
                        scaleUp();
                    } else if ((instanceToKill = must_scale_down(instance_utilization)).isPresent()) {
                        scaleDown(instanceToKill.get());
                    }

                    Thread.sleep(autoscale_interval);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Check the instance utilization
    private Map<String, List<Double>> get_instance_utilization() {
        Map<String, List<Double>> instance_utilization = new HashMap<>();
        LoadBalancer.instances.forEach(instance -> {
            System.out.println("Checking instance utilization");
            double cpuUsage = AwsEc2Manager.getCpuUtilization(instance.instanceId());
            System.out.println("Instance " + instance.instanceId() + " has " + cpuUsage + " CPU usage");
            instance_utilization.put(instance.instanceId(), List.of(cpuUsage));
        });

        return instance_utilization;
    }

    // Scale up
    private void scaleUp() {
        System.out.println("Scaling up");
    }

    // Scale down
    private void scaleDown(Instance instance){
        System.out.println("Scaling down");
    }

    // Check if we must scale up
    private boolean must_scale_up(Map<String, List<Double>> instance_utilization) {
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
        System.out.println("Checking if we must scale down");
        // scale down if one vm is below MIN_CPU
        for (Map.Entry<String, List<Double>> entry : instance_utilization.entrySet()) {
            if (entry.getValue().get(0) < MIN_CPU) {
                return Optional.of(LoadBalancer.instances.stream().filter(instance -> instance.instanceId().equals(entry.getKey())).findFirst().get());
            }
        }

        return Optional.empty();
    }


}
