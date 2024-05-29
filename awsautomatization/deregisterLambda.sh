#!/bin/bash

source ~/.aws/config.sh

aws lambda delete-function --function-name blur-service 
aws lambda delete-function --function-name enhance-service
aws lambda delete-function --function-name tracer-service


aws iam detach-role-policy \
	--role-name lambda-role\
	--policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

aws iam delete-role --role-name lambda-role