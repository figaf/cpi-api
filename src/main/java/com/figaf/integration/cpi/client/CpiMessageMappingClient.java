package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateMessageMappingRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateMessageMappingRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.MESSAGE_MAPPING;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiMessageMappingClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_MESSAGE_MAPPING = "/itspaces/api/1.0/workspace/%s/messagemappings/";
    private static final String API_DEPLOY_MESSAGE_MAPPING = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/messagemappings/%s?webdav=DEPLOY";

    public CpiMessageMappingClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getMessageMappingsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getMessageMappingsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            MESSAGE_MAPPING
        );
    }

    public byte[] downloadMessageMapping(
            RequestContext requestContext,
            String packageExternalId,
            String messageMappingExternalId
    ) {
        log.debug("#downloadMessageMapping(RequestContext requestContext, String packageExternalId, String messageMappingExternalId): {}, {}, {}",
                requestContext, packageExternalId, messageMappingExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, messageMappingExternalId);
    }

    public void createMessageMapping(RequestContext requestContext, CreateMessageMappingRequest request) {
        log.debug("#createMessageMapping(RequestContext requestContext, CreateMessageMappingRequest request): {}, {}", requestContext, request);
        executeMethod(
            requestContext,
            String.format(API_UPLOAD_MESSAGE_MAPPING, request.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    request,
                    "mmBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );

    }

    public void updateMessageMapping(RequestContext requestContext, UpdateMessageMappingRequest request) {
        log.debug("#updateMessageMapping(RequestContext requestContext, UpdateMessageMappingRequest request): {}, {}", requestContext, request);
        updateArtifact(requestContext, request);
    }

    public String deployMessageMapping(
        RequestContext requestContext,
        String packageExternalId,
        String messageMappingExternalId
    ) {
        log.debug("#deployMessageMapping(RequestContext commonClientWrapperEntity, String packageExternalId, " +
                "String messageMappingExternalId): {}, {}, {}",
            requestContext, packageExternalId, messageMappingExternalId
        );

        return executeMethod(
            requestContext,
            String.format(
                API_DEPLOY_MESSAGE_MAPPING,
                packageExternalId,
                messageMappingExternalId,
                messageMappingExternalId,
                messageMappingExternalId
            ),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                MESSAGE_MAPPING,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteMessageMapping(
        String packageExternalId,
        String messageMappingExternalId,
        String messageMappingTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#deleteMessageMapping(String packageExternalId, String messageMappingExternalId, " +
                "String messageMappingTechnicalName, RequestContext requestContext): {}, {}, {}, {}",
            packageExternalId, messageMappingExternalId, messageMappingTechnicalName, requestContext);

        deleteArtifact(packageExternalId, messageMappingExternalId, messageMappingTechnicalName, requestContext);
    }

}
