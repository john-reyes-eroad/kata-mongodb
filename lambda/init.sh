#!/bin/bash
set -e

echo "Deploying Lambda authorizer to LocalStack..."

python3 -c "
import zipfile
with zipfile.ZipFile('/tmp/authorizer.zip', 'w') as z:
    z.write('/lambda-src/authorizer.py', 'authorizer.py')
"

awslocal lambda create-function \
  --function-name authorizer \
  --runtime python3.12 \
  --handler authorizer.handler \
  --zip-file fileb:///tmp/authorizer.zip \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --region us-east-1

echo "Lambda authorizer deployed."
