import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;

public class EC2LaunchWaitTerminate {

    // TODO - fill fields with correct values.
    private static String AWS_REGION = "us-east-2";
    private static String AMI_ID = "ami-045df467fe4d245ab";
    private static String KEY_NAME = "ec2-ssh-keypar";
    private static String SEC_GROUP_ID = "sg-bacf59c2";

    // Time to wait until the instance is terminated (in milliseconds).
    private static long WAIT_TIME = 1000 * 60 * 10;

    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to the AWS Java SDK!");
        System.out.println("===========================================");

        /*
         * Amazon EC2
         *
         * The AWS EC2 client allows you to create, delete, and administer
         * instances programmatically.
         *
         * In this sample, we use an EC2 client to get a list of all the
         * availability zones, and all instances sorted by reservation id, then 
         * create an instance, list existing instances again, wait a minute and 
         * the terminate the started instance.
         */
        try {
            AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(AWS_REGION).withCredentials(new EnvironmentVariableCredentialsProvider()).build();

            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() + " Availability Zones.");
            System.out.println("You have " + ec2.describeInstances().getReservations().size() + " Amazon EC2 instance(s) running.");
            
            System.out.println("Starting a new instance.");
            RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
            runInstancesRequest.withImageId(AMI_ID)
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName(KEY_NAME)
                               .withSecurityGroupIds(SEC_GROUP_ID);
            RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
            String newInstanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
            System.out.println("You have " + ec2.describeInstances().getReservations().size() + " Amazon EC2 instance(s) running.");
            
            System.out.println("Waiting 10 minutes. See your instance in the AWS console...");
            Thread.sleep(WAIT_TIME);
            
            System.out.println("Terminating the instance.");
            TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
            termInstanceReq.withInstanceIds(newInstanceId);
            ec2.terminateInstances(termInstanceReq);            
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
    }
}
