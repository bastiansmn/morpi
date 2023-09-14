package fr.metamorpion.api.configuration;

import fr.metamorpion.api.model.ActionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

@Configuration
public class StompSessionHandler extends StompSessionHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(StompSessionHandler.class);

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        ActionDTO action = (ActionDTO) payload;
        System.out.println(action);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.out.println(exception.getMessage());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.out.println(exception.getMessage());
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        LOGGER.info("Connected to websocket {}", connectedHeaders);
    }
}
