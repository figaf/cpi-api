package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.IFlowResource;
import com.figaf.integration.cpi.response_parser.IFlowResourcesParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IFlowResourcesClient extends CpiBaseClient {

    private static final String API_IFLOW_RESOURCES = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/resource";

    private final ObjectMapper jsonMapper;

    public IFlowResourcesClient(
        String ssoUrl,
        HttpClientsFactory httpClientsFactory
    ) {
        super(ssoUrl, httpClientsFactory);
        this.jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public List<IFlowResource> getIFlowResources(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, String externalIFlowId) {
        log.debug("#getIFlowResources(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, String externalIFlowId): {}, {}, {}", commonClientWrapperEntity, externalPackageId, externalIFlowId);
        String path = String.format(API_IFLOW_RESOURCES, externalPackageId, externalIFlowId, externalIFlowId);
        return executeGet(commonClientWrapperEntity, path, body -> IFlowResourcesParser.buildIFlowResources(body, jsonMapper));
    }


    public void createResourceForIFlow(ConnectionProperties connectionProperties, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent) {
        log.debug("#createResource(ConnectionProperties connectionProperties, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent): " +
                "{}, {}, {}, {}, {}", connectionProperties, iFlowName, resourceName, resourceExtension, base64ResourceContent);

        try {
            HttpClient client = httpClientsFactory.createHttpClient();

            String csrfToken = getCsrfToken(connectionProperties, client);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/Resources", iFlowName));

            if (connectionProperties.getPort() != null) {
                uriBuilder.port(connectionProperties.getPort());
            }
            URI uri = uriBuilder.build().toUri();

            JSONObject requestBody = new JSONObject()
                    .put("Name", resourceName)
                    .put("ResourceType", resourceExtension)
                    .put("ResourceContent", base64ResourceContent);

            HttpPost createIFlowResourceRequest = new HttpPost(uri);
            createIFlowResourceRequest.setHeader(createBasicAuthHeader(connectionProperties));
            createIFlowResourceRequest.setHeader(createCsrfTokenHeader(csrfToken));
            createIFlowResourceRequest.setHeader("Accept", "application/json");
            createIFlowResourceRequest.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            HttpResponse createIFlowResourceResponse = null;
            try {

                createIFlowResourceResponse = client.execute(createIFlowResourceRequest);

                switch (createIFlowResourceResponse.getStatusLine().getStatusCode()) {
                    case 200:
                    case 201:
                    case 202: {
                        log.debug("Resource {} was created for the iFlow {}: {}", resourceName, iFlowName, IOUtils.toString(createIFlowResourceResponse.getEntity().getContent(), StandardCharsets.UTF_8));
                        break;
                    }
                    default: {
                        throw new ClientIntegrationException(String.format(
                                "Couldn't create resource %s for the iFlow %s: Code: %d, Message: %s",
                                resourceName,
                                iFlowName,
                                createIFlowResourceResponse.getStatusLine().getStatusCode(),
                                IOUtils.toString(createIFlowResourceResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                        );
                    }
                }

            } finally {
                HttpClientUtils.closeQuietly(createIFlowResourceResponse);
            }
        } catch (Exception ex) {
            log.error("Error occurred while creating resource for an iFlow" + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while creating resource for an iFlow: " + ex.getMessage(), ex);
        }

    }

    public void updateIFlowResource(ConnectionProperties connectionProperties, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent) {
        log.debug("#updateIFlowResource(ConnectionProperties connectionProperties, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent): " +
                "{}, {}, {}, {}, {}", connectionProperties, iFlowName, resourceName, resourceExtension, base64ResourceContent);

        try {
            HttpClient client = httpClientsFactory.createHttpClient();

            String csrfToken = getCsrfToken(connectionProperties, client);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/$links/Resources(Name='%s',ResourceType='%s')",
                            iFlowName, resourceName, resourceExtension)
                    );

            if (connectionProperties.getPort() != null) {
                uriBuilder.port(connectionProperties.getPort());
            }
            URI uri = uriBuilder.build().toUri();

            JSONObject requestBody = new JSONObject()
                    .put("ResourceContent", base64ResourceContent);

            HttpPut updateIFlowResourceRequest = new HttpPut(uri);
            updateIFlowResourceRequest.setHeader(createBasicAuthHeader(connectionProperties));
            updateIFlowResourceRequest.setHeader(createCsrfTokenHeader(csrfToken));
            updateIFlowResourceRequest.setHeader("Accept", "application/json");
            updateIFlowResourceRequest.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            HttpResponse updateIFlowResourceResponse = null;
            try {

                updateIFlowResourceResponse = client.execute(updateIFlowResourceRequest);

                switch (updateIFlowResourceResponse.getStatusLine().getStatusCode()) {
                    case 200:
                    case 201:
                    case 202: {
                        log.debug("Resource {} was updated of the iFlow {}: {}", resourceName, iFlowName, IOUtils.toString(updateIFlowResourceResponse.getEntity().getContent(), StandardCharsets.UTF_8));
                        break;
                    }
                    default: {
                        throw new ClientIntegrationException(String.format(
                                "Couldn't update resource %s of the iFlow %s: Code: %d, Message: %s",
                                resourceName,
                                iFlowName,
                                updateIFlowResourceResponse.getStatusLine().getStatusCode(),
                                IOUtils.toString(updateIFlowResourceResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                        );
                    }
                }

            } finally {
                HttpClientUtils.closeQuietly(updateIFlowResourceResponse);
            }
        } catch (Exception ex) {
            log.error("Error occurred while updating resource of an iFlow" + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while updating resource of an iFlow: " + ex.getMessage(), ex);
        }

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
