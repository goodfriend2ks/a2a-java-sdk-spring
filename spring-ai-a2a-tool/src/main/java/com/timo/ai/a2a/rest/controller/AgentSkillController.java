package com.timo.ai.a2a.rest.controller;

import com.timo.ai.a2a.AgentSkillMethods;
import com.timo.ai.a2a.rest.AgentSkillResponse;
import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.service.AgentSkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * REST controller for agent skills.
 *
 * @author Timo
 * @since 0.1.0
 */
@RestController
@RequestMapping("/agent-skills")
public class AgentSkillController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentSkillController.class);

    private final AgentSkillService agentSkillService;
    private final CallContextFactory callContextFactory;

    public AgentSkillController(
            AgentSkillService agentSkillService,
            CallContextFactory callContextFactory
    ) {
        this.agentSkillService = agentSkillService;
        this.callContextFactory = callContextFactory;
    }

    /**
     * Returns agent's skills.
     */
    @GetMapping(
            path = {"", "/"},
            consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AgentSkillResponse>> getAgentSkills(
            ServerWebExchange exchange,
            Authentication authentication
    ) {
        var context = this.callContextFactory.build(
                exchange, authentication, AgentSkillMethods.LIST_AGENT_SKILLS_METHOD
        );
        if (context == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        var agentSkills = agentSkillService.getAgentSkills();

        return ResponseEntity.ok(agentSkills);
    }

    /**
     * Execute agent skill's action.
     */
    @PostMapping(
            path = {"/{skillId}/actions/{actionName}/execute"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<String> executeAgentSkillAction(
            ServerWebExchange exchange,
            Authentication authentication,
            @PathVariable String skillId,
            @PathVariable String actionName,
            @RequestBody String parameters
    ) {
        var context = this.callContextFactory.build(
                exchange, authentication, AgentSkillMethods.EXECUTE_AGENT_SKILL_ACTION_METHOD
        );
        if (context == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            var result = agentSkillService.executeSkillAction(skillId, actionName, parameters);
            return ResponseEntity.ok(result);
        } catch (AgentSkillService.AgentActionNotFoundException ex) {
            LOGGER.warn("Agent skill ({})'s action {} not found", skillId, actionName);
            return  ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        } catch (Exception ex) {
            LOGGER.error("Unexpected error executing agent skill ({})'s action: {}", skillId, actionName, ex);
            return ResponseEntity.internalServerError()
                    .body(ex.getMessage());
        }
    }


}
