package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;

import java.util.List;

/**
 * @author Ilya Nesterov
 */
abstract class IntegrationContentAbstractClient extends CpiBaseClient {

    protected IntegrationContentAbstractClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public abstract List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext);

    public abstract IntegrationContent getIntegrationRuntimeArtifact(RequestContext requestContext, String identificationParameter);
    public abstract IntegrationContent getIntegrationRuntimeArtifactWithErrorInformation(RequestContext requestContext, String identificationParameter);

    public abstract IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(RequestContext requestContext, IntegrationContent integrationContent);

    public abstract void undeployIntegrationRuntimeArtifact(RequestContext requestContext, String identificationParameter);

}
