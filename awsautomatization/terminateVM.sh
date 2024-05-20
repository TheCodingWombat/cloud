# Step 6: terminate the vm instance.
aws ec2 terminate-instances --instance-ids $(cat outputs/instance.id)

echo "EC2 terminated successfully"
