package com.ldhdev.utilityserver.nameless

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.*
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.util.MimeTypeUtils
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@EnableWebSocketMessageBroker
@Configuration
class StompWebSocketConfig(private val handler: NamelessHandshakeHandler) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/nameless/stomp").setHandshakeHandler(handler)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/mod")
        registry.enableSimpleBroker("/topic")
    }

    override fun configureMessageConverters(messageConverters: MutableList<MessageConverter>): Boolean {
        messageConverters.add(ByteArrayMessageConverter())
        messageConverters.add(StringMessageConverter())
        messageConverters.add(KotlinSerializationJsonMessageConverter().apply {
            contentTypeResolver = DefaultContentTypeResolver().apply {
                defaultMimeType = MimeTypeUtils.APPLICATION_JSON
            }
        })
        return super.configureMessageConverters(messageConverters)
    }
}