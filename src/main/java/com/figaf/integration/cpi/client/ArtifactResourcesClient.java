package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;

import static com.figaf.integration.cpi.response_parser.ArtifactResourcesParser.buildIFlowResources;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public abstract class ArtifactResourcesClient extends CpiBaseClient {

    protected static final String API_ARTIFACT_RESOURCES = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/resource?artifactType=%s";

    protected final ObjectMapper jsonMapper;

    ArtifactResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        this.jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public List<ArtifactResource> getArtifactResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalIFlowId,
        String artifactType
    ) {
        log.debug("#IFlowResourcesClient(RequestContext requestContext, String externalPackageId, String externalIFlowId, String artifactType): " +
            "{}, {}, {}, {}", requestContext, externalPackageId, externalIFlowId, artifactType);
        String path = String.format(API_ARTIFACT_RESOURCES, externalPackageId, externalIFlowId, externalIFlowId, artifactType);
        return executeGet(requestContext, path, body -> buildIFlowResources(body, jsonMapper));
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
