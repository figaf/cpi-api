package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.RestTemplateWrapperHolder;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.factory.RestTemplateWrapperFactory;
import com.figaf.integration.cpi.entity.message_sender.MessageSendingAdditionalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public abstract class MessageSender {

    protected final HttpClientsFactory httpClientsFactory;
    protected final RestTemplateWrapperHolder restTemplateWrapperHolder;

    protected MessageSender(HttpClientsFactory httpClientsFactory) {
        this.httpClientsFactory = httpClientsFactory;
        this.restTemplateWrapperHolder = new RestTemplateWrapperHolder(new RestTemplateWrapperFactory(httpClientsFactory));
    }

    public ResponseEntity<String> sendMessage(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        log.debug("#sendMessage(ConnectionProperties testSystemProperties, String url, HttpMethod httpMethod, " +
            "HttpEntity<byte[]> requestEntity, MessageSendingAdditionalProperties messageSendingAdditionalProperties): " +
            "{}, {}, {}, {}, {}", connectionProperties, url, httpMethod, requestEntity, messageSendingAdditionalProperties);
        switch (messageSendingAdditionalProperties.getAuthenticationType()) {
            case BASIC:
                return sendMessageWithBasicAuthentication(
                    connectionProperties,
                    url,
                    httpMethod,
                    requestEntity,
                    messageSendingAdditionalProperties
                );
            case OAUTH:
                return sendMessageWithOAuth(
                    connectionProperties,
                    url,
                    httpMethod,
                    requestEntity,
                    messageSendingAdditionalProperties
                );
            default:
                throw new IllegalArgumentException("Unexpected authentication type " + messageSendingAdditionalProperties.getAuthenticationType());
        }
    }

    protected abstract ResponseEntity<String> sendMessageWithBasicAuthentication(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    );

    protected abstract ResponseEntity<String> sendMessageWithOAuth(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    );

}
