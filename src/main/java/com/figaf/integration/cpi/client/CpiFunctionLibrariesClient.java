package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateFunctionLibrariesRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.*;
import static com.figaf.integration.cpi.utils.CpiApiUtils.appendRuntimeProfileIfPresent;

@Slf4j
public class CpiFunctionLibrariesClient extends CpiRuntimeArtifactClient {

    private static final String API_CREATE_FUNCTION_LIBRARIES = "/itspaces/api/1.0/workspace/%s/functionlibraries/";

    private static final String API_UPLOAD_FUNCTION_LIBRARIES = API_CREATE_FUNCTION_LIBRARIES + "?isImport=true";

    //scriptcollections is not a mistake here. SAP CPI really uses such endpoint for Function libraries
    private static final String API_DEPLOY_FUNCTION_LIBRARIES = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/scriptcollections/%s?action=DEPLOY";


    public CpiFunctionLibrariesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CpiArtifact> getFunctionLibrariesByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        log.debug("#getFunctionLibrariesByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
                "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            FUNCTION_LIBRARIES
        );
    }


    public void createFunctionLibraries(RequestContext requestContext, CreateFunctionLibrariesRequest request) {
        log.debug("#createFunctionLibraries(RequestContext requestContext, CreateFunctionLibrariesRequest request): {}, {}", requestContext, request);
        String finalizedFunctionLibrariesUrl = Objects.nonNull(request.getBundledModel())
            ? API_UPLOAD_FUNCTION_LIBRARIES :
            API_CREATE_FUNCTION_LIBRARIES;
        executeMethod(
            requestContext,
            String.format(finalizedFunctionLibrariesUrl, request.getPackageExternalId()),
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

    public String deployFunctionLibraries(
        RequestContext requestContext,
        String packageExternalId,
        String functionLibrariesExternalId,
        String functionLibrariesTechnicalName
    ) {
        log.debug("#deployFunctionLibraries(RequestContext commonClientWrapperEntity, String packageExternalId, " +
                "String functionLibrariesExternalId, String functionLibrariesTechnicalName): {}, {}, {}, {}",
            requestContext, packageExternalId, functionLibrariesExternalId, functionLibrariesTechnicalName
        );

        String baseUrl = String.format(
            API_DEPLOY_FUNCTION_LIBRARIES,
            packageExternalId,
            functionLibrariesExternalId,
            functionLibrariesExternalId,
            functionLibrariesTechnicalName
        );
        String resolvedUrl = appendRuntimeProfileIfPresent(
            baseUrl,
            requestContext.getRuntimeLocationId(),
            requestContext
        );

        return executeMethod(
            requestContext,
            resolvedUrl,
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                FUNCTION_LIBRARIES,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }
}
