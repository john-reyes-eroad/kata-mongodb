import json
import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handler(event, context):
    headers = event.get("headers", {})
    auth = headers.get("Authorization") or headers.get("authorization", "")

    # Simple Bearer token check — replace with real JWT validation as needed
    is_authorized = auth.startswith("Bearer ")

    logger.info(json.dumps({
        "authorized": is_authorized,
        "auth_header_present": bool(auth),
    }))

    return {"isAuthorized": is_authorized}
