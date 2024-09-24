package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType;
import com.figaf.integration.cpi.response_parser.ArtifactResourcesParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class ArtifactResourcesClient extends CpiBaseClient {

    protected static final String API_ARTIFACT_RESOURCES = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/resource?artifactType=%s";

    public ArtifactResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public ArtifactResources getArtifactResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalIFlowId,
        CpiArtifactType cpiArtifactType
    ) {
        return getArtifactResources(
            requestContext,
            externalPackageId,
            externalIFlowId,
            cpiArtifactType.getQueryTitle()
        );
    }

    protected ArtifactResources getArtifactResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalIFlowId,
        String queryTitle
    ) {
        log.debug("#getArtifactResources(RequestContext requestContext, String externalPackageId, String externalIFlowId, String queryTitle): " +
            "{}, {}, {}, {}", requestContext, externalPackageId, externalIFlowId, queryTitle);
        String path = String.format(API_ARTIFACT_RESOURCES, externalPackageId, externalIFlowId, externalIFlowId, queryTitle);
        return executeGet(requestContext, path, ArtifactResourcesParser::buildArtifactResources);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
