package com.timo.ai.a2a.server.card;

import io.a2a.spec.AgentCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentCardControllerTest {

    @Mock
    private AgentCard agentCard;

    private AgentCardController controller;

    @BeforeEach
    void setUp() {
        controller = new AgentCardController(agentCard);
    }

    @Test
    void shouldReturnAgentCard() {
        // Given
        when(agentCard.name()).thenReturn("Test Agent");
        when(agentCard.description()).thenReturn("Test description");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldHandleAgentCardWithMinimalData() {
        // Given
        when(agentCard.name()).thenReturn("Minimal Agent");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturnJsonContentType() {
        // Given
        when(agentCard.name()).thenReturn("Test Agent");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldReturnOkStatus() {
        // Given
        when(agentCard.name()).thenReturn("Test Agent");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldIncludeAgentCardBody() {
        // Given
        when(agentCard.name()).thenReturn("Test Agent");
        when(agentCard.description()).thenReturn("Description");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertNotNull(response.getBody());
        String body = (String) response.getBody();
        assertFalse(body.isEmpty());
    }

    @Test
    void shouldHandleAgentCardWithComplexData() {
        // Given
        when(agentCard.name()).thenReturn("Complex Agent");
        when(agentCard.description()).thenReturn("Complex description with special chars: @#$%");

        // When
        ResponseEntity<?> response = controller.getAgentCard();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
