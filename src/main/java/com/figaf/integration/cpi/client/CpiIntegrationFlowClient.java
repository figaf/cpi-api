package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.wrapper.CommonClientWrapper;
import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateCpiArtifactRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateIFlowRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateValueMappingRequest;
import com.figaf.integration.cpi.response_parser.CpiIntegrationFlowParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class CpiIntegrationFlowClient extends CommonClientWrapper {

    private static final String API_ARTIFACTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Artifacts?$format=json";
    private static final String API_IFLOW_DEPLOY_STATUS = "/itspaces/api/1.0/deploystatus/%s";

    private final IntegrationPackageClient integrationPackageClient;

    public CpiIntegrationFlowClient(String ssoUrl, IntegrationPackageClient integrationPackageClient) {
        super(ssoUrl);
        this.integrationPackageClient = integrationPackageClient;
    }

    public List<CpiArtifact> getArtifactsByPackage(
        CommonClientWrapperEntity commonClientWrapperEntity,
        String packageTechnicalName,
        String packageDisplayedName,
        String packagePackageExternalId,
        Set<String> artifactTypes
    ) {
        log.debug("#getArtifactsByPackage(CommonClientWrapperEntity commonClientWrapperEntity, String packageTechnicalName, String packageDisplayedName, String packagePackageExternalId, Set<TrackedObjectType> artifactTypes): " +
            "{}, {}, {}, {}, {}", commonClientWrapperEntity, packageTechnicalName, packageDisplayedName, packagePackageExternalId, artifactTypes);
        String path = String.format(API_ARTIFACTS, packageTechnicalName);
        return executeGet(
            commonClientWrapperEntity,
            path,
            (body) -> CpiIntegrationFlowParser.buildCpiArtifacts(
                packageTechnicalName,
                packageDisplayedName,
                packagePackageExternalId,
                artifactTypes,
                body
            )
        );
    }

    public String checkDeployStatus(CommonClientWrapperEntity commonClientWrapperEntity, String taskId) {
        log.debug("checkDeployStatus(CommonClientWrapperEntity commonClientWrapperEntity, String taskId): {}, {}", commonClientWrapperEntity, taskId);
        String path = String.format(API_IFLOW_DEPLOY_STATUS, taskId);
        return executeGet(
            commonClientWrapperEntity,
            path,
            CpiIntegrationFlowParser::retrieveDeployStatus
        );
    }

    public byte[] downloadArtifact(
        CommonClientWrapperEntity commonClientWrapperEntity,
        String externalPackageId,
        String externalArtifactId) {
        log.debug("#downloadArtifact(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, String externalArtifactId): {}, {}, {}",
            commonClientWrapperEntity, externalPackageId, externalArtifactId
        );
        String path = String.format("/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s", externalPackageId, externalArtifactId, externalArtifactId);
        return executeGet(
            commonClientWrapperEntity,
            path,
            resolvedBody -> resolvedBody,
            byte[].class
        );
    }

    public void createIntegrationFlow(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, CreateOrUpdateIFlowRequest request, byte[] bundledModel) {
        log.debug("#updateIntegrationFlow(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, CreateIFlowRequest request, " +
            "byte[] bundledModel): {}, {}, {}", commonClientWrapperEntity, externalPackageId, request);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);

        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, String.format("/itspaces/api/1.0/workspace/%s/iflows/", externalPackageId));

        createArtifact(commonClientWrapperEntity.getConnectionProperties(), externalPackageId, request, bundledModel, "iflowBrowse-data", url, restTemplateWrapper, token);

    }

    public void createValueMapping(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, CreateOrUpdateValueMappingRequest request, byte[] model) {
        log.debug("#createValueMapping(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, CreateValueMappingRequest request, byte[] model): " +
            "{}, {}, {}", commonClientWrapperEntity, externalPackageId, request);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, String.format("/itspaces/api/1.0/workspace/%s/valuemappings/", externalPackageId));

        createArtifact(commonClientWrapperEntity.getConnectionProperties(), externalPackageId, request, model, "vmBrowse-data", url, restTemplateWrapper, token);
    }

    public void updateArtifact(
            CommonClientWrapperEntity commonClientWrapperEntity,
            String externalPackageId,
            String externalArtifactId,
            CreateOrUpdateCpiArtifactRequest request,
            byte[] bundledModel
    ) {
        updateArtifact(commonClientWrapperEntity, externalPackageId, externalArtifactId, request, bundledModel, false, null);
    }

    public void updateArtifact(
            CommonClientWrapperEntity commonClientWrapperEntity,
            String externalPackageId,
            String externalArtifactId,
            CreateOrUpdateCpiArtifactRequest request,
            byte[] bundledModel,
            boolean uploadDraftVersion,
            String newIflowVersion
    ) {
        log.debug("#updateIntegrationFlow(CommonClientWrapperEntity commonClientWrapperEntity, String externalPackageId, String externalArtifactId, UpdateIFlowRequest request, byte[] bundledModel): {}, {}, {}, {}", commonClientWrapperEntity, externalPackageId, externalArtifactId, request);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, String.format("/itspaces/api/1.0/workspace/%s/artifacts", externalPackageId));

        uploadArtifact(commonClientWrapperEntity.getConnectionProperties(), externalPackageId, externalArtifactId, request, bundledModel, url, restTemplateWrapper, token, uploadDraftVersion, newIflowVersion);
    }

    public String deployIFlow(CommonClientWrapperEntity commonClientWrapperEntity, String packageExternalId, String iFlowExternalId, String iFlowTechnicalName) {
        log.debug("#deployIFlow(CommonClientWrapperEntity commonClientWrapperEntity, String packageExternalId, String iFlowExternalId, String iFlowTechnicalName): {}, {}, {}, {}",
            commonClientWrapperEntity, packageExternalId, iFlowExternalId, iFlowTechnicalName
        );

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, String.format("/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY", packageExternalId, iFlowExternalId, iFlowExternalId, iFlowTechnicalName));

        return deployArtifact(commonClientWrapperEntity.getConnectionProperties(), packageExternalId, url, "CPI_IFLOW", restTemplateWrapper.getRestTemplate(), token);
    }

    public String deployValueMapping(CommonClientWrapperEntity commonClientWrapperEntity, String packageExternalId, String valueMappingExternalId) {
        log.debug("#deployValueMapping(CommonClientWrapperEntity commonClientWrapperEntity, String packageExternalId, String valueMappingExternalId): {}, {}, {}",
            commonClientWrapperEntity, packageExternalId, valueMappingExternalId
        );

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, String.format("/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/valuemappings/%s?webdav=DEPLOY", "undefined", valueMappingExternalId, valueMappingExternalId, valueMappingExternalId));

       return deployArtifact(commonClientWrapperEntity.getConnectionProperties(), packageExternalId, url, "VALUE_MAPPING", restTemplateWrapper.getRestTemplate(), token);

    }

    public void setTraceLogLevelForIFlows(CommonClientWrapperEntity commonClientWrapperEntity, List<String> iFlows) {
        log.debug("#setTraceLogLevelForIFlows(CommonClientWrapperEntity commonClientWrapperEntity, List<String> iFlows): {}, {}", commonClientWrapperEntity, iFlows);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        String url = buildUrl(commonClientWrapperEntity, "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentSetMplLogLevelCommand");

        setTraceLogLevelForIFlows(iFlows, url, token, restTemplateWrapper.getRestTemplate());
    }


    private void createArtifact(
            ConnectionProperties connectionProperties,
            String externalPackageId,
            CreateOrUpdateCpiArtifactRequest request,
            byte[] model,
            String textBodyAttrName,
            String uploadArtifactUri,
            RestTemplateWrapper restTemplateWrapper,
            String userApiCsrfToken
    ) {

        HttpResponse uploadArtifactResponse = null;
        boolean locked = false;
        try {
            integrationPackageClient.lockPackage(connectionProperties, externalPackageId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), true);
            locked = true;

            HttpPost uploadArtifactRequest = new HttpPost(uploadArtifactUri);

            JSONObject requestBody = new JSONObject();
            requestBody.put("id", request.getId());
            requestBody.put("name", request.getName());
            requestBody.put("description", request.getDescription());
            requestBody.put("type", request.getType());
            requestBody.put("additionalAttrs", new JSONObject(request.getAdditionalAttrs()));
            requestBody.put("fileName", "model.zip");

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("payload", model, ContentType.DEFAULT_BINARY, "model.zip");
            entityBuilder.addTextBody("_charset_", "UTF-8");
            entityBuilder.addTextBody(textBodyAttrName, requestBody.toString(), ContentType.APPLICATION_JSON);

            org.apache.http.HttpEntity entity = entityBuilder.build();
            uploadArtifactRequest.setHeader("X-CSRF-Token", userApiCsrfToken);
            uploadArtifactRequest.setEntity(entity);

            HttpClient client = restTemplateWrapper.getHttpClient();

            uploadArtifactResponse = client.execute(uploadArtifactRequest);

            if (uploadArtifactResponse.getStatusLine().getStatusCode() != 201) {
                throw new ClientIntegrationException("Couldn't execute artifact uploading:\n" + IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            }

        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(uploadArtifactResponse);
            if (locked) {
                integrationPackageClient.unlockPackage(connectionProperties, externalPackageId, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            }
        }

    }

    private void uploadArtifact(
            ConnectionProperties connectionProperties,
            String externalPackageId,
            String externalArtifactId,
            CreateOrUpdateCpiArtifactRequest request,
            byte[] bundledModel,
            String uploadArtifactUri,
            RestTemplateWrapper restTemplateWrapper,
            String userApiCsrfToken,
            boolean uploadDraftVersion,
            String newIflowVersion
    ) {
        HttpResponse uploadArtifactResponse = null;
        boolean locked = false;
        try {
            lockOrUnlockArtifact(connectionProperties, externalPackageId, externalArtifactId, restTemplateWrapper.getRestTemplate(), userApiCsrfToken, "LOCK", true);
            try {
                lockOrUnlockArtifact(connectionProperties, externalPackageId, externalArtifactId, restTemplateWrapper.getRestTemplate(), userApiCsrfToken, "LOCK", false);
            } catch (HttpClientErrorException ex) {
                if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                    log.warn("artifact {} is already locked", externalArtifactId);
                } else {
                    throw new ClientIntegrationException("Couldn't lock or unlock artifact\n" + ex.getResponseBodyAsString());
                }
            }
            locked = true;

            HttpPost uploadArtifactRequest = new HttpPost(uploadArtifactUri);

            JSONObject requestBody = new JSONObject();
            requestBody.put("id", request.getId());
            requestBody.put("entityID", request.getId());
            requestBody.put("name", request.getName());
            if (request.getDescription() != null) {
                requestBody.put("description", request.getDescription());
            }
            requestBody.put("type", request.getType());
            requestBody.put("additionalAttrs", new JSONObject(request.getAdditionalAttrs()));
            requestBody.put("fileName", "model.zip");

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("simpleUploader", bundledModel, ContentType.DEFAULT_BINARY, "model.zip");
            entityBuilder.addTextBody("_charset_", "UTF-8");
            entityBuilder.addTextBody("simpleUploader-data", requestBody.toString(), ContentType.APPLICATION_JSON);

            org.apache.http.HttpEntity entity = entityBuilder.build();
            uploadArtifactRequest.setHeader("X-CSRF-Token", userApiCsrfToken);
            uploadArtifactRequest.setEntity(entity);

            HttpClient client = restTemplateWrapper.getHttpClient();

            uploadArtifactResponse = client.execute(uploadArtifactRequest);

            if (uploadArtifactResponse.getStatusLine().getStatusCode() == 201) {
                JSONObject jsonObject = new JSONObject(IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
                if (!uploadDraftVersion) {
                    setVersionToArtifact(
                            connectionProperties,
                            externalPackageId,
                            externalArtifactId,
                            restTemplateWrapper.getRestTemplate(),
                            userApiCsrfToken,
                            newIflowVersion != null ? newIflowVersion : jsonObject.getString("bundleVersion")
                    );
                }
            } else {
                throw new RuntimeException("Couldn't execute artifact uploading:\n" + IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            }

        } catch (Exception ex) {
            log.error("Error occurred while uploading value mapping " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading value mapping: " + ex.getMessage(), ex);
        } finally {
            if (uploadArtifactResponse != null) {
                HttpClientUtils.closeQuietly(uploadArtifactResponse);
            }
            if (locked) {
                lockOrUnlockArtifact(connectionProperties, externalPackageId, externalArtifactId, restTemplateWrapper.getRestTemplate(), userApiCsrfToken, "UNLOCK", false);
            }
        }
    }

    private String deployArtifact(ConnectionProperties connectionProperties, String packageExternalId, String deployArtifactUri, String objectType, RestTemplate restTemplate, String userApiCsrfToken) {
        boolean locked = false;
        try {
            integrationPackageClient.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplate, true);
            locked = true;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

            HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    deployArtifactUri,
                    HttpMethod.PUT,
                    httpEntity,
                    String.class
            );

            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                String result = responseEntity.getBody();

                switch (objectType) {
                    case "CPI_IFLOW": {
                        return new JSONObject(result).getString("taskId");
                    }
                    case "VALUE_MAPPING": {
                        return result;
                    }
                    default: {
                        throw new ClientIntegrationException("Unexpected object type: " + objectType);
                    }
                }
            } else {
                throw new ClientIntegrationException(
                        String.format("Couldn't execute Artifact deployment:\n Code: %d, Message: %s",
                                responseEntity.getStatusCode().value(),
                                responseEntity.getBody()
                        )
                );
            }
        } finally {
            if (locked) {
                integrationPackageClient.unlockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplate);
            }
        }
    }

    private void setTraceLogLevelForIFlows(List<String> iflowTechnicalNames, String url, String token, RestTemplate restTemplate) {

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

    private void lockOrUnlockArtifact(ConnectionProperties connectionProperties, String externalPackageId, String artifactExternalId, RestTemplate restTemplate, String userApiCsrfToken, String webdav, boolean lockinfo) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("itspaces/api/1.0/workspace/{0}/artifacts/{1}");
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", "true");
        }
        uriBuilder.queryParam("webdav", webdav);

        if (connectionProperties.getPort() != null) {
            uriBuilder.port(connectionProperties.getPort());
        }

        URI lockOrUnlockArtifactUri = uriBuilder
                .buildAndExpand(externalPackageId, artifactExternalId)
                .encode()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                lockOrUnlockArtifactUri,
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("Couldn't lock or unlock artifact\n" + responseEntity.getBody());
        }

    }

    private void setVersionToArtifact(ConnectionProperties connectionProperties, String externalPackageId, String iflowExternalId, RestTemplate restTemplate, String userApiCsrfToken, String version) {
        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path("itspaces/api/1.0/workspace/{0}/artifacts/{1}")
                    .queryParam("notifications", "true")
                    .queryParam("webdav", "CHECKIN");

            if (connectionProperties.getPort() != null) {
                uriBuilder.port(connectionProperties.getPort());
            }

            URI lockOrUnlockArtifactUri = uriBuilder
                    .buildAndExpand(externalPackageId, iflowExternalId)
                    .encode()
                    .toUri();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("comment", "");
            requestBody.put("semanticVersion", version);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    lockOrUnlockArtifactUri,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new RuntimeException("Couldn't set version to Artifact:\n" + responseEntity.getBody());

            }

        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while setting version Artifact: " + ex.getMessage(), ex);
        }
    }

}
