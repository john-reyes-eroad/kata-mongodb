package com.example.mongocrud.lambda;

import java.util.List;
import java.util.Map;

public class Authorizer {

    public Map<String, Object> handleRequest(Map<String, Object> event) {
        var tokenValue = event.get("authorizationToken");
        var token = tokenValue == null ? "" : String.valueOf(tokenValue);

        var methodArnValue = event.get("methodArn");
        var methodArn = methodArnValue == null ? "*" : String.valueOf(methodArnValue);
        var effect = token.startsWith("Bearer ") ? "Allow" : "Deny";

        var statement = Map.of(
                "Action", "execute-api:Invoke",
                "Effect", effect,
                "Resource", methodArn
        );

        var policyDocument = Map.of(
                "Version", "2012-10-17",
                "Statement", List.of(statement)
        );

        return Map.of(
                "principalId", "user",
                "policyDocument", policyDocument
        );
    }
}
