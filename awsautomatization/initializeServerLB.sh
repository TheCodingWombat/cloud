#!/bin/bash
source ~/.aws/config.sh

INSTANCE_SERVICE=$(aws ec2 run-instances \
    --image-id ami-01ca7a31897ed5a69 \
    --instance-type t2.micro \
    --key-name $AWS_KEYPAIR_NAME \
    --security-group-ids $AWS_SECURITY_GROUP \
    --monitoring Enabled=true \
    --query 'Instances[0].InstanceId' \
    --output text)
echo "New Amazon Linux instance with id $INSTANCE_SERVICE."

INSTANCE_LB=$(aws ec2 run-instances \
    --image-id ami-0cc5962e2aee0bc60  \
    --instance-type t2.micro \
    --key-name $AWS_KEYPAIR_NAME \
    --security-group-ids $AWS_SECURITY_GROUP \
    --monitoring Enabled=true \
    --query 'Instances[0].InstanceId' \
    --output text)
echo "New Amazon Linux instance with id $INSTANCE_LB."

# Wait until both instances are running
aws ec2 wait instance-running --instance-ids $INSTANCE_SERVICE 
aws ec2 wait instance-running --instance-ids $INSTANCE_LB
echo "Both instances are now running"

INSTANCE_SERVICE_DNS=$(aws ec2 describe-instances --instance-ids $INSTANCE_SERVICE --query "Reservations[].Instances[].PublicDnsName" --output text)
INSTANCE_LB_DNS=$(aws ec2 describe-instances --instance-ids $INSTANCE_LB --query "Reservations[].Instances[].PublicDnsName" --output text)

# Save the Instance ID and Public DNS of InstanceB to files
echo $INSTANCE_SERVICE > outputs/instanceService.id
echo $INSTANCE_SERVICE_DNS > outputs/instanceService.dns

echo $INSTANCE_LB > outputs/instanceLB.id
echo $INSTANCE_LB_DNS > outputs/instanceLB.dns

# Copy DNS files to the instances
scp -i "~/.aws/newkey.pem" -o StrictHostKeyChecking=no outputs/instanceLB.dns ec2-user@$INSTANCE_SERVICE_DNS:other_instance_dns.dns
scp -i "~/.aws/newkey.pem" -o StrictHostKeyChecking=no outputs/instanceService.dns ec2-user@$INSTANCE_LB_DNS:other_instance_dns.dns
echo "Copy files"

echo "Now Replacing localhost to DNS" 

#!/bin/bash

# Command to read DNS value from service.dns and replace "localhost" with this value in the Java file
cmd=$(cat <<EOF
#!/bin/bash
# Read the DNS value from the other_instance_dns.dns file
OTHER_INSTANCE_DNS=\$(cat /home/ec2-user/other_instance_dns.dns)

# Replace "localhost" with the DNS value in the specified Java file directly
sed -i "s/localhost/\$OTHER_INSTANCE_DNS/" /home/ec2-user/cloud/LoadBalancer/src/main/java/load_balancer/LoadBalancer.java
EOF
)

# Execute the command on the remote machine
ssh -o StrictHostKeyChecking=no -i "~/.aws/newkey.pem" ec2-user@$INSTANCE_LB_DNS "$cmd"
