#!/usr/bin/env bash
set -e
source ~/.aws/config.sh


aws iam create-role \
	--role-name lambda-role \
	--assume-role-policy-document '{"Version": "2012-10-17","Statement": [{ "Effect": "Allow", "Principal": {"Service": "lambda.amazonaws.com"}, "Action": "sts:AssumeRole"}]}'

sleep 5

aws iam attach-role-policy \
	--role-name lambda-role \
	--policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

sleep 5

echo "Creating blur lambda"
aws lambda create-function --function-name blur-service \
  --zip-file fileb://../cnv24-g22-master/imageproc/target/imageproc-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --handler pt.ulisboa.tecnico.cnv.imageproc.BlurImageHandler \
  --runtime java21 --timeout 30 --memory-size 256 --role arn:aws:iam::$AWS_ACCOUNT_ID:role/lambda-role

echo "Creating enhance lambda"
aws lambda create-function --function-name enhance-service  \
  --zip-file fileb://../cnv24-g22-master/imageproc/target/imageproc-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --handler pt.ulisboa.tecnico.cnv.imageproc.EnhanceImageHandler \
  --runtime java21 --timeout 30 --memory-size 256 --role arn:aws:iam::$AWS_ACCOUNT_ID:role/lambda-role

echo "Creating raytrace lambda"
aws lambda create-function --function-name tracer-service \
  --zip-file fileb://../cnv24-g22-master/raytracer/target/raytracer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --handler pt.ulisboa.tecnico.cnv.raytracer.RaytracerHandler \
  --runtime java21 --timeout 60 --memory-size 256 --role arn:aws:iam::$AWS_ACCOUNT_ID:role/lambda-role

echo "All lambdas created!"