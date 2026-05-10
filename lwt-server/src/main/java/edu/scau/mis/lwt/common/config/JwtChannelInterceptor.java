package edu.scau.mis.lwt.common.config;

import edu.scau.mis.lwt.common.utils.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Long userId = JwtUtils.getUserId(token);
                    accessor.setUser((Principal) () -> String.valueOf(userId));
                    return message;
                } catch (Exception ignored) {
                }
            }

            StompHeaderAccessor error = StompHeaderAccessor.create(StompCommand.ERROR);
            error.setMessage("JWT authentication failed");
            return MessageBuilder.createMessage(new byte[0], error.getMessageHeaders());
        }

        return message;
    }
}
