package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.CloudPlatformType;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.DeleteAndUndeployIFlowResult;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import com.figaf.integration.cpi.entity.lock.Locker;
import com.figaf.integration.cpi.version.CpiObjectVersionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;
import static com.figaf.integration.cpi.utils.CpiApiUtils.loadXMLFromString;
import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiIntegrationFlowClient extends CpiRuntimeArtifactClient {

    private static final String API_UPLOAD_IFLOW = "/itspaces/api/1.0/workspace/%s/iflows/";
    private static final String API_DEPLOY_IFLOW = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY";
    private static final String API_NORMALIZE_IFLOW = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s";
    private static final String API_SET_TRACE_LOG_LEVEL_FOR_IFLOWS = "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentSetMplLogLevelCommand";

    public CpiIntegrationFlowClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
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

    public String createIFlow(RequestContext requestContext, CreateIFlowRequest request) {
        log.debug("#createIFlow(RequestContext requestContext, CreateIFlowRequest request): {}, {}", requestContext, request);
        return executeMethod(
            requestContext,
            String.format(API_UPLOAD_IFLOW, request.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> createArtifact(
                requestContext.getConnectionProperties(),
                request,
                "iflowBrowse-data",
                url,
                token,
                restTemplateWrapper
            )
        );
    }

    public void normalizeUsingCpiJsonModel(
        RequestContext requestContext,
        NormalizeIFlowRequest request
    ) {
        executeMethod(
            requestContext,
            String.format(API_NORMALIZE_IFLOW, request.getPackageExternalId(), request.getIflowExternalId(), request.getIflowExternalId(), request.getIflowTechnicalName()),
            (url, token, restTemplateWrapper) -> {
                normalizeUsingCpiJsonModel(
                    requestContext.getConnectionProperties(),
                    request,
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );
    }

    public void updateIFlow(RequestContext requestContext, UpdateIFlowRequest request) {
        log.debug("#updateIFlow(RequestContext requestContext, UpdateIFlowRequest request): {}, {}", requestContext, request);
        updateArtifact(requestContext, request);
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

    public String deployIFlowViaPublicApi(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deployIFlowViaPublicApi(RequestContext requestContext, String iFlowTechnicalName): {}, {}", requestContext, iFlowTechnicalName);
        return executeMethodPublicApi(
            requestContext,
            format("/api/v1/DeployIntegrationDesigntimeArtifact?Id='%s'&Version='active'", iFlowTechnicalName),
            null,
            HttpMethod.POST,
            HttpEntity::getBody
        );
    }

    public boolean undeployIFlow(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#undeployIFlow(RequestContext requestContext, String iFlowTechnicalName): {}, {}", requestContext, iFlowTechnicalName);
        return executeDeletePublicApi(
            requestContext,
            format("/api/v1/IntegrationRuntimeArtifacts('%s')", iFlowTechnicalName),
            Objects::nonNull
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

    public boolean deleteIFlow(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deleteIFlow(RequestContext requestContext, String iFlowTechnicalName): {}, {}", iFlowTechnicalName, requestContext);
        return executeDeletePublicApi(
            requestContext,
            format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')", iFlowTechnicalName),
            Objects::nonNull
        );
    }

    public CpiArtifactFromPublicApi getIFlowByTechnicalName(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#getIFlowByTechnicalName(RequestContext requestContext, String iFlowTechnicalName): {}, {}", iFlowTechnicalName, requestContext);
        return executeMethodPublicApi(
            requestContext,
            format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')", iFlowTechnicalName),
            "",
            HttpMethod.GET,
            responseEntity -> parseCpiArtifact(responseEntity.getBody())
        );
    }

    private CpiArtifactFromPublicApi parseCpiArtifact(String xml) {
        Document document = loadXMLFromString(xml);
        NodeList entries = document.getElementsByTagName("m:properties");

        NodeList propertiesList = entries.item(0).getChildNodes();
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < propertiesList.getLength(); i++) {
            Node propertyNode = propertiesList.item(i);
            String propertyName = propertyNode.getLocalName();
            String propertyValue = propertyNode.getTextContent();
            properties.put(propertyName, propertyValue);
        }
        CpiArtifactFromPublicApi cpiArtifact = new CpiArtifactFromPublicApi();
        cpiArtifact.setTechnicalName(properties.get("Id"));
        cpiArtifact.setDisplayedName(properties.get("Name"));
        cpiArtifact.setVersion(properties.get("Version"));
        cpiArtifact.setPackageTechnicalName(properties.get("PackageId"));
        cpiArtifact.setDescription(properties.get("Description"));
        cpiArtifact.setSender(properties.get("Sender"));
        cpiArtifact.setReceiver(properties.get("Receiver"));
        cpiArtifact.setCreatedBy(properties.get("CreatedBy"));
        cpiArtifact.setCreatedAt(properties.get("CreatedAt"));
        cpiArtifact.setModifiedBy(properties.get("ModifiedBy"));
        cpiArtifact.setModifiedAt(properties.get("ModifiedAt"));
        return cpiArtifact;
    }

    public DeleteAndUndeployIFlowResult deleteAndUndeployIFlow(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deleteAndUndeployIFlow(RequestContext requestContext, String iFlowTechnicalName): {}, {}", requestContext, iFlowTechnicalName);
        DeleteAndUndeployIFlowResult deleteAndUndeployIFlowResult = new DeleteAndUndeployIFlowResult(iFlowTechnicalName);

        boolean deletedSuccessfully = deleteIFlow(requestContext, iFlowTechnicalName);
        deleteAndUndeployIFlowResult.setDeleted(deletedSuccessfully);

        //It doesn't work for NEO due to a SAP bug
        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            boolean undeployedSuccessfully = undeployIFlow(requestContext, iFlowTechnicalName);
            deleteAndUndeployIFlowResult.setUndeployed(undeployedSuccessfully);
        }

        return deleteAndUndeployIFlowResult;
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

    private void normalizeUsingCpiJsonModel(
        ConnectionProperties connectionProperties,
        NormalizeIFlowRequest request,
        String normalizeIFlowUrl,
        String userApiCsrfToken,
        RestTemplateWrapper restTemplateWrapper
    ) {
        String packageExternalId = request.getPackageExternalId();
        String iflowExternalId = request.getIflowExternalId();
        boolean locked = false;
        try {
            Locker.lockCpiObject(connectionProperties, packageExternalId, iflowExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), API_LOCK_AND_UNLOCK_ARTIFACT);
            locked = true;

            HttpClient client = restTemplateWrapper.getHttpClient();

            String contentBody = getCpiJsonModel(client, normalizeIFlowUrl, userApiCsrfToken);
            putCpiJsonModel(client, normalizeIFlowUrl, userApiCsrfToken, contentBody);

            CpiObjectVersionHandler.setVersionToCpiObject(connectionProperties,
                packageExternalId,
                iflowExternalId,
                "1.0.0",
                userApiCsrfToken,
                null,
                restTemplateWrapper.getRestTemplate(),
                API_LOCK_AND_UNLOCK_ARTIFACT
            );

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                Locker.unlockCpiObject(connectionProperties, packageExternalId, iflowExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), API_LOCK_AND_UNLOCK_ARTIFACT);
            }
        }
    }

    private String getCpiJsonModel(HttpClient client, String normalizeIFlowUrl, String userApiCsrfToken) throws IOException {
        HttpResponse httpResponse = null;
        try {
            HttpGet uploadArtifactRequest = new HttpGet(normalizeIFlowUrl);
            uploadArtifactRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            httpResponse = client.execute(uploadArtifactRequest);
            String jsonBody = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(format("Couldn't get CPI IFlow JSON model: code: %d, body: %s", httpResponse.getStatusLine().getStatusCode(), jsonBody));
            }
            return jsonBody;
        } finally {
            if (httpResponse != null) {
                HttpClientUtils.closeQuietly(httpResponse);
            }
        }
    }

    private void putCpiJsonModel(HttpClient client, String normalizeIFlowUrl, String userApiCsrfToken, String cpiIflowJsonModel) throws IOException {
        HttpResponse httpResponse = null;
        try {
            HttpPut httpPut = new HttpPut(normalizeIFlowUrl);
            httpPut.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            org.apache.http.HttpEntity httpEntity = new StringEntity(cpiIflowJsonModel);
            httpPut.setEntity(httpEntity);
            httpResponse = client.execute(httpPut);
            String putResponseContentBody = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(format("Couldn't update CPI IFlow JSON model: code: %d, body: %s", httpResponse.getStatusLine().getStatusCode(), putResponseContentBody));
            }
        } finally {
            if (httpResponse != null) {
                HttpClientUtils.closeQuietly(httpResponse);
            }
        }

    }

}
