# Kong `authorizer-pre-function.lua` explained

This file documents how `kong/scripts/authorizer-pre-function.lua` works in the request path.

## What this script does

The script runs in Kong's `pre-function` plugin during the `access` phase.  
Its job is to call the local Lambda authorizer and decide whether Kong should allow the request to continue to the upstream app.
To reduce repeated Lambda calls, it caches each authorization decision in Kong for 60 seconds.

## Step-by-step flow

1. Loads dependencies:
   - `resty.http` for HTTP calls
   - `cjson` for JSON encoding and decoding
2. Builds a `methodArn` string from:
   - request method (`kong.request.get_method()`)
   - request path (`kong.request.get_path()`)
3. Reads the `Authorization` header and builds a cache key:
   - `lambda-authorizer:<authorization-token>:<method-arn>`
4. Checks Kong cache for an existing decision with a 60-second TTL.
5. On cache miss, builds the Lambda TOKEN-authorizer payload:
   - `type`: `"TOKEN"`
   - `authorizationToken`: value of `Authorization` header, or empty string
   - `methodArn`: value built above
6. Calls Lambda invoke endpoint at:
   - `http://floci:4566/2015-03-31/functions/authorizer/invocations`
7. Retries up to 3 times if the response is missing or not HTTP 200:
   - waits 1 second between attempts
8. If invoke still fails, returns:
   - `500` with `{"message":"Authorization service unavailable"}`
9. If invoke succeeds, reads `policyDocument.Statement[1].Effect` from Lambda response and caches allow or deny for 60 seconds.
10. If effect is not `"Allow"`, returns:
   - `403` with `{"message":"Forbidden"}`
11. If effect is `"Allow"`, the script exits without blocking, and Kong proxies the request to the upstream service.

## Decision matrix

| Condition | Outcome |
| --- | --- |
| Cache hit with cached allow | Request is forwarded to upstream |
| Cache hit with cached deny | `403 Forbidden` |
| Cache miss and Lambda call fails after retries or non-200 result | `500 Authorization service unavailable` |
| Cache miss and Lambda returns policy effect other than `Allow` | `403 Forbidden` (cached for 60 seconds) |
| Cache miss and Lambda returns policy effect `Allow` | Request is forwarded (cached for 60 seconds) |

## Notes

- The script only checks the first statement effect (`Statement[1].Effect`).
- It expects Lambda to return an IAM-style policy document.
- Cache TTL is 60 seconds (`CACHE_TTL_SECONDS = 60`).
- The current local authorizer behavior is simple and can later be replaced with full JWT validation logic.
