package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationContentPublicApiClient extends IntegrationContentAbstractClient {

    private static final String INTEGRATION_RUNTIME_ARTIFACTS_API = "/api/v1/IntegrationRuntimeArtifacts";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntegrationContentPublicApiClient(
        HttpClientsFactory httpClientsFactory
    ) {
        super(httpClientsFactory);
    }

    @Override
    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        log.debug("#getAllIntegrationRuntimeArtifacts: requestContext={}", requestContext);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                format("%s?$format=json", INTEGRATION_RUNTIME_ARTIFACTS_API),
                body -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<IntegrationContent> artifacts = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject integrationContentEntry = results.getJSONObject(ind);
                        IntegrationContent integrationContent = fillIntegrationContent(integrationContentEntry);
                        artifacts.add(integrationContent);
                    }

                    return artifacts;
                }
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching integration runtime artifacts", ex);
        }
    }

    @Override
    public IntegrationContent getIntegrationRuntimeArtifact(RequestContext requestContext, String technicalName) {
        log.debug("#getIntegrationRuntimeArtifact: requestContext={}, technicalName={}", requestContext, technicalName);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                format("%s('%s')?$format=json", INTEGRATION_RUNTIME_ARTIFACTS_API, technicalName),
                body -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject integrationContentEntry = responseModel.getJSONObject("d");
                    return fillIntegrationContent(integrationContentEntry);
                }
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException(
                "Failed to retrieve integration runtime artifact %s".formatted(technicalName),
                ex
            );
        }
    }

    @Override
    public IntegrationContent getIntegrationRuntimeArtifactWithErrorInformation(
        RequestContext requestContext,
        String technicalName
    ) {
        log.debug("#getIntegrationRuntimeArtifactWithErrorInformation: requestContext={}, technicalName={}",
            requestContext,
            technicalName
        );
        IntegrationContent integrationContent = getIntegrationRuntimeArtifact(requestContext, technicalName);
        if ("ERROR".equals(integrationContent.getStatus())) {
            integrationContent.setErrorInformation(getIntegrationRuntimeArtifactErrorInformation(requestContext, integrationContent));
        }

        return integrationContent;
    }

    @Override
    public IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(
        RequestContext requestContext,
        IntegrationContent integrationContent
    ) {
        log.debug("#getIntegrationRuntimeArtifactErrorInformation: requestContext={}, integrationContent={}",
            requestContext, integrationContent
        );
        try {
            return executeGetPublicApiAndReturnResponseEntity(
                requestContext,
                format("%s('%s')/ErrorInformation/$value", INTEGRATION_RUNTIME_ARTIFACTS_API, integrationContent.getId()),
                responseEntity ->
                    switch (responseEntity.getStatusCode().value()) {
                        case 200 -> objectMapper.readValue(responseEntity.getBody(), IntegrationContentErrorInformation.class);
                        case 204 -> new IntegrationContentErrorInformation();
                        default ->
                                throw new ClientIntegrationException("Couldn't error information about runtime artifact GET request:\n" + responseEntity.getBody());
                    }
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while fetching error information about runtime artifact: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void undeployIntegrationRuntimeArtifact(RequestContext requestContext, String artifactTechnicalName) {
        log.debug("#undeployIntegrationRuntimeArtifact: artifactTechnicalName={}, requestContext={}", artifactTechnicalName, requestContext);
        executeDeletePublicApi(
            requestContext,
            format("%s('%s')", INTEGRATION_RUNTIME_ARTIFACTS_API, artifactTechnicalName),
            Objects::nonNull
        );
    }

    private IntegrationContent fillIntegrationContent(JSONObject integrationContentEntry) throws JSONException {
        IntegrationContent integrationContent = new IntegrationContent();
        integrationContent.setId(integrationContentEntry.getString("Id"));
        integrationContent.setVersion(integrationContentEntry.getString("Version"));
        integrationContent.setName(integrationContentEntry.getString("Name"));
        integrationContent.setType(integrationContentEntry.getString("Type"));
        integrationContent.setDeployedBy(integrationContentEntry.getString("DeployedBy"));
        integrationContent.setDeployedOn(CpiApiUtils.parseDate(integrationContentEntry.getString("DeployedOn")));
        integrationContent.setStatus(integrationContentEntry.getString("Status"));
        return integrationContent;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
