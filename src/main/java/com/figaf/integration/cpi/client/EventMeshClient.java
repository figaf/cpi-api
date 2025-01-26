package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.event_mesh.QueueMetadata;
import com.figaf.integration.cpi.entity.event_mesh.SubscriptionMetadata;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;

import java.util.List;

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
                    TypeReference<List<QueueMetadata>> messageQueueDtoRef = new TypeReference<>() {};
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
                    TypeReference<List<SubscriptionMetadata>> subscriptionMetadataDtoRef = new TypeReference<>() {};
                    return ObjectMapperFactory.getJsonObjectMapper().readValue(subscriptionMetadataRawResponse, subscriptionMetadataDtoRef);
                },
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching queues subscriptions: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
