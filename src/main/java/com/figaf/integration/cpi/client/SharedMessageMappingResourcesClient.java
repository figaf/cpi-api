package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class SharedMessageMappingResourcesClient extends ArtifactResourcesClient {

    public SharedMessageMappingResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public ArtifactResources getSharedMessageMappingResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalMessageMappingId
    ) {
        log.debug("#getSharedMessageMappingResources(RequestContext requestContext, String externalPackageId, String externalMessageMappingId): " +
            "{}, {}, {}", requestContext, externalPackageId, externalMessageMappingId);
        return getArtifactResources(requestContext, externalPackageId, externalMessageMappingId, "messagemappings");
    }

}
