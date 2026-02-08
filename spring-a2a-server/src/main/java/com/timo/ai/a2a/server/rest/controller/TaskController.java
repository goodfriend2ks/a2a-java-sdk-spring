package com.timo.ai.a2a.server.rest.controller;

import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.server.rest.ResponseUtils;
import com.timo.ai.a2a.server.rest.handler.AgentRestHandler;
import io.a2a.spec.A2AError;
import io.a2a.spec.A2AMethods;
import io.a2a.transport.rest.handler.RestHandler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * REST controller for A2A task operations.
 *
 * @author Timo
 * @since 0.1.0
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private static final String HISTORY_LENGTH_PARAM = "historyLength";
    private static final String CONTEXT_ID_PARAM = "contextId";
    private static final String STATUS_PARAM = "status";
    private static final String LAST_UPDATED_AFTER_PARAM = "lastUpdatedAfter";
    private static final String INCLUDE_ARTIFACTS_PARAM = "includeArtifacts";

    private static final String PAGE_SIZE_PARAM = "pageSize";
    private static final String PAGE_TOKEN_PARAM = "pageToken";

    private static final String INTERNAL_ERROR_PREFIX = "Internal error: ";

    private final AgentRestHandler agentRestHandler;
    private final CallContextFactory callContextFactory;

    public TaskController(AgentRestHandler agentRestHandler, CallContextFactory callContextFactory) {
        this.agentRestHandler = agentRestHandler;
        this.callContextFactory = callContextFactory;
    }

    /**
     * Returns task status and results.
     */
    @GetMapping(
            path = {"", "/"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getTasks(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @Nullable @RequestParam(value = HISTORY_LENGTH_PARAM, required = false) Integer historyLength,
            @Nullable @RequestParam(value = CONTEXT_ID_PARAM, required = false) String contextId,
            @Nullable @RequestParam(value = STATUS_PARAM, required = false) String status,
            @Nullable @RequestParam(value = LAST_UPDATED_AFTER_PARAM, required = false) String lastUpdatedAfter,
            @Nullable @RequestParam(value = INCLUDE_ARTIFACTS_PARAM, required = false) Boolean includeArtifacts,
            @Nullable @RequestParam(value = PAGE_TOKEN_PARAM, required = false) String pageToken,
            @Nullable @RequestParam(value = PAGE_SIZE_PARAM, required = false) Integer pageSize
    ) {
        LOGGER.info("Getting tasks");

        try {
            var context = callContextFactory.build(exchange, authentication, A2AMethods.LIST_TASK_METHOD);

            var response = agentRestHandler.listTasks(
                    contextId,
                    status,
                    pageSize,
                    pageToken,
                    historyLength,
                    lastUpdatedAfter,
                    includeArtifacts,
                    context.getTenantUid(),
                    context
            );

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error getting tasks", ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error getting tasks", ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Returns task status and results.
     */
    @GetMapping(
            path = "/{taskId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getTask(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId,
            @Nullable @RequestParam(value = HISTORY_LENGTH_PARAM, required = false) Integer historyLength
    ) {
        LOGGER.info("Getting task: {}", taskId);

        try {
            var context = callContextFactory.build(exchange, authentication, A2AMethods.GET_TASK_METHOD);

            var response = agentRestHandler.getTask(taskId, historyLength, context.getTenantUid(), context);
            LOGGER.debug("Task retrieved: {}", taskId);

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error getting task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error getting task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Cancels a running task.
     */
    @PostMapping(
            path = "/{taskId}/cancel",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> cancelTask(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId
    ) {
        LOGGER.info("Cancelling task: {}", taskId);

        try {
            var context = callContextFactory.build(exchange, authentication, A2AMethods.CANCEL_TASK_METHOD);

            var response = agentRestHandler.cancelTask(taskId, context.getTenantUid(), context);
            LOGGER.debug("Task cancelled: {}", taskId);

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error cancelling task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error cancelling task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Subscribe to task.
     */
    @PostMapping(
            path = "/{taskId}/subscribe",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<?> subscribeToTask(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId
    ) {
        LOGGER.info("Subscribe to task: {}", taskId);

        try {
            var context = callContextFactory.build(exchange, authentication, A2AMethods.SUBSCRIBE_TO_TASK_METHOD);

            var response = agentRestHandler.subscribeToTask(taskId, context.getTenantUid(), context);
            if (response instanceof RestHandler.HTTPRestStreamingResponse) {
                LOGGER.debug("Task subscribed: {}", taskId);
            }

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error subscribing task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error subscribing task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Update push notification config for task.
     */
    /**
     @PostMapping( path = "/{taskId}/pushNotificationConfigs",
     consumes = MediaType.APPLICATION_JSON_VALUE,
     produces = MediaType.TEXT_EVENT_STREAM_VALUE
     )
     public ResponseEntity<?> setTaskPushNotificationConfiguration(
     ServerWebExchange exchange,
     @Nullable Authentication authentication,
     @PathVariable String taskId
     ) {
     LOGGER.info("Update push notification config for task: {}", taskId);

     try {
     var context = callContextFactory.build(exchange, authentication, A2AMethods.SET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD);

     var response = agentRestHandler.setTaskPushNotificationConfiguration(taskId, body, context.getTenantUid(), context);
     if (response instanceof RestHandler.HTTPRestStreamingResponse) {
     LOGGER.debug("Task push notification config updated: {}", taskId);
     }

     return ResponseUtils.toResponseEntity(response);
     } catch (A2AError ex) {
     LOGGER.error("Error updating push notification config for task: " + taskId, ex);
     return ResponseUtils.toResponseEntity(ex);
     } catch (Exception ex) {
     LOGGER.error("Unexpected error updating push notification config for task: " + taskId, ex);
     return ResponseUtils.toResponseEntity(
     new io.a2a.spec.InternalError("Internal error: " + ex.getMessage())
     );
     }
     }
     */

    /**
     * Returns all task's push notification configs.
     */
    @GetMapping(
            path = "/{taskId}/pushNotificationConfigs",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getTaskPushNotificationConfigurations(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId,
            @Nullable @RequestParam(value = PAGE_TOKEN_PARAM, required = false) String pageToken,
            @Nullable @RequestParam(value = PAGE_SIZE_PARAM, required = false) Integer pageSize
    ) {
        LOGGER.info("Getting push notification configs for task: {}", taskId);

        try {
            var isPagination = pageSize != null || (pageToken != null && !pageToken.isEmpty());

            var jsonRpcMethodName = isPagination
                    ? A2AMethods.LIST_TASK_PUSH_NOTIFICATION_CONFIG_METHOD
                    : A2AMethods.GET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD;

            var context = callContextFactory.build(exchange, authentication, jsonRpcMethodName);

            var response = isPagination ? agentRestHandler.listTaskPushNotificationConfigurations(
                    taskId,
                    null == pageSize ? 0 : pageSize,
                    null == pageToken ? "" : pageToken,
                    context.getTenantUid(),
                    context
            ) : agentRestHandler.getTaskPushNotificationConfiguration(taskId, null, context.getTenantUid(), context);

            LOGGER.debug("Complete getting push notification configs for task: {}", taskId);
            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error getting push notification configs for task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error getting push notification configs for task: {}", taskId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Returns task's push notification config.
     */
    @GetMapping(
            path = "/{taskId}/pushNotificationConfigs/{configId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getTaskPushNotificationConfiguration(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId,
            @PathVariable String configId
    ) {
        LOGGER.info("Get task ({})'s push notification config: {}", taskId, configId);

        try {
            var context = callContextFactory.build(
                    exchange, authentication,
                    A2AMethods.GET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD
            );

            var response = agentRestHandler.getTaskPushNotificationConfiguration(
                    taskId, configId,
                    context.getTenantUid(), context
            );
            LOGGER.debug("Task ({})'s push notification config retrieved: {}", taskId, configId);

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error getting task ({})'s push notification config: {}", taskId, configId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error getting task ({})'s push notification config: {}", taskId, configId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }

    /**
     * Returns task's push notification config.
     */
    @DeleteMapping(
            path = "/{taskId}/pushNotificationConfigs/{configId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> deleteTaskPushNotificationConfiguration(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @PathVariable String taskId,
            @PathVariable String configId
    ) {
        LOGGER.info("Delete task ({})'s push notification config: {}", taskId, configId);

        try {
            var context = callContextFactory.build(
                    exchange, authentication,
                    A2AMethods.DELETE_TASK_PUSH_NOTIFICATION_CONFIG_METHOD
            );

            var response = agentRestHandler.deleteTaskPushNotificationConfiguration(
                    taskId, configId,
                    context.getTenantUid(), context
            );
            LOGGER.debug("Task ({})'s push notification config deleted: {}", taskId, configId);

            return ResponseUtils.toResponseEntity(response);
        } catch (A2AError ex) {
            LOGGER.error("Error deleting task ({})'s push notification config: {}", taskId, configId, ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error deleting task ({})'s push notification config: {}", taskId, configId, ex);
            return ResponseUtils.toResponseEntity(
                    new io.a2a.spec.InternalError(INTERNAL_ERROR_PREFIX + ex.getMessage())
            );
        }
    }
}
