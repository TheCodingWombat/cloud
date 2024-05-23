import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class EC2MeasureCPU {

    // TODO - fill fields with correct values.
    private static final String AWS_REGION = "us-east-2";

    // Total observation time in milliseconds.
    private static final long OBS_TIME = 1000 * 60 * 20;

    private static Set<Instance> getInstances(AmazonEC2 ec2) throws Exception {
        Set<Instance> instances = new HashSet<Instance>();
        for (Reservation reservation : ec2.describeInstances().getReservations()) {
            instances.addAll(reservation.getInstances());
        }
        return instances;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("Welcome to the AWS Java SDK!");
        System.out.println("===========================================");

        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(AWS_REGION).withCredentials(new EnvironmentVariableCredentialsProvider()).build();
        AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion(AWS_REGION).withCredentials(new EnvironmentVariableCredentialsProvider()).build();

        try {
            Set<Instance> instances = getInstances(ec2);
            System.out.println("total instances = " + instances.size());

            Dimension instanceDimension = new Dimension();
            instanceDimension.setName("InstanceId");
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(instanceDimension);

            for (Instance instance : instances) {
                String iid = instance.getInstanceId();
                String state = instance.getState().getName();
                if (state.equals("running")) { 
                    System.out.println("running instance id = " + iid);
                    instanceDimension.setValue(iid);
                    GetMetricStatisticsRequest request = new GetMetricStatisticsRequest().withStartTime(new Date(new Date().getTime() - OBS_TIME))
                                                                                         .withNamespace("AWS/EC2")
                                                                                         .withPeriod(60)
                                                                                         .withMetricName("CPUUtilization")
                                                                                         .withStatistics("Average")
                                                                                         .withDimensions(instanceDimension)
                                                                                         .withEndTime(new Date());
                     for (Datapoint dp : cloudWatch.getMetricStatistics(request).getDatapoints()) {
                       System.out.println(" CPU utilization for instance " + iid + " = " + dp.getAverage());
                     }
                 }
                 else {
                    System.out.println("instance id = " + iid);
                 }
                System.out.println("Instance State : " + state +".");
            }
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
    }
}
