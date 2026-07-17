import json
import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handler(event, context):
    token = event.get("authorizationToken", "")
    method_arn = event.get("methodArn", "*")

    # Simple Bearer token check — replace with real JWT validation as needed
    is_authorized = token.startswith("Bearer ")
    effect = "Allow" if is_authorized else "Deny"

    logger.info(json.dumps({
        "effect": effect,
        "token_present": bool(token),
        "method_arn": method_arn,
    }))

    return {
        "principalId": "user",
        "policyDocument": {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Action": "execute-api:Invoke",
                    "Effect": effect,
                    "Resource": method_arn,
                }
            ],
        },
    }
