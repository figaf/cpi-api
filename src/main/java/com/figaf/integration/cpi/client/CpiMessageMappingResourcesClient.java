package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiMessageMappingResourcesClient extends ArtifactResourcesClient {

    public CpiMessageMappingResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public ArtifactResources getMessageMappingResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalMessageMappingId
    ) {
        log.debug("#getMessageMappingResources(RequestContext requestContext, String externalPackageId, String externalMessageMappingId): " +
            "{}, {}, {}", requestContext, externalPackageId, externalMessageMappingId);
        return getArtifactResources(requestContext, externalPackageId, externalMessageMappingId, "messagemappings");
    }

}
