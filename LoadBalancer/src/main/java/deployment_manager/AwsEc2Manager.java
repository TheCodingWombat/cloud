package deployment_manager;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;


import java.time.Instant;
import java.util.*;

public class AwsEc2Manager {

    private static final String AWS_REGION = "eu-west-3";
    private static final String AMI_ID = "ami-0b075650ea4392389";
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

    private static final LambdaClient lambdaClient = LambdaClient.builder()
            .region(Region.of(AWS_REGION))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    public static boolean isInstanceRunning(String instanceId) {
        System.out.println("Checking instance status.");
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();
    
        try {
            DescribeInstancesResponse response = ec2.describeInstances(request);
    
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if (instanceId.equals(instance.instanceId()) && "running".equals(instance.state().nameAsString())) {
                        System.out.println("Instance " + instanceId + " is currently running.");
                        return true;
                    }
                }
            }
            System.out.println("Instance " + instanceId + " is not running.");
            return false;
        } catch (Ec2Exception e) {
            System.err.println("EC2 Exception: " + e.awsErrorDetails().errorMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return false;
        }
    }

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
    public static boolean terminateInstance(String instanceId) {
        System.out.println("Terminating instance: " + instanceId);
        TerminateInstancesRequest terminateRequest = TerminateInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        try {
            TerminateInstancesResponse terminateResponse = ec2.terminateInstances(terminateRequest);
            terminateResponse.terminatingInstances().forEach(instanceChange -> {
                System.out.println("Terminated instance ID: " + instanceChange.instanceId());
                System.out.println("Current state: " + instanceChange.currentState().nameAsString());
                System.out.println("Previous state: " + instanceChange.previousState().nameAsString());
            });
            return true;
        } catch (Ec2Exception e) {
            System.err.println("EC2 Exception: " + e.awsErrorDetails().errorMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return false;
        }
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

    public static Optional<Double> getCpuUtilization(String instanceId) {
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
            return Optional.of(response.datapoints().get(0).average());
        } else {
            System.out.println("No data points found for CPU utilization.");
            return Optional.empty();
        }
    }
    public static void storeMetricInDynamoDB(String timestamp, String requestType, String metric) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("timestamp", AttributeValue.builder().s(timestamp).build());
        item.put("requestType", AttributeValue.builder().s(requestType).build());
        item.put("metric", AttributeValue.builder().s(metric).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(DB_TABLE)
                .item(item)
                .build();

        dynamoDb.putItem(request);
    }

    public static String invokeLambdaFunction(String functionName, String payload) {
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);

        String response = invokeResponse.payload().asUtf8String();
        return response;
    }
}
