package io.a2a.server.requesthandlers;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.events.QueueManager;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStore;

import java.util.concurrent.Executor;

public class AgentRequestHandler extends DefaultRequestHandler {
    public AgentRequestHandler(AgentExecutor agentExecutor,
                               TaskStore taskStore,
                               QueueManager queueManager,
                               PushNotificationConfigStore pushConfigStore,
                               PushNotificationSender pushSender,
                               Executor executor,
                               A2AConfigProvider a2aConfigProvider) {
        super(agentExecutor, taskStore, queueManager, pushConfigStore, pushSender, executor);

        super.configProvider = a2aConfigProvider;
        super.initConfig();
    }
}
