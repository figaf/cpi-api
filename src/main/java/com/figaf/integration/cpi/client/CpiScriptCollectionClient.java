package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateScriptCollectionRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.SCRIPT_COLLECTION;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiScriptCollectionClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_SCRIPT_COLLECTION = "/itspaces/api/1.0/workspace/%s/scriptcollections/?isImport=true";
    private static final String API_DEPLOY_SCRIPT_COLLECTION = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/scriptcollections/%s?action=DEPLOY";

    public CpiScriptCollectionClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getScriptCollectionsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getScriptCollectionsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            SCRIPT_COLLECTION
        );
    }

    public byte[] downloadScriptCollection(
            RequestContext requestContext,
            String packageExternalId,
            String scriptCollectionExternalId
    ) {
        log.debug("#downloadScriptCollection(RequestContext requestContext, String packageExternalId, String scriptCollectionExternalId): {}, {}, {}",
                requestContext, packageExternalId, scriptCollectionExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, scriptCollectionExternalId);
    }

    public void createScriptCollection(
        RequestContext requestContext,
        String packageExternalId,
        CreateOrUpdateScriptCollectionRequest request,
        byte[] model
    ) {
        log.debug("#createScriptCollection(RequestContext requestContext, String packageExternalId, CreateOrUpdateScriptCollectionRequest request, byte[] model): " +
                "{}, {}, {}", requestContext, packageExternalId, request);

        executeMethod(
            requestContext,
            String.format(API_UPLOAD_SCRIPT_COLLECTION, packageExternalId),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    packageExternalId,
                    request,
                    model,
                    "scriptBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );

    }

    public void updateScriptCollection(
            RequestContext requestContext,
            String packageExternalId,
            String scriptCollectionExternalId,
            CreateOrUpdateScriptCollectionRequest request,
            byte[] model
    ) {
        log.debug("#updateScriptCollection(RequestContext requestContext, String packageExternalId, String scriptCollectionExternalId, " +
                "CreateOrUpdateScriptCollectionRequest request, byte[] model): {}, {}, {}, {}",
            requestContext, packageExternalId, scriptCollectionExternalId, request);
        updateArtifact(requestContext, packageExternalId, scriptCollectionExternalId, request, model, false, null);
    }

    public String deployScriptCollection(
        RequestContext requestContext,
        String packageExternalId,
        String scriptCollectionExternalId,
        String scriptCollectionTechnicalName
    ) {
        log.debug("#deployScriptCollection(RequestContext commonClientWrapperEntity, String packageExternalId, " +
                "String scriptCollectionExternalId, String scriptCollectionTechnicalName): {}, {}, {}, {}",
            requestContext, packageExternalId, scriptCollectionExternalId, scriptCollectionTechnicalName
        );

        return executeMethod(
            requestContext,
            String.format(
                API_DEPLOY_SCRIPT_COLLECTION,
                packageExternalId,
                scriptCollectionExternalId,
                scriptCollectionExternalId,
                scriptCollectionTechnicalName
            ),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                SCRIPT_COLLECTION,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteScriptCollection(
        String packageExternalId,
        String scriptCollectionExternalId,
        String scriptCollectionName,
        RequestContext requestContext
    ) {
        log.debug("#deleteScriptCollection(String packageExternalId, String scriptCollectionExternalId, String scriptCollectionName, RequestContext requestContext): " +
                "{}, {}, {}, {}", packageExternalId, scriptCollectionExternalId, scriptCollectionName, requestContext);

        deleteArtifact(packageExternalId, scriptCollectionExternalId, scriptCollectionName, requestContext);
    }

}
