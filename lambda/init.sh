#!/bin/bash
set -e

echo "Deploying Lambda authorizer to LocalStack..."

python3 -c "
import zipfile
with zipfile.ZipFile('/tmp/authorizer.zip', 'w') as z:
    z.write('/lambda-src/authorizer.py', 'authorizer.py')
"

if awslocal lambda get-function --function-name authorizer --region us-east-1 > /dev/null 2>&1; then
  echo "Function already exists, updating code..."
  awslocal lambda update-function-code \
    --function-name authorizer \
    --zip-file fileb:///tmp/authorizer.zip \
    --region us-east-1
else
  awslocal lambda create-function \
    --function-name authorizer \
    --runtime python3.12 \
    --handler authorizer.handler \
    --zip-file fileb:///tmp/authorizer.zip \
    --role arn:aws:iam::000000000000:role/lambda-role \
    --region us-east-1
fi

echo "Waiting for Lambda function to become active..."
awslocal lambda wait function-active \
  --function-name authorizer \
  --region us-east-1

echo "Lambda authorizer deployed and active."
