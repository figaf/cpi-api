package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateSharedMessageMappingRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateSharedMessageMappingRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.SHARED_MESSAGE_MAPPING;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiSharedMessageMappingClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_SHARED_MESSAGE_MAPPING = "/itspaces/api/1.0/workspace/%s/messagemappings/";
    private static final String API_DEPLOY_SHARED_MESSAGE_MAPPING = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/messagemappings/%s?webdav=DEPLOY";

    public CpiSharedMessageMappingClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getSharedMessageMappingByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getSharedMessageMappingByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            SHARED_MESSAGE_MAPPING
        );
    }

    public byte[] downloadSharedMessageMapping(
            RequestContext requestContext,
            String packageExternalId,
            String sharedMessageMappingExternalId
    ) {
        log.debug("#downloadSharedMessageMapping(RequestContext requestContext, String packageExternalId, String sharedMessageMappingExternalId): {}, {}, {}",
                requestContext, packageExternalId, sharedMessageMappingExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, sharedMessageMappingExternalId);
    }

    public void createSharedMessageMapping(RequestContext requestContext, CreateSharedMessageMappingRequest request) {
        log.debug("#createSharedMessageMapping(RequestContext requestContext, CreateSharedMessageMappingRequest request): {}, {}", requestContext, request);
        executeMethod(
            requestContext,
            String.format(API_UPLOAD_SHARED_MESSAGE_MAPPING, request.getPackageExternalId()),
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

    public void updateSharedMessageMapping(RequestContext requestContext, UpdateSharedMessageMappingRequest request) {
        log.debug("#updateSharedMessageMapping(RequestContext requestContext, UpdateSharedMessageMappingRequest request): {}, {}", requestContext, request);
        updateArtifact(requestContext, request);
    }

    public String deploySharedMessageMapping(
        RequestContext requestContext,
        String packageExternalId,
        String sharedMessageMappingExternalId
    ) {
        log.debug("#deploySharedMessageMapping(RequestContext commonClientWrapperEntity, String packageExternalId, " +
                "String sharedMessageMappingExternalId): {}, {}, {}",
            requestContext, packageExternalId, sharedMessageMappingExternalId
        );

        return executeMethod(
            requestContext,
            String.format(
                API_DEPLOY_SHARED_MESSAGE_MAPPING,
                packageExternalId,
                sharedMessageMappingExternalId,
                sharedMessageMappingExternalId,
                sharedMessageMappingExternalId
            ),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                SHARED_MESSAGE_MAPPING,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteSharedMessageMapping(
        String packageExternalId,
        String sharedMessageMappingExternalId,
        String sharedMessageMappingTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#deleteSharedMessageMapping(String packageExternalId, String sharedMessageMappingExternalId, " +
                "String sharedMessageMappingTechnicalName, RequestContext requestContext): {}, {}, {}, {}",
            packageExternalId, sharedMessageMappingExternalId, sharedMessageMappingTechnicalName, requestContext);

        deleteArtifact(packageExternalId, sharedMessageMappingExternalId, sharedMessageMappingTechnicalName, requestContext);
    }

}
