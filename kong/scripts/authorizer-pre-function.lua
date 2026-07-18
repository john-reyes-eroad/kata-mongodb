local http = require("resty.http")
local cjson = require("cjson")
local CACHE_TTL_SECONDS = 60

local function invoke_authorizer(authorization_token, method_arn)
  local payload = cjson.encode({
    type = "TOKEN",
    authorizationToken = authorization_token,
    methodArn = method_arn,
  })

  local res, err
  for attempt = 1, 3 do
    local httpc = http.new()
    res, err = httpc:request_uri(
      "http://floci:4566/2015-03-31/functions/authorizer/invocations",
      {
        method = "POST",
        body = payload,
        headers = { ["Content-Type"] = "application/json" },
      }
    )
    if res and res.status == 200 then break end
    ngx.sleep(1)
  end

  if err or not res or res.status ~= 200 then
    return nil, "Authorization service unavailable"
  end

  local result = cjson.decode(res.body)
  local statements = result.policyDocument
    and result.policyDocument.Statement
  local effect = statements and statements[1] and statements[1].Effect

  return effect == "Allow"
end

local method_arn = "arn:aws:execute-api:us-east-1:000000000000:local/"
  .. kong.request.get_method() .. kong.request.get_path()
local authorization_token = kong.request.get_header("Authorization") or ""
local cache_key = "lambda-authorizer:" .. authorization_token .. ":" .. method_arn

local allowed, cache_err = kong.cache:get(
  cache_key,
  { ttl = CACHE_TTL_SECONDS },
  invoke_authorizer,
  authorization_token,
  method_arn
)

if cache_err then
  return kong.response.exit(500, { message = "Authorization service unavailable" })
end

if not allowed then
  return kong.response.exit(403, { message = "Forbidden" })
end
