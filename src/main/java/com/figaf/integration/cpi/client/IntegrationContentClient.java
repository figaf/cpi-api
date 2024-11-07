package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;
import com.figaf.integration.cpi.entity.runtime_artifacts.RuntimeArtifactIdentifier;
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
     * Retrieves an {@link IntegrationContent} based on the provided {@link RequestContext} and {@link RuntimeArtifactIdentifier}.
     * This method resolves the appropriate identifier using {@link RuntimeArtifactIdentifier#getIdentificationParameter(String)}.
     * If no valid identifier can be resolved, it throws a {@link RuntimeException}.
     *
     * @param requestContext            the context of the request containing runtime location information
     * @param runtimeArtifactIdentifier encapsulates the technical name and the runtime artifact ID for resolution
     * @return the retrieved {@link IntegrationContent} object
     * @throws RuntimeException if no valid identifier can be resolved
     */
    public IntegrationContent getIntegrationRuntimeArtifact(
        RequestContext requestContext,
        RuntimeArtifactIdentifier runtimeArtifactIdentifier
    ) {
        String identificationParameter = runtimeArtifactIdentifier.getIdentificationParameter(requestContext.getRuntimeLocationId())
            .orElseThrow(() -> new RuntimeException(String.format("couldn't find appropriate identifier for %s", runtimeArtifactIdentifier)));
        return this.withRuntime(requestContext.getRuntimeLocationId()).getIntegrationRuntimeArtifact(
            requestContext,
            identificationParameter
        );
    }

    /**
     * Retrieves an {@link IntegrationContent} with additional error information based on the provided {@link RequestContext} and {@link RuntimeArtifactIdentifier}.
     * Similar to {@link #getIntegrationRuntimeArtifact(RequestContext, RuntimeArtifactIdentifier)}, this method handles identifier resolution and error handling explicitly,
     * with additional mechanisms to include error context in the returned {@link IntegrationContent}.
     *
     * @param requestContext            the context of the request containing runtime location information
     * @param runtimeArtifactIdentifier encapsulates the technical name and the runtime artifact ID for resolution
     * @return the retrieved {@link IntegrationContent} object, with additional error information if applicable
     * @throws RuntimeException if no valid identifier can be resolved
     */
    public IntegrationContent getIntegrationRuntimeArtifactWithErrorInformation(
        RequestContext requestContext,
        RuntimeArtifactIdentifier runtimeArtifactIdentifier
    ) {
        String identificationParameter = runtimeArtifactIdentifier.getIdentificationParameter(requestContext.getRuntimeLocationId())
            .orElseThrow(() -> new RuntimeException(String.format("couldn't find appropriate identifier for %s", runtimeArtifactIdentifier)));
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

    public void undeployIntegrationRuntimeArtifact(RequestContext requestContext, RuntimeArtifactIdentifier runtimeArtifactIdentifier) {
        String identificationParameter = runtimeArtifactIdentifier.getIdentificationParameter(requestContext.getRuntimeLocationId())
            .orElseThrow(() -> new RuntimeException(String.format("couldn't find appropriate identifier for %s", runtimeArtifactIdentifier)));
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
