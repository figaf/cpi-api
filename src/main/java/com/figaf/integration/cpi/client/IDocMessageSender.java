package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.support.OAuthTokenInterceptor;
import com.figaf.integration.common.client.support.parser.CloudFoundryOAuthTokenParser;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.OAuthTokenRequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_sender.MessageSendingAdditionalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singleton;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class IDocMessageSender extends MessageSender {

    public IDocMessageSender(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    protected ResponseEntity<String> sendMessageWithBasicAuthentication(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        RestTemplate restTemplate = httpClientsFactory.createRestTemplate(
            new BasicAuthenticationInterceptor(connectionProperties.getUsername(), connectionProperties.getPassword())
        );
        return restTemplate.exchange(url, httpMethod, requestEntity, String.class);
    }

    protected ResponseEntity<String> sendMessageWithOAuth(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        RestTemplate restTemplate = restTemplateWrapperHolder.getOrCreateRestTemplateWrapperSingletonWithInterceptors(
            messageSendingAdditionalProperties.getRestTemplateWrapperKey(),
            singleton(new OAuthTokenInterceptor(
                new OAuthTokenRequestContext(
                    connectionProperties.getUsername(),
                    connectionProperties.getPassword(),
                    messageSendingAdditionalProperties.getOauthUrl()
                ),
                new CloudFoundryOAuthTokenParser(),
                httpClientsFactory
            ))
        ).getRestTemplate();
        return restTemplate.exchange(url, httpMethod, requestEntity, String.class);
    }
}
