package com.timo.ai.a2a.server.jsonrpc;

import io.a2a.spec.TransportProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONRPCTransportMetadataTest {

    @Test
    void shouldReturnJsonRpcProtocol() {
        // Given
        JSONRPCTransportMetadata metadata = new JSONRPCTransportMetadata();

        // When
        String protocol = metadata.getTransportProtocol();

        // Then
        assertNotNull(protocol);
        assertEquals(TransportProtocol.JSONRPC.asString(), protocol);
    }

    @Test
    void shouldBeConsistentAcrossMultipleCalls() {
        // Given
        JSONRPCTransportMetadata metadata = new JSONRPCTransportMetadata();

        // When
        String protocol1 = metadata.getTransportProtocol();
        String protocol2 = metadata.getTransportProtocol();

        // Then
        assertEquals(protocol1, protocol2);
    }

    @Test
    void shouldImplementTransportMetadataInterface() {
        // Given
        JSONRPCTransportMetadata metadata = new JSONRPCTransportMetadata();

        // Then
        assertTrue(metadata instanceof io.a2a.server.TransportMetadata);
    }

    @Test
    void shouldReturnNonNullProtocol() {
        // Given
        JSONRPCTransportMetadata metadata = new JSONRPCTransportMetadata();

        // When
        String protocol = metadata.getTransportProtocol();

        // Then
        assertNotNull(protocol);
        assertFalse(protocol.isEmpty());
    }
}
