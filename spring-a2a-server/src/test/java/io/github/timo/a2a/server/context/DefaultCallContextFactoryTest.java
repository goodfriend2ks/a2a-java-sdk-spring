package io.github.timo.a2a.server.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCallContextFactoryTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private Authentication authentication;

    private DefaultCallContextFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultCallContextFactory("test-tenant");
    }

    @Test
    void shouldCreateFactoryWithDefaultConstructor() {
        // When
        DefaultCallContextFactory defaultFactory = new DefaultCallContextFactory();

        // Then
        assertNotNull(defaultFactory);
    }

    @Test
    void shouldCreateFactoryWithTenantUid() {
        // When
        DefaultCallContextFactory tenantFactory = new DefaultCallContextFactory("tenant-123");

        // Then
        assertNotNull(tenantFactory);
    }

    @Test
    void shouldBuildContextWithAuthenticatedUser() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Custom-Header", "custom-value");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(authentication.getPrincipal()).thenReturn("user-principal");

        // When
        TenantServerCallContext context = factory.build(exchange, authentication, "testMethod");

        // Then
        assertNotNull(context);
        assertEquals("test-tenant", context.getTenantUid());
    }

    @Test
    void shouldBuildContextWithUnauthenticatedUser() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, "testMethod");

        // Then
        assertNotNull(context);
        assertEquals("test-tenant", context.getTenantUid());
    }

    @Test
    void shouldHandleNullAuthentication() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, null);

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldHandleMultipleHeaderValues() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.put("Multi-Header", List.of("value1", "value2", "value3"));

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, "testMethod");

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldHandleEmptyHeaderValues() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.put("Empty-Header", List.of());

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, "testMethod");

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldExtractA2AVersionHeader() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-A2A-Version", "1.0.0");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, "testMethod");

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldExtractA2AExtensionsHeader() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.put("X-A2A-Extensions", List.of("ext1", "ext2"));

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, "testMethod");

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldHandleNullMethodName() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        // When
        TenantServerCallContext context = factory.build(exchange, null, null);

        // Then
        assertNotNull(context);
    }

    @Test
    void shouldHandleAuthenticationWithNullPrincipal() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        TenantServerCallContext context = factory.build(exchange, authentication, "testMethod");

        // Then
        assertNotNull(context);
    }
}
