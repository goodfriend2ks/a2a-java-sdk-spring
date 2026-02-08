package com.timo.ai.a2a.server.rest;

import io.a2a.spec.A2AError;
import io.a2a.transport.rest.handler.RestHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.adapter.JdkFlowAdapter;

public final class ResponseUtils {
    private ResponseUtils() {
        // N/A
    }

    public static ResponseEntity<?> toResponseEntity(RestHandler.HTTPRestResponse restResponse) {
        var builder = ResponseEntity.status(restResponse.getStatusCode());

        try {
            builder.contentType(MediaType.parseMediaType(restResponse.getContentType()));
        } catch (Exception ex) {
            // N/A
        }

        if (restResponse instanceof RestHandler.HTTPRestStreamingResponse restStreamingResponse) {
            return builder.body(JdkFlowAdapter.flowPublisherToFlux(restStreamingResponse.getPublisher()));
        }

        return builder.body(restResponse.getBody());
    }

    public static ResponseEntity<?> toResponseEntity(A2AError error) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RestErrorResponse(error).toJson());
    }
}
