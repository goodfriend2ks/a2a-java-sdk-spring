package io.github.timo.a2a.server.card;

import io.github.timo.a2a.server.rest.ResponseUtils;
import io.a2a.jsonrpc.common.json.JsonUtil;
import io.a2a.spec.A2AError;
import io.a2a.spec.AgentCard;
import io.a2a.spec.InternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for A2A agent card metadata.
 *
 * @author Timo
 * @since 0.1.0
 */
@RestController
public class AgentCardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCardController.class);

    private static final String AGENT_CARD_WELL_KNOWN_PATH = "/.well-known/agent-card.json";
    private static final String ALTERNATIVE_AGENT_CARD_PATH = "/card";

    private final AgentCard agentCard;

    /**
     * Constructor with automatically Spring binding beans
     * */
    public AgentCardController(AgentCard agentCard) {
        this.agentCard = agentCard;
    }

    /**
     * Returns agent card metadata.
     * <p>
     * Note: Some A2A implementations may use alternative endpoint `/card` for getting the agent card
     *
     * @return the agent card in JSON format
     */
    @GetMapping(
            path = {
                    AGENT_CARD_WELL_KNOWN_PATH,
                    ALTERNATIVE_AGENT_CARD_PATH
            },
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAgentCard() {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(agentCard));
        } catch (A2AError ex) {
            LOGGER.error("Error getting agent card metadata", ex);
            return ResponseUtils.toResponseEntity(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error getting agent card metadata", ex);
            var error = new InternalError("Internal error: " + ex.getMessage());
            return ResponseUtils.toResponseEntity(error);
        }
    }
}
