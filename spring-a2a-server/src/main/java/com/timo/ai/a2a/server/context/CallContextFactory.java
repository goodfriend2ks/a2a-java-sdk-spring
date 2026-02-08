package com.timo.ai.a2a.server.context;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

@FunctionalInterface
public interface CallContextFactory {
    /**
     * Build the {@link io.a2a.server.ServerCallContext} based on request context
     *
     * @param exchange          the current request
     * @param authentication    the request security authentication
     * @param jsonRpcMethodName the A2A method, see [io.a2a.spec.A2AMethods]
     * @return the server call context for current tenant, {@link TenantServerCallContext}
     *
     */
    TenantServerCallContext build(
            @NonNull ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @Nullable String jsonRpcMethodName
    );
}
