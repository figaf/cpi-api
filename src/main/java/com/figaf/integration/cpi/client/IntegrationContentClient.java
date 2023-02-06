package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationContentClient extends CpiBaseClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntegrationContentClient(
        HttpClientsFactory httpClientsFactory
    ) {
        super(httpClientsFactory);
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        log.debug("#getAllIntegrationRuntimeArtifacts(RequestContext requestContext): {}", requestContext);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                "/api/v1/IntegrationRuntimeArtifacts?$format=json",
                (body) -> {
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
            log.error("Error occurred while fetching integration runtime artifacts " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration runtime artifacts: " + ex.getMessage(), ex);
        }
    }

    public IntegrationContent getIntegrationRuntimeArtifactByName(RequestContext requestContext, String name) {
        log.debug("#getIntegrationRuntimeArtifactByName(RequestContext requestContext, String name): {}, {}", requestContext, name);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                String.format("/api/v1/IntegrationRuntimeArtifacts('%s')?$format=json", name),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject integrationContentEntry = responseModel.getJSONObject("d");
                    IntegrationContent integrationContent = fillIntegrationContent(integrationContentEntry);
                    return integrationContent;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching integration runtime artifact " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration runtime artifact: " + ex.getMessage(), ex);
        }
    }

    public IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(RequestContext requestContext, IntegrationContent integrationContent) {
        log.debug("#getIntegrationRuntimeArtifactErrorInformation(RequestContext requestContext, IntegrationContent integrationContent):, {}, {}", requestContext, integrationContent);
        try {
            return executeGetPublicApiAndReturnResponseEntity(
                requestContext,
                String.format("/api/v1/IntegrationRuntimeArtifacts('%s')/ErrorInformation/$value", integrationContent.getId()),
                (responseEntity) -> {
                    switch (responseEntity.getStatusCode().value()) {
                        case 200: {
                            IntegrationContentErrorInformation integrationContentErrorInformation = objectMapper.readValue(responseEntity.getBody(), IntegrationContentErrorInformation.class);
                            return integrationContentErrorInformation;
                        }
                        case 204: {
                            return new IntegrationContentErrorInformation();
                        }
                        default: {
                            throw new RuntimeException("Couldn't error information about runtime artifact GET request:\n" + responseEntity.getBody());
                        }
                    }
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching error information about runtime artifact " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching error information about runtime artifact: " + ex.getMessage(), ex);
        }

    }

    //This API is not working for Draft versions
    public List<CpiExternalConfiguration> getCpiExternalConfigurations(RequestContext requestContext, String iFlowName) {
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/Configurations?$format=json", iFlowName),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");
                    List<CpiExternalConfiguration> cpiExternalConfigurationList = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject jsonObject = results.getJSONObject(ind);
                        CpiExternalConfiguration cpiExternalConfiguration = new CpiExternalConfiguration();
                        cpiExternalConfiguration.setParameterKey(jsonObject.getString("ParameterKey"));
                        cpiExternalConfiguration.setParameterValue(jsonObject.getString("ParameterValue"));
                        cpiExternalConfiguration.setDataType(jsonObject.getString("DataType"));
                        cpiExternalConfigurationList.add(cpiExternalConfiguration);
                    }
                    return cpiExternalConfigurationList;
                }
            );

        } catch (Exception ex) {
            log.error("Error occurred while fetching integration design artifacts configurations " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration design artifacts configurations: " + ex.getMessage(), ex);
        }

    }

    public int uploadCpiExternalConfiguration(RequestContext requestContext, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations) {
        log.debug("#uploadCpiExternalConfiguration(RequestContext requestContext, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations): {}, {}, {}", requestContext, iFlowName, cpiExternalConfigurations);

        AtomicInteger numberOfSuccessfullyProcessedConfigurations = new AtomicInteger();
        for (CpiExternalConfiguration cpiExternalConfiguration : cpiExternalConfigurations) {
            try {

                JSONObject requestBody = new JSONObject()
                    .put("ParameterValue", cpiExternalConfiguration.getParameterValue());

                executeMethodPublicApi(
                    requestContext,
                    String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/$links/Configurations('%s')", iFlowName, cpiExternalConfiguration.getParameterKey()),
                    requestBody.toString(),
                    HttpMethod.PUT,
                    (responseEntity) -> {
                        switch (responseEntity.getStatusCode().value()) {
                            case 200:
                            case 201:
                            case 202: {
                                log.debug("CpiExternalConfiguration {} was applied: {}", cpiExternalConfiguration, responseEntity.getBody());
                                numberOfSuccessfullyProcessedConfigurations.getAndIncrement();
                                break;
                            }
                            default: {
                                throw new ClientIntegrationException(String.format(
                                    "Couldn't apply CpiExternalConfiguration %s: Code: %d, Message: %s",
                                    cpiExternalConfiguration.toString(),
                                    responseEntity.getStatusCode().value(),
                                    responseEntity.getBody())
                                );
                            }
                        }
                        return null;
                    }
                );
            } catch (Exception ex) {
                log.error("Error occurred while applying CpiExternalConfiguration: " + ex.getMessage(), ex);
            }
        }

        return numberOfSuccessfullyProcessedConfigurations.get();

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
