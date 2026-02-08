package com.timo.ai.a2a.server.rest;

import io.a2a.spec.A2AError;
import io.a2a.spec.InternalError;
import io.a2a.transport.rest.handler.RestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponseUtilsTest {

    @Mock
    private RestHandler.HTTPRestResponse restResponse;

    @Mock
    private RestHandler.HTTPRestStreamingResponse streamingResponse;

    @Test
    void shouldConvertRestResponseToResponseEntity() {
        // Given
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{\"result\": \"success\"}");

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(restResponse);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"result\": \"success\"}", responseEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
    }

    @Test
    void shouldHandleInvalidContentType() {
        // Given
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("invalid-content-type");
        when(restResponse.getBody()).thenReturn("body");

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(restResponse);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("body", responseEntity.getBody());
    }

    @Test
    void shouldHandleStreamingResponse() {
        // Given
        Flow.Publisher<String> publisher = subscriber -> {
            // Mock publisher
        };

        when(streamingResponse.getStatusCode()).thenReturn(200);
        when(streamingResponse.getContentType()).thenReturn("application/json");
        when(streamingResponse.getPublisher()).thenReturn(publisher);

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(streamingResponse);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertInstanceOf(Flux.class, responseEntity.getBody());
    }

    @Test
    void shouldHandleNullContentType() {
        // Given
        when(restResponse.getStatusCode()).thenReturn(204);
        when(restResponse.getContentType()).thenReturn(null);
        when(restResponse.getBody()).thenReturn(null);

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(restResponse);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    void shouldConvertA2AErrorToResponseEntity() {
        // Given
        A2AError error = new InternalError("Something went wrong");

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(error);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        String body = (String) responseEntity.getBody();
        assertNotNull(body);
        assertTrue(body.contains("error"));
        assertTrue(body.contains("message"));
        assertTrue(body.contains("Something went wrong"));
    }

    @Test
    void shouldHandleDifferentStatusCodes() {
        // Given
        when(restResponse.getStatusCode()).thenReturn(404);
        when(restResponse.getContentType()).thenReturn("text/plain");
        when(restResponse.getBody()).thenReturn("Not Found");

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(restResponse);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Not Found", responseEntity.getBody());
    }

    @Test
    void shouldHandleEmptyBody() {
        // Given
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("");

        // When
        ResponseEntity<?> responseEntity = ResponseUtils.toResponseEntity(restResponse);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("", responseEntity.getBody());
    }
}
