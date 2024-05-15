#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
source $SCRIPT_DIR/config.sh

aws lambda invoke --function-name eg-lambda out --payload '{ "number": "10" }' --log-type Tail --query 'LogResult' --output text |  base64 -d
cat out
