package com.timo.ai.a2a.server.grpc;

import io.a2a.spec.TransportProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcTransportMetadataTest {

    @Test
    void shouldReturnGrpcProtocol() {
        // Given
        GrpcTransportMetadata metadata = new GrpcTransportMetadata();

        // When
        String protocol = metadata.getTransportProtocol();

        // Then
        assertNotNull(protocol);
        assertEquals(TransportProtocol.GRPC.asString(), protocol);
    }

    @Test
    void shouldBeConsistentAcrossMultipleCalls() {
        // Given
        GrpcTransportMetadata metadata = new GrpcTransportMetadata();

        // When
        String protocol1 = metadata.getTransportProtocol();
        String protocol2 = metadata.getTransportProtocol();

        // Then
        assertEquals(protocol1, protocol2);
    }

    @Test
    void shouldImplementTransportMetadataInterface() {
        // Given
        GrpcTransportMetadata metadata = new GrpcTransportMetadata();

        // Then
        assertTrue(metadata instanceof io.a2a.server.TransportMetadata);
    }

    @Test
    void shouldReturnNonNullProtocol() {
        // Given
        GrpcTransportMetadata metadata = new GrpcTransportMetadata();

        // When
        String protocol = metadata.getTransportProtocol();

        // Then
        assertNotNull(protocol);
        assertFalse(protocol.isEmpty());
    }
}
