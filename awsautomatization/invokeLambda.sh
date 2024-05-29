
source ~/.aws/config.sh

# Base64 encode the image
image_b64=$(base64 ../cnv24-g22-master/imageproc/resources/airplane.jpg | tr -d '\n')

# Create the JSON payload directly with required fields
payload=$(jq -n --arg body "$image_b64" '{body: $body, fileFormat: "jpg"}')

# Invoke the Lambda function with raw JSON payload (not base64 encoded)
aws lambda invoke --function-name blur-service --payload "$payload" --cli-binary-format raw-in-base64-out response.json --log-type Tail --query 'LogResult' --output text | base64 -d

