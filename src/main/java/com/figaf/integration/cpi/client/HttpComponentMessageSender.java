package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.support.OAuthTokenInterceptor;
import com.figaf.integration.common.client.support.parser.CloudFoundryOAuthTokenParser;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.OAuthTokenRequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.singleton;

/**
 * @author Nesterov Ilya
 */
@Slf4j
public class HttpComponentMessageSender extends MessageSender {

    public HttpComponentMessageSender(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    protected ResponseEntity<String> sendMessageWithBasicAuthentication(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity
    ) {
        RestTemplate restTemplate = httpClientsFactory.createRestTemplate(
            new BasicAuthenticationInterceptor(connectionProperties.getUsername(), connectionProperties.getPassword())
        );
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        return restTemplate.exchange(uriBuilder.toUriString(), httpMethod, requestEntity, String.class);
    }

    protected ResponseEntity<String> sendMessageWithOAuth(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        String oauthUrl,
        String restTemplateWrapperKey
    ) {
        RestTemplate restTemplate = restTemplateWrapperHolder.getOrCreateRestTemplateWrapperSingletonWithInterceptors(
            restTemplateWrapperKey,
            singleton(new OAuthTokenInterceptor(
                new OAuthTokenRequestContext(connectionProperties.getUsername(), connectionProperties.getPassword(), oauthUrl),
                new CloudFoundryOAuthTokenParser(),
                httpClientsFactory
            ))
        ).getRestTemplate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        return restTemplate.exchange(uriBuilder.toUriString(), httpMethod, requestEntity, String.class);
    }
}
