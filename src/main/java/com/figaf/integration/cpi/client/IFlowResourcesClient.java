package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IFlowResourcesClient extends ArtifactResourcesClient {

    public IFlowResourcesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public ArtifactResources getIFlowResources(RequestContext requestContext, String externalPackageId, String externalIFlowId) {
        log.debug("#getIFlowResources(RequestContext requestContext, String externalPackageId, String externalIFlowId): " +
            "{}, {}, {}", requestContext, externalPackageId, externalIFlowId);
        return getArtifactResources(requestContext, externalPackageId, externalIFlowId, "IFlow");
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

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
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

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
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

    public String getIFlowResourceContent(RequestContext requestContext, String iflowTechnicalName, String resourceName, String resourceType) {
        log.debug("#getIFlowResourceContent(RequestContext requestContext, String iflowTechnicalName, String resourceName, String resourceType): {}, {}, {}, {}", requestContext, iflowTechnicalName, resourceName, resourceType);
        return executeGetPublicApiAndReturnResponseBody(
            requestContext,
            format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/Resources(Name='%s',ResourceType='%s')/$value", iflowTechnicalName, resourceName, resourceType),
            responseBody -> responseBody
        );
    }

}
