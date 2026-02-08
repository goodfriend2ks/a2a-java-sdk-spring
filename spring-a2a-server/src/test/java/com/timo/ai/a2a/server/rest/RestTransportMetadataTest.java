package com.timo.ai.a2a.server.rest;

import io.a2a.spec.TransportProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestTransportMetadataTest {

    @Test
    void shouldReturnHttpJsonProtocol() {
        // Given
        RestTransportMetadata metadata = new RestTransportMetadata();

        // When
        String protocol = metadata.getTransportProtocol();

        // Then
        assertNotNull(protocol);
        assertEquals(TransportProtocol.HTTP_JSON.asString(), protocol);
    }

    @Test
    void shouldBeConsistentAcrossMultipleCalls() {
        // Given
        RestTransportMetadata metadata = new RestTransportMetadata();

        // When
        String protocol1 = metadata.getTransportProtocol();
        String protocol2 = metadata.getTransportProtocol();

        // Then
        assertEquals(protocol1, protocol2);
    }

    @Test
    void shouldImplementTransportMetadataInterface() {
        // Given
        RestTransportMetadata metadata = new RestTransportMetadata();

        // Then
        assertTrue(metadata instanceof io.a2a.server.TransportMetadata);
    }
}
