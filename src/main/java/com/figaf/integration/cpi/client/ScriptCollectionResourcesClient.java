package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class ScriptCollectionResourcesClient extends ArtifactResourcesClient {

    public ScriptCollectionResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public ArtifactResources getScriptCollectionResources(
        RequestContext requestContext,
        String externalPackageId,
        String externalScriptCollectionId
    ) {
        log.debug("#getScriptCollectionResources(RequestContext requestContext, String externalPackageId, String externalScriptCollectionId): " +
            "{}, {}, {}", requestContext, externalPackageId, externalScriptCollectionId);
        return getArtifactResources(requestContext, externalPackageId, externalScriptCollectionId, "scriptcollections");
    }

}
