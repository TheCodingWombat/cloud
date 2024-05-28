package deployment_manager;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AwsEc2Manager {

    private static final String AWS_REGION = "eu-west-3";
    private static final String AMI_ID = "ami-0b85d21c87147d1d6";
    private static final String KEY_NAME = "newkey";
    private static final String SEC_GROUP_ID = "sg-070bd3e1192ba2323";
    private static final String DB_TABLE = "MetricsTable";

    private static final Ec2Client ec2 = Ec2Client.builder()
            .region(Region.of(AWS_REGION))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    private static final CloudWatchClient cloudWatch = CloudWatchClient.builder()
            .region(Region.of(AWS_REGION))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    private static final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.of(AWS_REGION))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    public static boolean checkAvailableInstances() {
        System.out.println("Checking available instances.");
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();

        try {
            DescribeInstancesResponse response = ec2.describeInstances(request);

            int totalInstances = 0;
            boolean hasRunningInstance = false;

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if ("running".equals(instance.state().nameAsString())) {
                        totalInstances++;
                        hasRunningInstance = true;
                    }
                }
            }
            System.out.println("You have " + totalInstances + " Amazon EC2 instance(s) which are currently running.");
            return hasRunningInstance;
        } catch (Ec2Exception e) {
            System.err.println("EC2 Exception: " + e.awsErrorDetails().errorMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return false;
        }
    }

    public static Instance deployNewInstance() {
        // AWS SDK code to launch a new EC2 instance
        System.out.println("Starting a new instance.");
        RunInstancesRequest runInstancesRequest = RunInstancesRequest.builder()
                .imageId(AMI_ID)
                .instanceType("t2.micro")
                .minCount(1)
                .maxCount(1)
                .keyName(KEY_NAME)
                .securityGroupIds(SEC_GROUP_ID)
                .monitoring(builder -> builder.enabled(true))
                .build();

        RunInstancesResponse runInstancesResponse = ec2.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResponse.instances().get(0).instanceId();
        Instance newInstance = runInstancesResponse.instances().get(0);

        System.out.println("New EC2 instance deployed but not running: " + newInstanceId);
        waitForInstanceRunning(newInstanceId);

        return getInstanceDetails(newInstanceId);
    }

    private static void waitForInstanceRunning(String instanceId) {
        boolean isRunning = false;

        while (!isRunning) {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            DescribeInstancesResponse response = ec2.describeInstances(request);

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if ("running".equals(instance.state().nameAsString())) {
                        isRunning = true;
                    }
                }
            }

            if (!isRunning) {
                try {
                    Thread.sleep(10000); // Wait for 10 seconds before checking again
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Instance getInstanceDetails(String instanceId) {
        Instance instance = null;
        do {
            DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            DescribeInstancesResponse describeResponse = ec2.describeInstances(describeRequest);
            instance = describeResponse.reservations().get(0).instances().get(0);

            if (instance.publicIpAddress() == null || instance.publicDnsName() == null) {
                try {
                    Thread.sleep(10000); // Wait for 10 seconds before checking again
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (instance.publicIpAddress() == null || instance.publicDnsName() == null);

        return instance;
    }

    public static List<Instance> getAllRunningInstances() {
        List<Instance> runningInstances = new ArrayList<>();
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .filters(Filter.builder()
                        .name("instance-state-name")
                        .values("running")
                        .build())
                .build();

        try {
            DescribeInstancesResponse response = ec2.describeInstances(request);
            for (Reservation reservation : response.reservations()) {
                runningInstances.addAll(reservation.instances());
            }
        } catch (Ec2Exception e) {
            System.err.println("EC2 Exception: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }

        return runningInstances;
    }

    public static double getCpuUtilization(String instanceId) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace("AWS/EC2")
                .metricName("CPUUtilization")
                .dimensions(d -> d.name("InstanceId").value(instanceId))
                .startTime(Instant.now().minusSeconds(3600))  // Last hour
                .endTime(Instant.now())
                .period(60)
                .statistics(Statistic.AVERAGE)
                .build();

        GetMetricStatisticsResponse response = cloudWatch.getMetricStatistics(request);

        if (!response.datapoints().isEmpty()) {
            return response.datapoints().get(0).average();
        } else {
            System.out.println("No data points found for CPU utilization.");
            return 0.0;
        }
    }

    public static double getMemoryUtilization(String instanceId) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace("System/Linux")
                .metricName("MemoryUtilization")
                .dimensions(d -> d.name("InstanceId").value(instanceId))
                .startTime(Instant.now().minusSeconds(3600))  // Last hour
                .endTime(Instant.now())
                .period(60)
                .statistics(Statistic.AVERAGE)
                .build();

        GetMetricStatisticsResponse response = cloudWatch.getMetricStatistics(request);

        if (!response.datapoints().isEmpty()) {
            return response.datapoints().get(0).average();
        } else {
            System.out.println("No data points found for memory utilization.");
            return 0.0;
        }
    }

    public static void main(String[] args) {
        // Example usage
        if (checkAvailableInstances()) {
            List<Instance> instances = getAllRunningInstances();
            for (Instance instance : instances) {
                String instanceId = instance.instanceId();
                double cpuUtilization = getCpuUtilization(instanceId);
                double memoryUtilization = getMemoryUtilization(instanceId);

                System.out.println("Instance ID: " + instanceId);
                System.out.println("CPU Utilization: " + cpuUtilization + "%");
                System.out.println("Memory Utilization: " + memoryUtilization + "%");
            }
        } else {
            deployNewInstance();
        }
    }
}
