package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.event_mesh.QueueMetadata;
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
                runtimeLocationsResponseRaw -> {
                    TypeReference<List<QueueMetadata>> messageQueueDtoRef = new TypeReference<>() {};
                    return ObjectMapperFactory.getJsonObjectMapper().readValue(runtimeLocationsResponseRaw, messageQueueDtoRef);
                },
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching error queues: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
