package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.event_mesh.CreateQueueDto;
import com.figaf.integration.cpi.entity.event_mesh.QueueMetadata;
import com.figaf.integration.cpi.entity.event_mesh.SubscriptionMetadata;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.lang.String.format;

@Slf4j
public class EventMeshClient extends CpiBaseClient {

    public EventMeshClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<QueueMetadata> getAllQueuesMetadata(RequestContext requestContext) {
        log.debug("#getAllQueuesMetadata: requestContext={}", requestContext);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept", "application/json");
            return executeGet(
                requestContext,
                httpHeaders,
                API_QUEUES,
                queuesMetadataResponseRaw -> {
                    TypeReference<List<QueueMetadata>> messageQueueDtoRef = new TypeReference<>() {
                    };
                    return ObjectMapperFactory.getJsonObjectMapper().readValue(queuesMetadataResponseRaw, messageQueueDtoRef);
                },
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching queues: " + ex.getMessage(), ex);
        }
    }

    public List<SubscriptionMetadata> getSubscriptionMetadata(String queueName, RequestContext requestContext) {
        log.debug("#getSubscriptionMetadata: queueName={}, requestContext={}", queueName, requestContext);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept", "application/json");
            return executeGet(
                requestContext,
                httpHeaders,
                String.format(API_SUBSCRIPTIONS, queueName),
                subscriptionMetadataRawResponse -> {
                    TypeReference<List<SubscriptionMetadata>> subscriptionMetadataDtoRef = new TypeReference<>() {
                    };
                    return ObjectMapperFactory.getJsonObjectMapper().readValue(subscriptionMetadataRawResponse, subscriptionMetadataDtoRef);
                },
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching queues subscriptions: " + ex.getMessage(), ex);
        }
    }

    public QueueMetadata createQueue(RequestContext requestContext, String queueName) {
        log.debug("#createQueue(RequestContext requestContext, String queueName): {}, {}", requestContext, queueName);
        String queueNameEncoded = URLEncoder.encode(queueName, StandardCharsets.UTF_8);
        String path = String.format(API_QUEUES_CREATE, queueNameEncoded);
        try {
            return executeMethod(
                requestContext,
                "/api/1.0/user",
                path,
                (url, token, restTemplateWrapper) -> createQueue(url, token, restTemplateWrapper.getRestTemplate())
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while calling while creating queue: " + ex.getMessage(), ex);
        }
    }

    public SubscriptionMetadata createSubscription(RequestContext requestContext, String queueName, String subscription) {
        log.debug("#createSubscriptions(RequestContext requestContext, String queueName, String subscription): {}, {}, {}", requestContext, queueName, subscription);
        String path = String.format(
            API_SUBSCRIPTIONS_CREATE,
            queueName,
            URLEncoder.encode(subscription, StandardCharsets.UTF_8)
        );
        try {
            return executeMethod(
                requestContext,
                "/api/1.0/user",
                path,
                (url, token, restTemplateWrapper) -> createSubscription(url, token, restTemplateWrapper.getRestTemplate())
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while calling while creating queue: " + ex.getMessage(), ex);
        }
    }

    private QueueMetadata createQueue(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateQueueDto> requestEntity = new HttpEntity<>(defaultCreateQueueDto(), httpHeaders);
            URI uri = UriComponentsBuilder
                .fromUriString(url)
                .build(true)
                .toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

            if (HttpStatus.CREATED.equals(responseEntity.getStatusCode()) || HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                return ObjectMapperFactory.getJsonObjectMapper().readValue(responseEntity.getBody(), QueueMetadata.class);
            } else {
                throw new ClientIntegrationException(format(
                    "Couldn't create queue. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while creating queue: " + ex.getMessage(), ex);
        }
    }

    private SubscriptionMetadata createSubscription(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateQueueDto> requestEntity = new HttpEntity<>(httpHeaders);
            URI uri = UriComponentsBuilder
                .fromUriString(url)
                .build(true)
                .toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

            if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
                return ObjectMapperFactory.getJsonObjectMapper().readValue(responseEntity.getBody(), SubscriptionMetadata.class);
            } else {
                throw new ClientIntegrationException(format(
                    "Couldn't create subscription. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while creating subscription: " + ex.getMessage(), ex);
        }
    }

    private static CreateQueueDto defaultCreateQueueDto() {
        return CreateQueueDto
            .builder()
            .accessType("NON_EXCLUSIVE")
            .egressDisabled(false)
            .ingressDisabled(false)
            .maxDeliveredUnackedMessagesPerFlow(10000)
            .maxMessageSizeInBytes(10000000L)
            .maxMessageTimeToLiveInSeconds(604800L)
            .maxQueueSizeInBytes(1572864000L)
            .maxRedeliveryCount(0)
            .respectTimeToLiveInSeconds(true)
            .build();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
