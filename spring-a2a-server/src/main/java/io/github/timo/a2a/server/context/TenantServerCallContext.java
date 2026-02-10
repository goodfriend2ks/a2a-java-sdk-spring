package io.github.timo.a2a.server.context;

import io.a2a.server.ServerCallContext;
import io.a2a.server.auth.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TenantServerCallContext extends ServerCallContext {
    private final String tenantId;

    public TenantServerCallContext(
            @NonNull  User user,
            @NonNull  Map<String, Object> state,
            @NonNull  Set<String> requestedExtensions,
            @NonNull  String tenantUid
    ) {
        super(user, new HashMap<>(state), requestedExtensions);
        this.tenantId = tenantUid;
    }

    public TenantServerCallContext(
            @NonNull User user,
            @NonNull Map<String, Object> state,
            @NonNull Set<String> requestedExtensions,
            @Nullable String requestedProtocolVersion,
            @NonNull String tenantUid
    ) {
        super(user, new HashMap<>(state), requestedExtensions, requestedProtocolVersion);
        this.tenantId = tenantUid;
    }

    public String getTenantUid() {
        return tenantId;
    }
}
