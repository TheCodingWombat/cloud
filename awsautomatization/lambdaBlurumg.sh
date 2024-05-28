#!/bin/bash
source ~/.aws/config.sh

aws iam create-role \
	--role-name lambda-role \
	--assume-role-policy-document '{"Version": "2012-10-17","Statement": [{ "Effect": "Allow", "Principal": {"Service": "lambda.amazonaws.com"}, "Action": "sts:AssumeRole"}]}'

sleep 5

aws iam attach-role-policy \
	--role-name lambda-role \
	--policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

sleep 5

aws lambda create-function \
	--function-name imageproc-lambda \
	--zip-file fileb://../cnv24-g22-master/imageproc/src/main/java/pt/ulisboa/tecnico/cnv/imageproc/BlurImageHandler.java \
	--handler pt.ulisboa.tecnico.cnv.imageproc \
	--runtime java11 \
	--timeout 5 \
	--memory-size 256 \
	--role arn:aws:iam::$AWS_ACCOUNT_ID:role/lambda-role
