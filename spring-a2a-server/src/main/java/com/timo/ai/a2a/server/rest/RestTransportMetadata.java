package com.timo.ai.a2a.server.rest;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;
import org.jspecify.annotations.NullMarked;

public class RestTransportMetadata implements TransportMetadata {
    @Override
    @NullMarked
    public String getTransportProtocol() {
        return TransportProtocol.HTTP_JSON.asString();
    }
}
