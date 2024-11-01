package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Ilya Nesterov
 */
@Slf4j
public class IntegrationContentClient {

    private final IntegrationContentPublicApiClient defaultRuntimeClient;

    private final IntegrationContentWebApiClient edgeRuntimeClient;

    public IntegrationContentClient(HttpClientsFactory httpClientsFactory) {
        this.defaultRuntimeClient = new IntegrationContentPublicApiClient(httpClientsFactory);
        this.edgeRuntimeClient = new IntegrationContentWebApiClient(httpClientsFactory);
    }

    public IntegrationContentClient(
        IntegrationContentPublicApiClient defaultRuntimeClient,
        IntegrationContentWebApiClient edgeRuntimeClient
    ) {
        this.defaultRuntimeClient = defaultRuntimeClient;
        this.edgeRuntimeClient = edgeRuntimeClient;
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        return this.withRuntime(requestContext.getRuntimeLocationId()).getAllIntegrationRuntimeArtifacts(requestContext);
    }

    /**
     * @param identificationParameter - if requestContext.getRuntimeLocationId is not defined it should be technical name,
     * otherwise - runtime artifact id
     */
    public IntegrationContent getIntegrationRuntimeArtifact(
        RequestContext requestContext,
        String identificationParameter
    ) {
        return this.withRuntime(requestContext.getRuntimeLocationId()).getIntegrationRuntimeArtifact(
            requestContext,
            identificationParameter
        );
    }

    /**
     * @param identificationParameter - if requestContext.getRuntimeLocationId is not defined it should be technical name,
     * otherwise - runtime artifact id
     */
    public IntegrationContent getIntegrationRuntimeArtifactWithErrorInformation(
        RequestContext requestContext,
        String identificationParameter
    ) {
        return this.withRuntime(requestContext.getRuntimeLocationId()).getIntegrationRuntimeArtifactWithErrorInformation(
            requestContext,
            identificationParameter
        );
    }

    public IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(
        RequestContext requestContext,
        IntegrationContent integrationContent
    ) {
        return this.withRuntime(requestContext.getRuntimeLocationId()).getIntegrationRuntimeArtifactErrorInformation(requestContext, integrationContent);
    }

    /**
     * @param identificationParameter - if requestContext.getRuntimeLocationId is not defined it should be technical name,
     * otherwise - runtime artifact id
     */
    public void undeployIntegrationRuntimeArtifact(RequestContext requestContext, String identificationParameter) {
        this.withRuntime(requestContext.getRuntimeLocationId()).undeployIntegrationRuntimeArtifact(requestContext, identificationParameter);
    }

    private IntegrationContentAbstractClient withRuntime(String runtimeLocationId) {
        if (StringUtils.isNotBlank(runtimeLocationId)) {
            return edgeRuntimeClient;
        } else {
            return defaultRuntimeClient;
        }
    }
}
