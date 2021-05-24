package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.support.OAuthTokenInterceptor;
import com.figaf.integration.common.client.support.parser.CloudFoundryOAuthTokenParser;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.OAuthTokenRequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_sender.MessageSendingAdditionalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

/**
 * @author Nesterov Ilya
 */
@Slf4j
public class HttpComponentMessageSender extends MessageSender {

    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    private final CsrfTokenHolder csrfTokenHolder;

    public HttpComponentMessageSender(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        csrfTokenHolder = new CsrfTokenHolder();
    }

    protected ResponseEntity<String> sendMessageWithBasicAuthentication(
        ConnectionProperties connectionProperties,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        RestTemplate restTemplate = restTemplateWrapperHolder.getOrCreateRestTemplateWrapperSingletonWithInterceptors(
            messageSendingAdditionalProperties.getRestTemplateWrapperKey(),
            singleton(
                new BasicAuthenticationInterceptor(connectionProperties.getUsername(), connectionProperties.getPassword())
            )
        ).getRestTemplate();
        return sendMessage(
            restTemplate,
            url,
            httpMethod,
            requestEntity,
            messageSendingAdditionalProperties
        );
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
        return sendMessage(
            restTemplate,
            url,
            httpMethod,
            requestEntity,
            messageSendingAdditionalProperties
        );
    }

    private ResponseEntity<String> sendMessage(
        RestTemplate restTemplate,
        String url,
        HttpMethod httpMethod,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        String uri = uriBuilder.toUriString();
        HttpEntity<byte[]> actualRequestEntity = createActualRequestEntity(
            restTemplate,
            uri,
            requestEntity,
            messageSendingAdditionalProperties
        );
        try {
            return restTemplate.exchange(
                uri,
                httpMethod,
                actualRequestEntity,
                String.class
            );
        } catch (HttpClientErrorException.Forbidden ex) {
            return processForbiddenHttpClientErrorException(
                ex,
                restTemplate,
                uri,
                requestEntity,
                httpMethod,
                messageSendingAdditionalProperties,
                actualRequestEntity.getHeaders().getFirst(X_CSRF_TOKEN)
            );
        }
    }

    private HttpEntity<byte[]> createActualRequestEntity(
        RestTemplate restTemplate,
        String url,
        HttpEntity<byte[]> requestEntity,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties
    ) {
        if (messageSendingAdditionalProperties.isCsrfProtected()) {
            return createRequestEntityWithCsrfToken(
                restTemplate,
                url,
                requestEntity,
                messageSendingAdditionalProperties.getRestTemplateWrapperKey()
            );
        } else {
            return requestEntity;
        }
    }

    private ResponseEntity<String> processForbiddenHttpClientErrorException(
        HttpClientErrorException.Forbidden ex,
        RestTemplate restTemplate,
        String url,
        HttpEntity<byte[]> requestEntity,
        HttpMethod httpMethod,
        MessageSendingAdditionalProperties messageSendingAdditionalProperties,
        String oldToken
    ) {
        if (ex.getResponseHeaders() != null &&
            "required".equalsIgnoreCase(ex.getResponseHeaders().getFirst(X_CSRF_TOKEN))
        ) {
            return restTemplate.exchange(
                url,
                httpMethod,
                createRequestEntityWithNewCsrfToken(
                    restTemplate,
                    url,
                    requestEntity,
                    messageSendingAdditionalProperties.getRestTemplateWrapperKey(),
                    oldToken
                ),
                String.class
            );
        } else {
            throw ex;
        }
    }

    private HttpEntity<byte[]> createRequestEntityWithCsrfToken(
        RestTemplate restTemplate,
        String url,
        HttpEntity<byte[]> requestEntity,
        String tokenKey
    ) {
        String csrfToken = csrfTokenHolder.getCsrfToken(tokenKey, restTemplate, url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(requestEntity.getHeaders());
        httpHeaders.put(X_CSRF_TOKEN, singletonList(csrfToken));
        return new HttpEntity<>(requestEntity.getBody(), httpHeaders);
    }

    private HttpEntity<byte[]> createRequestEntityWithNewCsrfToken(
        RestTemplate restTemplate,
        String url,
        HttpEntity<byte[]> requestEntity,
        String tokenKey,
        String oldToken
    ) {
        String csrfToken = csrfTokenHolder.getAndSaveNewCsrfTokenIfNeed(tokenKey, restTemplate, url, oldToken);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(requestEntity.getHeaders());
        httpHeaders.put(X_CSRF_TOKEN, singletonList(csrfToken));
        return new HttpEntity<>(requestEntity.getBody(), httpHeaders);
    }
}
