import boto3
import zipfile
import io
import os

endpoint_url = os.getenv("AWS_ENDPOINT_URL", "http://moto:5000")

client = boto3.client(
    "lambda",
    endpoint_url=endpoint_url,
    region_name="us-east-1",
    aws_access_key_id="test",
    aws_secret_access_key="test",
)

buf = io.BytesIO()
with zipfile.ZipFile(buf, "w") as z:
    z.write("/lambda-src/authorizer.py", "authorizer.py")
zip_content = buf.getvalue()

function_name = "authorizer"

try:
    client.get_function(FunctionName=function_name)
    print("Updating existing function...")
    client.update_function_code(FunctionName=function_name, ZipFile=zip_content)
except client.exceptions.ResourceNotFoundException:
    print("Creating new function...")
    client.create_function(
        FunctionName=function_name,
        Runtime="python3.12",
        Handler="authorizer.handler",
        Code={"ZipFile": zip_content},
        Role="arn:aws:iam::000000000000:role/lambda-role",
    )

print("Waiting for Lambda function to become active...")
client.get_waiter("function_active").wait(FunctionName=function_name)

print("Lambda authorizer deployed and active.")
