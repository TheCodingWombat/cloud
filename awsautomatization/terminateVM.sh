source ~/.aws/config.sh

# Iterate over each .id file in the outputs directory
for id_file in outputs/*.id; do
    # Read the instance ID from the file
    INSTANCE_ID=$(cat "$id_file")
    
    # Terminate the instance
    aws ec2 terminate-instances --instance-ids $INSTANCE_ID
    
    echo "EC2 instance with ID $INSTANCE_ID terminated successfully."
done