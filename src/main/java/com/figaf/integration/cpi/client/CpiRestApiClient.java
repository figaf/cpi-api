package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateRestApiRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.*;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiRestApiClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_REST_API = "/itspaces/api/1.0/workspace/%s/restapis/";
    private static final String API_DEPLOY_REST_API = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY";

    public CpiRestApiClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getRestApiObjectsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getRestApisByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            REST_API
        );
    }

    public byte[] downloadRestApi(
            RequestContext requestContext,
            String packageExternalId,
            String restApiExternalId
    ) {
        log.debug("#downloadValueMapping(RequestContext requestContext, String packageExternalId, String restApiExternalId): {}, {}, {}",
                requestContext, packageExternalId, restApiExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, restApiExternalId);
    }

    public void createRestApi(RequestContext requestContext, String packageExternalId, CreateOrUpdateRestApiRequest request, byte[] model) {
        log.debug("#createRestApi(RequestContext requestContext, String packageExternalId, CreateOrUpdateRestApiRequest request, byte[] model): " +
                "{}, {}, {}", requestContext, packageExternalId, request);

        executeMethod(
            requestContext,
            String.format(API_UPLOAD_REST_API, packageExternalId),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    packageExternalId,
                    request,
                    model,
                    "iflowBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );

    }

    public void updateRestApi(
            RequestContext requestContext,
            String packageExternalId,
            String restApiExternalId,
            CreateOrUpdateRestApiRequest request,
            byte[] model
    ) {
        log.debug("#updateRestApi(RequestContext requestContext, String packageExternalId, String restApiExternalId, " +
                "CreateOrUpdateRestApiRequest request, byte[] model): {}, {}, {}, {}",
            requestContext, packageExternalId, restApiExternalId, request);
        updateArtifact(requestContext, packageExternalId, restApiExternalId, request, model, false, null);
    }

    public String deployRestApi(RequestContext requestContext, String packageExternalId, String restApiExternalId, String restApiTechnicalName) {
        log.debug("#deployRestApi(RequestContext commonClientWrapperEntity, String packageExternalId, String restApiExternalId): {}, {}, {}, {}",
            requestContext, packageExternalId, restApiExternalId, restApiTechnicalName
        );

        return executeMethod(
            requestContext,
            String.format(API_DEPLOY_REST_API, packageExternalId, restApiExternalId, restApiExternalId, restApiTechnicalName),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                REST_API,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteRestApi(
        String packageExternalId,
        String restApiExternalId,
        String restApiTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#deleteRestApi(String packageExternalId, String restApiExternalId, String restApiTechnicalName, RequestContext requestContext): " +
                "{}, {}, {}, {}", packageExternalId, restApiExternalId, restApiTechnicalName, requestContext);

        deleteArtifact(packageExternalId, restApiExternalId, restApiTechnicalName, requestContext);
    }

}
