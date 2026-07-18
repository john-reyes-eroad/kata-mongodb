# Kong API with AWS Lambda authorizer

This project uses Kong as the public gateway and delegates authorization to a Lambda function running in floci.

## Request flow

1. Client sends request to Kong at `http://localhost:9090` with `Authorization` header.
2. Kong `pre-function` plugin (Lua) builds a TOKEN authorizer event:
   - `type`
   - `authorizationToken`
   - `methodArn`
3. Kong calls Lambda invoke endpoint in floci:
   - `http://floci:4566/2015-03-31/functions/authorizer/invocations`
4. Lambda returns IAM-style policy document with `Effect` set to `Allow` or `Deny`.
5. Kong behavior:
   - `Allow` -> proxies request to app (`http://app:8080`)
   - not `Allow` -> returns `403 Forbidden`
   - Lambda unavailable/non-200 -> returns `500 Authorization service unavailable`

Kong caches each auth decision for 60 seconds, keyed by authorization token and request method/path (`methodArn`).

## Where it is configured

- Kong Lua logic: `kong/scripts/authorizer-pre-function.lua`
- Kong declarative template: `kong/kong.yml` (rendered to `/tmp/kong.yml` at container startup)
- Kong render script: `kong/render-kong-config.sh`
- Lambda implementation (Java 25): `lambda-authorizer/src/main/java/com/example/mongocrud/lambda/Authorizer.java`
- Lambda packaging and deployment script: `lambda/deploy.sh`
- Compose wiring (`floci`, `lambda-init`, `kong`): `docker-compose.yml`

## Lambda deployment model

`lambda-init` runs once on startup and executes `lambda/deploy.sh`, which:

1. Installs AWS CLI v2 in the container
2. Builds `lambda-authorizer` Maven module
3. Uses generated `lambda-authorizer/target/authorizer.zip`
4. Creates or updates Lambda `authorizer` in floci
5. Waits until function state is active

Lambda runtime and handler:

- Runtime: `java25`
- Handler: `com.example.mongocrud.lambda.Authorizer::handleRequest`

## Current authorization rule

The authorizer currently allows any non-empty header that starts with `Bearer `.
It denies all other requests.

This is intentionally simple for local testing and can be replaced with real JWT verification later.

## Pros and cons of using Kong API gateway

| Pros | Cons |
| --- | --- |
| Centralized cross-cutting concerns (auth, routing, rate limiting, logging) | Extra network hop adds latency |
| Keeps app service focused on domain logic | More moving parts to operate and debug |
| Flexible plugin model, including custom Lua flow | Plugin/Lua logic increases gateway complexity |
| Easy to enforce consistent security policy for all endpoints | Gateway becomes a critical dependency and potential bottleneck |
| Good fit for multi-service expansion in future | Local/dev setup is heavier than direct app access |
