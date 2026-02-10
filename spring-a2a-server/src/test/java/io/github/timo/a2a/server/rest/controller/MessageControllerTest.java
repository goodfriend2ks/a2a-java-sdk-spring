package io.github.timo.a2a.server.rest.controller;

import io.github.timo.a2a.server.context.CallContextFactory;
import io.github.timo.a2a.server.context.TenantServerCallContext;
import io.github.timo.a2a.server.rest.handler.AgentRestHandler;
import io.a2a.transport.rest.handler.RestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private AgentRestHandler agentRestHandler;

    @Mock
    private CallContextFactory callContextFactory;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private Authentication authentication;

    @Mock
    private TenantServerCallContext callContext;

    @Mock
    private RestHandler.HTTPRestResponse restResponse;

    private MessageController controller;

    @BeforeEach
    void setUp() {
        controller = new MessageController(agentRestHandler, callContextFactory);
    }

    @Test
    void shouldSendMessage() {
        // Given
        String jsonRpcMessage = "{\"method\": \"sendMessage\", \"params\": {}}";
        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendMessage(anyString(), anyString(), any())).thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{\"result\": \"success\"}");

        // When
        ResponseEntity<?> response = controller.sendMessage(exchange, authentication, jsonRpcMessage);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agentRestHandler).sendMessage(eq(jsonRpcMessage), eq("test-tenant"), eq(callContext));
    }

    @Test
    void shouldSendTextMessage() {
        // Given
        String userMessage = "Hello, agent!";
        String contextId = "ctx-123";
        String taskId = "task-456";

        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendUserMessage(anyString(), anyString(), any(), any(), any()))
                .thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{\"result\": \"success\"}");

        // When
        ResponseEntity<?> response = controller.sendTextMessage(
                exchange, authentication, contextId, taskId, userMessage
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agentRestHandler).sendUserMessage(
                eq(userMessage), eq("test-tenant"), eq(callContext), eq(contextId), eq(taskId)
        );
    }

    @Test
    void shouldSendTextMessageWithNullContextAndTaskId() {
        // Given
        String userMessage = "Hello!";

        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendUserMessage(anyString(), anyString(), any(), any(), any()))
                .thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{\"result\": \"success\"}");

        // When
        ResponseEntity<?> response = controller.sendTextMessage(
                exchange, authentication, null, null, userMessage
        );

        // Then
        assertNotNull(response);
        verify(agentRestHandler).sendUserMessage(
                eq(userMessage), eq("test-tenant"), eq(callContext), isNull(), isNull()
        );
    }

    @Test
    void shouldSendMessageStreaming() {
        // Given
        String jsonRpcMessage = "{\"method\": \"sendMessage\", \"params\": {}}";
        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendStreamingMessage(anyString(), anyString(), any())).thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("text/event-stream");

        // When
        ResponseEntity<?> response = controller.sendMessageStreaming(exchange, authentication, jsonRpcMessage);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agentRestHandler).sendStreamingMessage(eq(jsonRpcMessage), eq("test-tenant"), eq(callContext));
    }

    @Test
    void shouldSendTextMessageStreaming() {
        // Given
        String userMessage = "Streaming message";
        String contextId = "ctx-789";
        String taskId = "task-101";

        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendStreamingUserMessage(anyString(), anyString(), any(), any(), any()))
                .thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("text/event-stream");

        // When
        ResponseEntity<?> response = controller.sendTextMessageStreaming(
                exchange, authentication, contextId, taskId, userMessage
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agentRestHandler).sendStreamingUserMessage(
                eq(userMessage), eq("test-tenant"), eq(callContext), eq(contextId), eq(taskId)
        );
    }

    @Test
    void shouldHandleNullAuthentication() {
        // Given
        String jsonRpcMessage = "{\"method\": \"sendMessage\"}";
        when(callContextFactory.build(any(), isNull(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendMessage(anyString(), anyString(), any())).thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{}");

        // When
        ResponseEntity<?> response = controller.sendMessage(exchange, null, jsonRpcMessage);

        // Then
        assertNotNull(response);
        verify(callContextFactory).build(eq(exchange), isNull(), anyString());
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        String emptyMessage = "";
        when(callContextFactory.build(any(), any(), anyString())).thenReturn(callContext);
        when(callContext.getTenantUid()).thenReturn("test-tenant");
        when(agentRestHandler.sendUserMessage(anyString(), anyString(), any(), any(), any()))
                .thenReturn(restResponse);
        when(restResponse.getStatusCode()).thenReturn(200);
        when(restResponse.getContentType()).thenReturn("application/json");
        when(restResponse.getBody()).thenReturn("{}");

        // When
        ResponseEntity<?> response = controller.sendTextMessage(
                exchange, authentication, null, null, emptyMessage
        );

        // Then
        assertNotNull(response);
        verify(agentRestHandler).sendUserMessage(eq(emptyMessage), anyString(), any(), any(), any());
    }
}
