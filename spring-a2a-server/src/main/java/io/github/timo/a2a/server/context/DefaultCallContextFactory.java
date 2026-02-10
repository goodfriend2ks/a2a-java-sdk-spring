package io.github.timo.a2a.server.context;

import io.a2a.common.A2AHeaders;
import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.server.auth.User;
import io.a2a.server.extensions.A2AExtensions;
import io.a2a.transport.jsonrpc.context.JSONRPCContextKeys;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultCallContextFactory implements CallContextFactory {
    private final String tenantUid;

    public DefaultCallContextFactory() {
        this("");
    }

    public DefaultCallContextFactory(String tenantUid) {
        this.tenantUid = tenantUid;
    }

    @Override
    public TenantServerCallContext build(
            @NonNull ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @Nullable String jsonRpcMethodName
    ) {
        var user = getUser(authentication);
        var requestHeaders = exchange.getRequest().getHeaders();

        var headers = new HashMap<String, String>(requestHeaders.size(), 1.0F);
        for (var requestHeader : requestHeaders.entrySet()) {
            headers.put(
                    requestHeader.getKey(),
                    requestHeader.getValue().isEmpty() ? "" : requestHeader.getValue().getFirst()
            );
        }

        var state = Map.<String, Object>of(
                JSONRPCContextKeys.HEADERS_KEY, headers,
                JSONRPCContextKeys.METHOD_NAME_KEY, jsonRpcMethodName == null ? "" : jsonRpcMethodName
        );

        // Extract requested protocol version from X-A2A-Version header
        var requestedVersion = requestHeaders.getFirst(A2AHeaders.X_A2A_VERSION);

        // Extract requested extensions from X-A2A-Extensions header and load A2AExtensions
        var extensionHeaderValues = requestHeaders.get(A2AHeaders.X_A2A_EXTENSIONS);
        var requestedExtensions = A2AExtensions.getRequestedExtensions(
                extensionHeaderValues == null ? Collections.emptyList() : extensionHeaderValues
        );

        return new TenantServerCallContext(
                user,
                state,
                requestedExtensions,
                requestedVersion,
                tenantUid
        );
    }

    private User getUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return UnauthenticatedUser.INSTANCE;
        }

        return new User() {
            @Override
            public boolean isAuthenticated() {
                return authentication.isAuthenticated();
            }

            @Override
            @NullMarked
            public String getUsername() {
                return authentication.getName();
            }
        };
    }
}
