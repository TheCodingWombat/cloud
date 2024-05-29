#!/bin/bash

source ~/.aws/config.sh

# Run new instance.
aws ec2 run-instances \
	--image-id ami-0b85d21c87147d1d6\
	--instance-type t2.micro \
	--key-name $AWS_KEYPAIR_NAME \
	--security-group-ids $AWS_SECURITY_GROUP \
	--monitoring Enabled=true | jq -r ".Instances[0].InstanceId" > outputs/instance.id
echo "New instance with id $(cat outputs/instance.id)."

# Wait for instance to be running.
aws ec2 wait instance-running --instance-ids $(cat outputs/instance.id)
echo "New instance with id $(cat outputs/instance.id) is now running."

# Extract DNS nane.
aws ec2 describe-instances \
	--instance-ids $(cat outputs/instance.id) | jq -r ".Reservations[0].Instances[0].NetworkInterfaces[0].PrivateIpAddresses[0].Association.PublicDnsName" > outputs/instance.dns
echo "New instance with id $(cat outputs/instance.id) has address $(cat outputs/instance.dns)."

# Wait for instance to have SSH ready.
while ! nc -z $(cat outputs/instance.dns) 22; do
	echo "Waiting for $(cat outputs/instance.dns):22 (SSH)..."
	sleep 0.5
done
echo "New instance with id $(cat outputs/instance.id) is ready for SSH access."
