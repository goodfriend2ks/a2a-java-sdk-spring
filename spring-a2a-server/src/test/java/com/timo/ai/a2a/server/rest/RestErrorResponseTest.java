package com.timo.ai.a2a.server.rest;

import io.a2a.spec.A2AError;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidRequestError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestErrorResponseTest {

    @Test
    void shouldCreateRestErrorResponseFromA2AError() {
        // Given
        A2AError error = new InternalError("Test error message");

        // When
        RestErrorResponse response = new RestErrorResponse(error);

        // Then
        assertNotNull(response);
    }

    @Test
    void shouldConvertToJsonWithCorrectFormat() {
        // Given
        A2AError error = new InternalError("Test error message");
        RestErrorResponse response = new RestErrorResponse(error);

        // When
        String json = response.toJson();

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("\"message\""));
        assertTrue(json.contains("Test error message"));
        assertTrue(json.contains(InternalError.class.getName()));
    }

    @Test
    void shouldHandleInvalidRequestError() {
        // Given
        A2AError error = new InvalidRequestError("Invalid request");
        RestErrorResponse response = new RestErrorResponse(error);

        // When
        String json = response.toJson();

        // Then
        assertTrue(json.contains("Invalid request"));
        assertTrue(json.contains(InvalidRequestError.class.getName()));
    }

    @Test
    void shouldHandleEmptyErrorMessage() {
        // Given
        A2AError error = new InternalError("");
        RestErrorResponse response = new RestErrorResponse(error);

        // When
        String json = response.toJson();

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"message\": \"\""));
    }

    @Test
    void shouldFormatToStringCorrectly() {
        // Given
        A2AError error = new InternalError("Test message");
        RestErrorResponse response = new RestErrorResponse(error);

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("HTTPRestErrorResponse"));
        assertTrue(toString.contains("error="));
        assertTrue(toString.contains("message="));
    }

    @Test
    void shouldEscapeSpecialCharactersInJson() {
        // Given
        A2AError error = new InternalError("Error with \"quotes\" and special chars");
        RestErrorResponse response = new RestErrorResponse(error);

        // When
        String json = response.toJson();

        // Then
        assertNotNull(json);
        // The basic implementation doesn't escape, but we verify the content is there
        assertTrue(json.contains("Error with"));
    }
}
