package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

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

    public void createResourceForIFlow(RequestContext requestContext, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent) {
        log.debug("#createResourceForIFlow(RequestContext requestContext, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent): {}, {}, {}, {}, {}",
            requestContext, iFlowName, resourceName, resourceExtension, base64ResourceContent
        );
        JSONObject requestBody = new JSONObject()
            .put("Name", resourceName)
            .put("ResourceType", resourceExtension)
            .put("ResourceContent", base64ResourceContent);
        executeMethodPublicApi(
            requestContext,
            String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/Resources", iFlowName),
            requestBody.toString(),
            HttpMethod.POST,
            responseEntity -> {
                switch (responseEntity.getStatusCode().value()) {
                    case 200:
                    case 201:
                    case 202: {
                        log.debug("Resource {} was created for the iFlow {}: {}", resourceName, iFlowName, responseEntity.getBody());
                        break;
                    }
                    default: {
                        throw new ClientIntegrationException(String.format(
                            "Couldn't create resource %s for the iFlow %s: Code: %d, Message: %s",
                            resourceName,
                            iFlowName,
                            responseEntity.getStatusCode().value(),
                            responseEntity.getBody()
                        ));
                    }
                }

                return null;
            }
        );
    }

    public void updateIFlowResource(RequestContext requestContext, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent) {
        log.debug("#updateIFlowResource(RequestContext requestContext, String iFlowName, String resourceName, String resourceExtension, String base64ResourceContent): {}, {}, {}, {}, {}",
            requestContext, iFlowName, resourceName, resourceExtension, base64ResourceContent
        );
        JSONObject requestBody = new JSONObject()
            .put("ResourceContent", base64ResourceContent);

        executeMethodPublicApi(
            requestContext,
            String.format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')/$links/Resources(Name='%s',ResourceType='%s')",
                iFlowName, resourceName, resourceExtension),
            requestBody.toString(),
            HttpMethod.PUT,
            responseEntity -> {
                switch (responseEntity.getStatusCode().value()) {
                    case 200:
                    case 201:
                    case 202: {
                        log.debug("Resource {} was updated of the iFlow {}: {}", resourceName, iFlowName, responseEntity.getBody());
                        break;
                    }
                    default: {
                        throw new ClientIntegrationException(String.format(
                            "Couldn't update resource %s of the iFlow %s: Code: %d, Message: %s",
                            resourceName,
                            iFlowName,
                            responseEntity.getStatusCode().value(),
                            responseEntity.getBody())
                        );
                    }
                }

                return null;
            }
        );
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
