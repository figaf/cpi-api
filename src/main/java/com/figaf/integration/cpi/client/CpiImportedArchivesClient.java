package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateImportedArchivesRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IMPORTED_ARCHIVES;

@Slf4j
public class CpiImportedArchivesClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_IMPORTED_ARCHIVES = "/itspaces/api/1.0/workspace/%s/importedarchives/?isImport=true";

    //scriptcollections is not a mistake here. SAP CPI really uses such endpoint for Imported archives
    private static final String API_DEPLOY_IMPORTED_ARCHIVES = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/scriptcollections/%s?action=DEPLOY";


    public CpiImportedArchivesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CpiArtifact> getImportedArchivesByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        log.debug("#getImportedArchivesByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
                "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            IMPORTED_ARCHIVES
        );
    }


    public void createImportedArchives(RequestContext requestContext, CreateImportedArchivesRequest request) {
        log.debug("#createImportedArchives(RequestContext requestContext, CreateImportedArchivesRequest request): {}, {}", requestContext, request);
        executeMethod(
            requestContext,
            String.format(API_UPLOAD_IMPORTED_ARCHIVES, request.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    request,
                    "scriptBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );
    }

    public String deployImportedArchives(
        RequestContext requestContext,
        String packageExternalId,
        String importedArchivesExternalId,
        String importedArchivesTechnicalName
    ) {
        log.debug("#deployImportedArchives(RequestContext commonClientWrapperEntity, String packageExternalId, " +
                "String importedArchivesExternalId, String importedArchivesTechnicalName): {}, {}, {}, {}",
            requestContext, packageExternalId, importedArchivesExternalId, importedArchivesTechnicalName
        );

        return executeMethod(
            requestContext,
            String.format(
                API_DEPLOY_IMPORTED_ARCHIVES,
                packageExternalId,
                importedArchivesExternalId,
                importedArchivesExternalId,
                importedArchivesTechnicalName
            ),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                IMPORTED_ARCHIVES,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }
}
