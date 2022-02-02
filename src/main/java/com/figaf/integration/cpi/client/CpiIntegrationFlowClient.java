package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateIFlowRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;

/**
 * @author Arsenii Istlentev
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiIntegrationFlowClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_IFLOW = "/itspaces/api/1.0/workspace/%s/iflows/";
    private static final String API_DEPLOY_IFLOW = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY";
    private static final String API_SET_TRACE_LOG_LEVEL_FOR_IFLOWS = "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentSetMplLogLevelCommand";

    public CpiIntegrationFlowClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(integrationPackageClient, httpClientsFactory);
    }

    public List<CpiArtifact> getIFlowsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId
    ) {
        log.debug("#getIFlowsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
            "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            IFLOW
        );
    }

    public byte[] downloadIFlow(
            RequestContext requestContext,
            String packageExternalId,
            String iFlowExternalId
    ) {
        log.debug("#downloadIFlow(RequestContext requestContext, String packageExternalId, String iFlowExternalId): {}, {}, {}",
                requestContext, packageExternalId, iFlowExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, iFlowExternalId);
    }

    public void createIFlow(
        RequestContext requestContext,
        String packageExternalId,
        CreateOrUpdateIFlowRequest request,
        byte[] bundledModel
    ) {
        log.debug("#createIFlow(RequestContext requestContext, String packageExternalId, CreateIFlowRequest request, " +
                "byte[] bundledModel): {}, {}, {}", requestContext, packageExternalId, request);

        executeMethod(
                requestContext,
                String.format(API_UPLOAD_IFLOW, packageExternalId),
                (url, token, restTemplateWrapper) -> {
                    createArtifact(
                        requestContext.getConnectionProperties(),
                        packageExternalId,
                        request,
                        bundledModel,
                        "iflowBrowse-data",
                        url,
                        token,
                        restTemplateWrapper
                    );
                    return null;
                }
        );

    }

    public void updateIFlow(
            RequestContext requestContext,
            String packageExternalId,
            String iFlowExternalId,
            CreateOrUpdateIFlowRequest request,
            byte[] bundledModel
    ) {
        log.debug("#updateIFlow(RequestContext requestContext, String packageExternalId, String iFlowExternalId, " +
            "CreateOrUpdateIFlowRequest request, byte[] bundledModel): {}, {}, {}, {}",
            requestContext, packageExternalId, iFlowExternalId, request);
        updateArtifact(requestContext, packageExternalId, iFlowExternalId, request, bundledModel, false, null);
    }

    public String deployIFlow(
        RequestContext requestContext,
        String packageExternalId,
        String iFlowExternalId,
        String iFlowTechnicalName
    ) {
        log.debug("#deployIFlow(RequestContext requestContext, String packageExternalId, String iFlowExternalId, String iFlowTechnicalName): {}, {}, {}, {}",
                requestContext, packageExternalId, iFlowExternalId, iFlowTechnicalName
        );
        return executeMethod(
                requestContext,
                String.format(API_DEPLOY_IFLOW, packageExternalId, iFlowExternalId, iFlowExternalId, iFlowTechnicalName),
                (url, token, restTemplateWrapper) -> deployArtifact(
                    requestContext.getConnectionProperties(),
                    packageExternalId,
                    IFLOW,
                    url,
                    token,
                    restTemplateWrapper.getRestTemplate()
                )
        );
    }

    public void setTraceLogLevelForIFlows(RequestContext commonClientWrapperEntity, List<String> iFlows) {
        log.debug("#setTraceLogLevelForIFlows(RequestContext commonClientWrapperEntity, List<String> iFlows): {}, {}",
            commonClientWrapperEntity, iFlows);

        executeMethod(
            commonClientWrapperEntity,
            API_SET_TRACE_LOG_LEVEL_FOR_IFLOWS,
            (url, token, restTemplateWrapper) -> {
                setTraceLogLevelForIFlows(iFlows, url, token, restTemplateWrapper.getRestTemplate());
                return null;
            }
        );
    }

    public void deleteIFlow(
        String packageExternalId,
        String iFlowExternalId,
        String iFlowName,
        RequestContext requestContext
    ) {
        log.debug("#deleteArtifact(String packageExternalId, String iFlowExternalId, RequestContext requestContext): {}, {}, {}",
            packageExternalId, iFlowExternalId, requestContext);
        deleteArtifact(packageExternalId, iFlowExternalId, iFlowName, requestContext);
    }

    private void setTraceLogLevelForIFlows(
        List<String> iflowTechnicalNames,
        String url,
        String token,
        RestTemplate restTemplate
    ) {

        HttpHeaders setTraceLogLevelRequestHttpHeaders = new HttpHeaders();
        setTraceLogLevelRequestHttpHeaders.set("X-CSRF-Token", token);
        setTraceLogLevelRequestHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

        iflowTechnicalNames.forEach(iflowTechnicalName -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("artifactSymbolicName", iflowTechnicalName);
                request.put("mplLogLevel", "TRACE");
                request.put("nodeType", "IFLMAP");

                HttpEntity<Map<String, String>> setTraceLogLevelRequestHttpEntity = new HttpEntity<>(request, setTraceLogLevelRequestHttpHeaders);

                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        setTraceLogLevelRequestHttpEntity,
                        String.class
                );
            } catch (Exception ex) {
                log.error(
                        String.format(
                                "Error occurred while setting TRACE level for iflow %s: %s",
                                iflowTechnicalName,
                                ex.getMessage()
                        ),
                        ex
                );
            }
        });
    }

}
