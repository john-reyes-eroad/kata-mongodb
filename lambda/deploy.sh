#!/bin/sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"

export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y --no-install-recommends curl unzip >/dev/null
rm -rf /var/lib/apt/lists/*

ARCH="$(uname -m)"
case "$ARCH" in
  x86_64|amd64) AWSCLI_ARCH="x86_64" ;;
  aarch64|arm64) AWSCLI_ARCH="aarch64" ;;
  *)
    echo "Unsupported architecture for AWS CLI installer: $ARCH" >&2
    exit 1
    ;;
esac

curl -sSL "https://awscli.amazonaws.com/awscli-exe-linux-${AWSCLI_ARCH}.zip" -o /tmp/awscliv2.zip
unzip -q /tmp/awscliv2.zip -d /tmp
/tmp/aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli --update >/dev/null
rm -rf /tmp/aws /tmp/awscliv2.zip

cd "$REPO_DIR"
mvn -q -pl lambda-authorizer -DskipTests package

ZIP_FILE="$REPO_DIR/lambda-authorizer/target/authorizer.zip"

if aws --endpoint-url "$AWS_ENDPOINT_URL" lambda get-function --function-name authorizer >/dev/null 2>&1; then
  aws --endpoint-url "$AWS_ENDPOINT_URL" lambda update-function-code \
    --function-name authorizer \
    --zip-file "fileb://$ZIP_FILE"
else
  aws --endpoint-url "$AWS_ENDPOINT_URL" lambda create-function \
    --function-name authorizer \
    --runtime java25 \
    --handler com.example.mongocrud.lambda.Authorizer::handleRequest \
    --zip-file "fileb://$ZIP_FILE" \
    --role arn:aws:iam::000000000000:role/lambda-role
fi

aws --endpoint-url "$AWS_ENDPOINT_URL" lambda wait function-active --function-name authorizer
