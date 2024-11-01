package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ilya Nesterov
 */
@Slf4j
public class CpiIntegrationFlowExternalConfigurationsClient extends CpiBaseClient {

    public CpiIntegrationFlowExternalConfigurationsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
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
            throw new ClientIntegrationException("Error occurred while fetching integration design artifacts configurations: " + ex.getMessage(), ex);
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

    @Override
    protected Logger getLogger() {
        return log;
    }
}
