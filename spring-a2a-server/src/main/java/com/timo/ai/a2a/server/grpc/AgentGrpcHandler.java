package com.timo.ai.a2a.server.grpc;

import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.transport.grpc.handler.CallContextFactory;
import io.a2a.transport.grpc.handler.GrpcHandler;

import java.util.concurrent.Executor;

public class AgentGrpcHandler extends GrpcHandler {
    private final AgentCard agentCard;
    private final RequestHandler requestHandler;
    private final CallContextFactory callContextFactory;
    private final Executor executor;

    public AgentGrpcHandler(
            AgentCard agentCard,
            RequestHandler requestHandler,
            CallContextFactory callContextFactory,
            Executor executor
    ) {
        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
        this.callContextFactory = callContextFactory;
        this.executor = executor;
    }

    @Override
    protected RequestHandler getRequestHandler() {
        return requestHandler;
    }

    @Override
    protected AgentCard getAgentCard() {
        return agentCard;
    }

    @Override
    protected CallContextFactory getCallContextFactory() {
        return callContextFactory;
    }

    @Override
    protected Executor getExecutor() {
        return executor;
    }
}
