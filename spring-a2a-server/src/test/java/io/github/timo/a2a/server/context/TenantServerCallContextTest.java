package io.github.timo.a2a.server.context;

import io.a2a.server.auth.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TenantServerCallContextTest {

    @Mock
    private User user;

    @Test
    void shouldCreateContextWithBasicConstructor() {
        // Given
        Map<String, Object> state = Map.of("key", "value");
        Set<String> extensions = Set.of("ext1", "ext2");
        String tenantId = "tenant-123";

        // When
        TenantServerCallContext context = new TenantServerCallContext(user, state, extensions, tenantId);

        // Then
        assertNotNull(context);
        assertEquals(tenantId, context.getTenantUid());
    }

    @Test
    void shouldCreateContextWithProtocolVersion() {
        // Given
        Map<String, Object> state = Map.of("key", "value");
        Set<String> extensions = Set.of("ext1");
        String protocolVersion = "1.0.0";
        String tenantId = "tenant-456";

        // When
        TenantServerCallContext context = new TenantServerCallContext(
                user,
                state,
                extensions,
                protocolVersion,
                tenantId
        );

        // Then
        assertNotNull(context);
        assertEquals(tenantId, context.getTenantUid());
    }

    @Test
    void shouldHandleNullProtocolVersion() {
        // Given
        Map<String, Object> state = Map.of("key", "value");
        Set<String> extensions = Set.of("ext1");
        String tenantId = "tenant-789";

        // When
        TenantServerCallContext context = new TenantServerCallContext(
                user,
                state,
                extensions,
                null,
                tenantId
        );

        // Then
        assertNotNull(context);
        assertEquals(tenantId, context.getTenantUid());
    }

    @Test
    void shouldHandleEmptyState() {
        // Given
        Map<String, Object> state = Map.of();
        Set<String> extensions = Set.of();
        String tenantId = "tenant-empty";

        // When
        TenantServerCallContext context = new TenantServerCallContext(user, state, extensions, tenantId);

        // Then
        assertNotNull(context);
        assertEquals(tenantId, context.getTenantUid());
    }

    @Test
    void shouldHandleEmptyTenantId() {
        // Given
        Map<String, Object> state = Map.of("key", "value");
        Set<String> extensions = Set.of("ext1");
        String tenantId = "";

        // When
        TenantServerCallContext context = new TenantServerCallContext(user, state, extensions, tenantId);

        // Then
        assertNotNull(context);
        assertEquals("", context.getTenantUid());
    }

    @Test
    void shouldExtendServerCallContext() {
        // Given
        Map<String, Object> state = new HashMap<>();
        state.put("key1", "value1");
        state.put("key2", 123);
        Set<String> extensions = new HashSet<>();
        extensions.add("extension1");
        String tenantId = "tenant-inheritance";

        // When
        TenantServerCallContext context = new TenantServerCallContext(user, state, extensions, tenantId);

        // Then
        assertNotNull(context);
        assertTrue(context instanceof io.a2a.server.ServerCallContext);
    }
}
