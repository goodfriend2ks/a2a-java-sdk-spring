package io.github.timo.a2a.server.grpc;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;
import org.jspecify.annotations.NullMarked;

public class GrpcTransportMetadata implements TransportMetadata {
    @Override
    @NullMarked
    public String getTransportProtocol() {
        return TransportProtocol.GRPC.asString();
    }
}
