package com.timo.ai.a2a.server.jsonrpc;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;
import org.jspecify.annotations.NullMarked;

public class JSONRPCTransportMetadata implements TransportMetadata {
    @Override
    @NullMarked
    public String getTransportProtocol() {
        return TransportProtocol.JSONRPC.asString();
    }
}
