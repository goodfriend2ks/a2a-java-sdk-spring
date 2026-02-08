package com.timo.ai.a2a.server.rest;

import io.a2a.spec.A2AError;
import org.jspecify.annotations.NonNull;

public class RestErrorResponse {
    private final String error;
    private final String message;

    public RestErrorResponse(A2AError jsonRpcError) {
        this.error = jsonRpcError.getClass().getName();
        this.message = jsonRpcError.getMessage();
    }

    public String toJson() {
        return String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("HTTPRestErrorResponse{error=%s, message=%s", error, message);
    }
}
