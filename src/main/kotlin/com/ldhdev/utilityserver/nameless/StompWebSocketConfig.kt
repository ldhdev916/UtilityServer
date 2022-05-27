package com.ldhdev.utilityserver.nameless

import com.ldhdev.namelessstd.Prefix
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@EnableWebSocketMessageBroker
@Configuration
class StompWebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/nameless/stomp")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes(Prefix.Server.prefix)
        registry.enableSimpleBroker(Prefix.Client.prefix)
    }
}