package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.common.entity.CloudPlatformType;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContentErrorInformation;
import com.figaf.integration.cpi.response_parser.IntegrationContentPrivateApiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationContentClient extends CpiBaseClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntegrationContentClient(
        String ssoUrl,
        HttpClientsFactory httpClientsFactory
    ) {
        super(ssoUrl, httpClientsFactory);
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        log.debug("#getAllIntegrationRuntimeArtifacts(RequestContext requestContext): {}", requestContext);
        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            String path = "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentsListCommand";
            return executeGet(requestContext, path, IntegrationContentPrivateApiParser::getAllIntegrationRuntimeArtifacts);
        } else {
            return getAllIntegrationRuntimeArtifacts(requestContext.getConnectionProperties());
        }
    }

    public IntegrationContent getIntegrationRuntimeArtifactByName(RequestContext requestContext, String name) {
        log.debug("#getIntegrationRuntimeArtifactByName(RequestContext requestContext, String name): {}, {}", requestContext, name);

        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            String path = "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentsListCommand";
            return executeGet(
                requestContext,
                path,
                body -> IntegrationContentPrivateApiParser.getIntegrationRuntimeArtifactByName(body, name)
            );
        } else {
            return getIntegrationRuntimeArtifactByName(requestContext.getConnectionProperties(), name);
        }
    }

    public List<CpiExternalConfiguration> getCpiExternalConfigurations(RequestContext requestContext, String iFlowName) {
        log.debug("#getCpiExternalConfigurations(RequestContext requestContext, String iFlowName): {}, {}", requestContext, iFlowName);
        return getCpiExternalConfigurations(requestContext.getConnectionProperties(), iFlowName);
    }

    public void uploadCpiExternalConfiguration(RequestContext requestContext, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations) {
        log.debug("#uploadCpiExternalConfiguration(RequestContext requestContext, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations): {}, {}, {}", requestContext, iFlowName, cpiExternalConfigurations);
        uploadCpiExternalConfiguration(requestContext.getConnectionProperties(), iFlowName, cpiExternalConfigurations);
    }

    public IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(RequestContext requestContext, IntegrationContent integrationContent) {
        log.debug("#getIntegrationRuntimeArtifactErrorInformation(RequestContext requestContext, IntegrationContent integrationContent):, {}, {}", requestContext, integrationContent);
        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            String path = String.format("/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentDetailCommand?artifactId=%s", integrationContent.getExternalId());
            List<String> errorMessages = executeGet(
                requestContext,
                path,
                IntegrationContentPrivateApiParser::getIntegrationRuntimeErrorInformation
            );
            IntegrationContentErrorInformation integrationContentErrorInformation = new IntegrationContentErrorInformation();
            integrationContentErrorInformation.setErrorMessage(StringUtils.join(errorMessages,", "));
            return integrationContentErrorInformation;
        } else {
            return getIntegrationContentErrorInformation(requestContext.getConnectionProperties(), integrationContent);
        }
    }


    public IntegrationContent getIntegrationRuntimeArtifactByName(ConnectionProperties connectionProperties, String name) {
        log.debug("#getIntegrationRuntimeArtifactByName(ConnectionProperties connectionProperties, String name): {}, {}", connectionProperties, name);
        try {
            URI uri = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(String.format("/api/v1/IntegrationRuntimeArtifacts('%s')", name))
                    .queryParam("$format", "json")
                    .build()
                    .encode()
                    .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONObject integrationContentEntry = responseModel.getJSONObject("d");
                    IntegrationContent integrationContent = fillIntegrationContent(integrationContentEntry);
                    return integrationContent;
                } else {
                    throw new RuntimeException("Couldn't execute integration runtime artifact GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching integration runtime artifact " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration runtime artifact: " + ex.getMessage(), ex);
        }
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(ConnectionProperties connectionProperties) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("/api/v1/IntegrationRuntimeArtifacts")
                .queryParam("$format", "json")
                .build()
                .encode()
                .toUri();
        return getAllIntegrationRuntimeArtifacts(connectionProperties, uri);
    }

    //This API is not working for Draft versions
    public List<CpiExternalConfiguration> getCpiExternalConfigurations(ConnectionProperties connectionProperties, String iFlowName) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/Configurations", iFlowName))
                .queryParam("$format", "json")
                .build()
                .encode()
                .toUri();

        try {

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
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
                } else {
                    throw new RuntimeException("Couldn't execute integration design artifacts configurations GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching integration design artifacts configurations " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration design artifacts configurations: " + ex.getMessage(), ex);
        }
    }

    public void uploadCpiExternalConfiguration(ConnectionProperties connectionProperties, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations) {
        log.debug("#uploadCpiExternalConfiguration(ConnectionProperties connectionProperties, String iFlowName, List<CpiExternalConfiguration> cpiExternalConfigurations): {}, {}, {}", connectionProperties, iFlowName, cpiExternalConfigurations);

        HttpClient client = httpClientsFactory.createHttpClient();

        String csrfToken = getCsrfToken(connectionProperties, client);

        HttpResponse uploadExternalConfigurationResponse = null;
        for (CpiExternalConfiguration cpiExternalConfiguration : cpiExternalConfigurations) {
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                        .scheme(connectionProperties.getProtocol())
                        .host(connectionProperties.getHost())
                        .path(String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/$links/Configurations('%s')", iFlowName, cpiExternalConfiguration.getParameterKey()));

                if (connectionProperties.getPort() != null) {
                    uriBuilder.port(connectionProperties.getPort());
                }
                URI uri = uriBuilder.build().toUri();

                JSONObject requestBody = new JSONObject()
                        .put("ParameterValue", cpiExternalConfiguration.getParameterValue());

                HttpPut uploadCpiExternalConfiguration = new HttpPut(uri);
                uploadCpiExternalConfiguration.setHeader(createBasicAuthHeader(connectionProperties));
                uploadCpiExternalConfiguration.setHeader(createCsrfTokenHeader(csrfToken));
                uploadCpiExternalConfiguration.setHeader("Accept", "application/json");
                uploadCpiExternalConfiguration.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

                uploadExternalConfigurationResponse = client.execute(uploadCpiExternalConfiguration);

                switch (uploadExternalConfigurationResponse.getStatusLine().getStatusCode()) {
                    case 200:
                    case 201:
                    case 202: {
                        log.debug("CpiExternalConfiguration {} was applied: {}", cpiExternalConfiguration, IOUtils.toString(uploadExternalConfigurationResponse.getEntity().getContent(), StandardCharsets.UTF_8));
                        break;
                    }
                    default: {
                        throw new ClientIntegrationException(String.format(
                                "Couldn't apply CpiExternalConfiguration %s: Code: %d, Message: %s",
                                cpiExternalConfiguration.toString(),
                                uploadExternalConfigurationResponse.getStatusLine().getStatusCode(),
                                IOUtils.toString(uploadExternalConfigurationResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                        );
                    }
                }
            } catch (Exception ex) {
                log.error("Error occurred while applying CpiExternalConfiguration: " + ex.getMessage(), ex);
            } finally {
                HttpClientUtils.closeQuietly(uploadExternalConfigurationResponse);
            }
        }

    }

    public IntegrationContentErrorInformation getIntegrationContentErrorInformation(ConnectionProperties connectionProperties, IntegrationContent integrationContent) {
        log.debug("#getIntegrationContentErrorInformation(ConnectionProperties connectionProperties, IntegrationContent integrationContent):, {}, {}", connectionProperties, integrationContent);

        try {
            URI uri = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(String.format("/api/v1/IntegrationRuntimeArtifacts('%s')/ErrorInformation/$value", integrationContent.getId()))
                    .build()
                    .encode()
                    .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                switch (response.getStatusLine().getStatusCode()) {
                    case 200: {
                        String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                        IntegrationContentErrorInformation integrationContentErrorInformation = objectMapper.readValue(result, IntegrationContentErrorInformation.class);
                        return integrationContentErrorInformation;
                    }
                    case 204: {
                        return new IntegrationContentErrorInformation();
                    }
                    default: {
                        String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                        throw new RuntimeException("Couldn't error information about runtime artifact GET request:\n" + result);
                    }
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        } catch (Exception ex) {
            log.error("Error occurred while fetching error information about runtime artifact " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching error information about runtime artifact: " + ex.getMessage(), ex);
        }
    }

    private List<IntegrationContent> getAllIntegrationRuntimeArtifacts(ConnectionProperties connectionProperties, URI uri) {
        log.debug("#getAllIntegrationRuntimeArtifacts(ConnectionProperties connectionProperties, URI uri): {}, {}", connectionProperties, uri);

        try {

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<IntegrationContent> artifacts = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject integrationContentEntry = results.getJSONObject(ind);
                        IntegrationContent integrationContent = fillIntegrationContent(integrationContentEntry);
                        artifacts.add(integrationContent);
                    }

                    return artifacts;
                } else {
                    throw new RuntimeException("Couldn't execute integration runtime artifacts GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching integration runtime artifacts " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching integration runtime artifacts: " + ex.getMessage(), ex);
        }
    }

    private IntegrationContent fillIntegrationContent(JSONObject integrationContentEntry) throws JSONException {
        IntegrationContent integrationContent = new IntegrationContent();
        integrationContent.setId(integrationContentEntry.getString("Id"));
        integrationContent.setVersion(integrationContentEntry.getString("Version"));
        integrationContent.setName(integrationContentEntry.getString("Name"));
        integrationContent.setType(integrationContentEntry.getString("Type"));
        integrationContent.setDeployedBy(integrationContentEntry.getString("DeployedBy"));
        integrationContent.setDeployedOn(
                new Timestamp(Long.parseLong(integrationContentEntry.getString("DeployedOn").replaceAll("[^0-9]", "")))
        );
        integrationContent.setStatus(integrationContentEntry.getString("Status"));
        return integrationContent;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
