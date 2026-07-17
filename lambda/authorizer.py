def handler(event, context):
    headers = event.get("headers", {})
    auth = headers.get("Authorization") or headers.get("authorization", "")

    # Simple Bearer token check — replace with real JWT validation as needed
    is_authorized = auth.startswith("Bearer ")

    return {"isAuthorized": is_authorized}
