package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.CloudPlatformType;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateRestOrSoapApiRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateRestOrSoapApiRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.*;
import static java.lang.String.format;
import static org.apache.commons.collections4.SetUtils.hashSet;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiRestAndSoapApiClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_REST_API = "/itspaces/api/1.0/workspace/%s/restapis/";
    private static final String API_UPLOAD_SOAP_API = "/itspaces/api/1.0/workspace/%s/soapapis/";
    private static final String API_DEPLOY_REST_OR_SOAP_API = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY";

    public CpiRestAndSoapApiClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
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

    public List<CpiArtifact> getSoapApiObjectsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        log.debug("#getSoapApisByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId
        );
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            SOAP_API
        );
    }

    public List<CpiArtifact> getRestAndSoapApiObjectsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        log.debug("#getRestAndSoapApiObjectsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId
        );
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            hashSet(REST_API, SOAP_API)
        );
    }

    public byte[] downloadRestOrSoapApi(
        RequestContext requestContext,
        String packageExternalId,
        String restOrSoapApiExternalId
    ) {
        log.debug("#downloadRestOrSoapApi(RequestContext requestContext, String packageExternalId, String restOrSoapApiExternalId): {}, {}, {}",
            requestContext, packageExternalId, restOrSoapApiExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, restOrSoapApiExternalId);
    }

    public void createRestApi(RequestContext requestContext, CreateRestOrSoapApiRequest request) {
        log.debug("#createRestApi(RequestContext requestContext, CreateRestOrSoapApiRequest request): {}, {}", requestContext, request);
        createRestOrSoapApi(requestContext, API_UPLOAD_REST_API, request);
    }

    public void createSoapApi(RequestContext requestContext, CreateRestOrSoapApiRequest request) {
        log.debug("#createSoapApi(RequestContext requestContext, CreateRestOrSoapApiRequest request): {}, {}", requestContext, request);
        createRestOrSoapApi(requestContext, API_UPLOAD_SOAP_API, request);
    }

    private void createRestOrSoapApi(RequestContext requestContext, String apiUploadUrl, CreateRestOrSoapApiRequest request) {
        executeMethod(
            requestContext,
            String.format(apiUploadUrl, request.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    request,
                    "iflowBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );
    }

    @Deprecated
    public void updateRestOrSoapApi(RequestContext requestContext, UpdateRestOrSoapApiRequest request) {
        log.debug("#updateRestOrSoapApi(RequestContext requestContext, UpdateRestOrSoapApiRequest request): {}, {}", requestContext, request);
        updateArtifact(requestContext, request);
    }

    public String deployRestApi(RequestContext requestContext, String packageExternalId, String restApiExternalId, String restApiTechnicalName) {
        log.debug("#deployRestApi(RequestContext commonClientWrapperEntity, String packageExternalId, String restApiExternalId): {}, {}, {}, {}",
            requestContext, packageExternalId, restApiExternalId, restApiTechnicalName
        );
        return deployRestOrSoapApi(requestContext, packageExternalId, restApiExternalId, restApiTechnicalName, REST_API);
    }

    public String deploySoapApi(RequestContext requestContext, String packageExternalId, String soapApiExternalId, String soapApiTechnicalName) {
        log.debug("#deploySoapApi(RequestContext commonClientWrapperEntity, String packageExternalId, String soapApiTechnicalName): {}, {}, {}, {}",
            requestContext, packageExternalId, soapApiExternalId, soapApiTechnicalName
        );
        return deployRestOrSoapApi(requestContext, packageExternalId, soapApiExternalId, soapApiTechnicalName, SOAP_API);
    }

    private String deployRestOrSoapApi(RequestContext requestContext, String packageExternalId, String restOrSoapApiExternalId, String restOrSoapApiTechnicalName, CpiArtifactType cpiArtifactType) {
        return executeMethod(
            requestContext,
            String.format(API_DEPLOY_REST_OR_SOAP_API, packageExternalId, restOrSoapApiExternalId, restOrSoapApiExternalId, restOrSoapApiTechnicalName),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                cpiArtifactType,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public void deleteRestOrSoapApi(
        String packageExternalId,
        String restOrSoapApiExternalId,
        String restOrSoapApiTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#deleteRestOrSoapApi(String packageExternalId, String restOrSoapApiExternalId, String restOrSoapApiTechnicalName, RequestContext requestContext): {}, {}, {}, {}",
            packageExternalId, restOrSoapApiExternalId, restOrSoapApiTechnicalName, requestContext
        );

        deleteArtifact(packageExternalId, restOrSoapApiExternalId, restOrSoapApiTechnicalName, requestContext);
    }

    public void undeployRestOrSoapApi(RequestContext requestContext, String restOrSoapApiTechnicalName) {
        log.debug("#undeployRestOrSoapApi(RequestContext requestContext, String restOrSoapApiTechnicalName): {}, {}", requestContext, restOrSoapApiTechnicalName);
        executeDeletePublicApi(
            requestContext,
            format("/api/v1/IntegrationRuntimeArtifacts('%s')", restOrSoapApiTechnicalName),
            Objects::nonNull
        );
    }

    public void deleteAndUndeployRestOrSoapApi(
        String packageExternalId,
        String restOrSoapApiExternalId,
        String restOrSoapApiTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#deleteAndUndeployRestOrSoapApi(String packageExternalId, String restOrSoapApiExternalId, String restOrSoapApiTechnicalName, RequestContext requestContext): {}, {}, {}, {}",
            packageExternalId, restOrSoapApiExternalId, restOrSoapApiTechnicalName, requestContext
        );

        deleteRestOrSoapApi(packageExternalId, restOrSoapApiExternalId, restOrSoapApiTechnicalName, requestContext);
        //It doesn't work for NEO due to a SAP bug
        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            undeployRestOrSoapApi(requestContext, restOrSoapApiTechnicalName);
        }
    }

}
