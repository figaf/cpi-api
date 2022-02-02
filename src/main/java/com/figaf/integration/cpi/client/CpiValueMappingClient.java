package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateValueMappingRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.VALUE_MAPPING;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiValueMappingClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_VALUE_MAPPING = "/itspaces/api/1.0/workspace/%s/valuemappings/";
    private static final String API_DEPLOY_VALUE_MAPPING = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/valuemappings/%s?webdav=DEPLOY";

    public CpiValueMappingClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getValueMappingsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getValueMappingsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            VALUE_MAPPING
        );
    }

    public byte[] downloadValueMapping(
            RequestContext requestContext,
            String packageExternalId,
            String valueMappingExternalId
    ) {
        log.debug("#downloadValueMapping(RequestContext requestContext, String packageExternalId, String valueMappingExternalId): {}, {}, {}",
                requestContext, packageExternalId, valueMappingExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, valueMappingExternalId);
    }

    public void createValueMapping(RequestContext requestContext, String packageExternalId, CreateOrUpdateValueMappingRequest request, byte[] model) {
        log.debug("#createValueMapping(RequestContext requestContext, String packageExternalId, CreateValueMappingRequest request, byte[] model): " +
                "{}, {}, {}", requestContext, packageExternalId, request);

        executeMethod(
            requestContext,
            String.format(API_UPLOAD_VALUE_MAPPING, packageExternalId),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    packageExternalId,
                    request,
                    model,
                    "vmBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );

    }

    public void updateValueMapping(
            RequestContext requestContext,
            String packageExternalId,
            String valueMappingExternalId,
            CreateOrUpdateValueMappingRequest request,
            byte[] model
    ) {
        log.debug("#updateValueMapping(RequestContext requestContext, String packageExternalId, String valueMappingExternalId, " +
                "CreateOrUpdateValueMappingRequest request, byte[] model): {}, {}, {}, {}",
            requestContext, packageExternalId, valueMappingExternalId, request);
        updateArtifact(requestContext, packageExternalId, valueMappingExternalId, request, model, false, null);
    }

    public String deployValueMapping(RequestContext requestContext, String packageExternalId, String valueMappingExternalId) {
        log.debug("#deployValueMapping(RequestContext commonClientWrapperEntity, String packageExternalId, String valueMappingExternalId): {}, {}, {}",
            requestContext, packageExternalId, valueMappingExternalId
        );

        return executeMethod(
            requestContext,
            String.format(API_DEPLOY_VALUE_MAPPING, "undefined", valueMappingExternalId, valueMappingExternalId, valueMappingExternalId),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                VALUE_MAPPING,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteValueMapping(
        String packageExternalId,
        String valueMappingExternalId,
        String valueMappingName,
        RequestContext requestContext
    ) {
        log.debug("#deleteValueMapping(String packageExternalId, String valueMappingExternalId, String valueMappingName, RequestContext requestContext): " +
                "{}, {}, {}, {}", packageExternalId, valueMappingExternalId, valueMappingName, requestContext);

        deleteArtifact(packageExternalId, valueMappingExternalId, valueMappingName, requestContext);
    }

}
