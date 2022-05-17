package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import static com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser.buildCpiArtifacts;
import static com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser.retrieveDeployingResult;
import static java.lang.String.format;
import static org.apache.commons.collections4.SetUtils.hashSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Arsenii Istlentev
 * @author Klochkov Sergey
 */
@Slf4j
public abstract class CpiRuntimeArtifactClient extends BaseClient {

    private static final String API_PACKAGES = "/itspaces/odata/1.0/workspace.svc/ContentPackages";
    private static final String API_ARTIFACTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Artifacts?$format=json";
    private static final String API_ARTIFACT_DEPLOY_STATUS = "/itspaces/api/1.0/deploystatus/%s";
    private static final String API_DELETE_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s";
    private static final String API_DOWNLOAD_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s";
    private static final String API_UPDATE_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts";
    private static final String API_LOCK_AND_UNLOCK_ARTIFACT = "itspaces/api/1.0/workspace/{0}/artifacts/{1}";

    private static final String FILE_NAME = "model.zip";
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    protected final IntegrationPackageClient integrationPackageClient;

    CpiRuntimeArtifactClient(IntegrationPackageClient integrationPackageClient, HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        this.integrationPackageClient = integrationPackageClient;
    }

    public String checkDeploymentStatus(RequestContext requestContext, String taskId) {
        String path = String.format(API_ARTIFACT_DEPLOY_STATUS, taskId);
        return executeGet(
            requestContext,
            path,
            CpiRuntimeArtifactParser::retrieveDeployStatus
        );
    }

    protected List<CpiArtifact> getArtifactsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packageExternalId,
            CpiArtifactType artifactType
    ) {
        String path = String.format(API_ARTIFACTS, packageTechnicalName);
        return executeGet(
                requestContext,
                path,
                body -> buildCpiArtifacts(
                        packageTechnicalName,
                        packageDisplayedName,
                        packageExternalId,
                        hashSet(artifactType),
                        body
                )
        );
    }

    protected byte[] downloadArtifact(
            RequestContext requestContext,
            String packageExternalId,
            String artifactExternalId
    ) {
        String path = String.format(API_DOWNLOAD_ARTIFACT, packageExternalId, artifactExternalId, artifactExternalId);
        return executeGet(
                requestContext,
                path,
                resolvedBody -> resolvedBody,
                byte[].class
        );
    }

    protected void updateArtifact(RequestContext requestContext, CreateOrUpdateCpiArtifactRequest request) {
        executeMethod(
                requestContext,
                String.format(API_UPDATE_ARTIFACT, request.getPackageExternalId()),
                (url, token, restTemplateWrapper) -> {
                    uploadArtifact(
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

    protected void deleteArtifact(
        String packageExternalId,
        String artifactExternalId,
        String artifactName,
        RequestContext requestContext
    ) {
        executeMethod(
            requestContext,
            API_PACKAGES,
            format(API_DELETE_ARTIFACT, packageExternalId, artifactExternalId),
            (url, token, restTemplateWrapper) -> {
                deleteArtifact(
                    requestContext.getConnectionProperties(),
                    packageExternalId,
                    artifactName,
                    url,
                    token,
                    restTemplateWrapper.getRestTemplate()
                );
                return null;
            }
        );
    }

    protected void createArtifact(
            ConnectionProperties connectionProperties,
            CreateOrUpdateCpiArtifactRequest request,
            String textBodyAttrName,
            String uploadArtifactUri,
            String userApiCsrfToken,
            RestTemplateWrapper restTemplateWrapper
    ) {

        HttpResponse uploadArtifactResponse = null;
        boolean locked = false;
        String packageExternalId = request.getPackageExternalId();
        try {
            integrationPackageClient.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), true);
            locked = true;

            HttpPost uploadArtifactRequest = new HttpPost(uploadArtifactUri);

            JSONObject requestBody = new JSONObject();
            requestBody.put("id", request.getId());
            requestBody.put("name", request.getName());
            requestBody.put("description", request.getDescription());
            requestBody.put("type", request.getType());
            requestBody.put("additionalAttrs", new JSONObject(request.getAdditionalAttrs()));
            requestBody.put("fileName", FILE_NAME);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("payload", request.getBundledModel(), ContentType.DEFAULT_BINARY, FILE_NAME);
            entityBuilder.addTextBody("_charset_", "UTF-8");
            entityBuilder.addTextBody(textBodyAttrName, requestBody.toString(), ContentType.APPLICATION_JSON);

            org.apache.http.HttpEntity entity = entityBuilder.build();
            uploadArtifactRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
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
                integrationPackageClient.unlockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            }
        }

    }

    protected String deployArtifact(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        CpiArtifactType objectType,
        String deployArtifactUri,
        String userApiCsrfToken,
        RestTemplate restTemplate
    ) {
        boolean locked = false;
        try {
            integrationPackageClient.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplate, true);
            locked = true;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);

            HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                deployArtifactUri,
                HttpMethod.PUT,
                httpEntity,
                String.class
            );

            if (OK.equals(responseEntity.getStatusCode())) {
                String result = responseEntity.getBody();
                return retrieveDeployingResult(result, objectType);
            } else {
                throw new ClientIntegrationException(
                    String.format("Couldn't execute Artifact deployment:%n Code: %d, Message: %s",
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

    private void deleteArtifact(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactName,
        String url,
        String token,
        RestTemplate restTemplate
    ) {
        boolean locked = false;
        try {
            integrationPackageClient.lockPackage(connectionProperties, packageExternalId, token, restTemplate, true);
            locked = true;

            HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
            HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, DELETE, httpEntity, String.class);

            if (!OK.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(format(
                    "Couldn't delete artifact %s: Code: %d, Message: %s",
                    artifactName,
                    responseEntity.getStatusCode().value(),
                    responseEntity.getBody())
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while deleting artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while deleting artifact: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                integrationPackageClient.unlockPackage(connectionProperties, packageExternalId, token, restTemplate);
            }
        }

    }

    private void uploadArtifact(
            ConnectionProperties connectionProperties,
            CreateOrUpdateCpiArtifactRequest request,
            String uploadArtifactUri,
            String userApiCsrfToken,
            RestTemplateWrapper restTemplateWrapper
    ) {
        HttpResponse uploadArtifactResponse = null;
        boolean locked = false;
        String packageExternalId = request.getPackageExternalId();
        String artifactExternalId = request.getId();
        try {
            lockOrUnlockArtifact(connectionProperties, packageExternalId, artifactExternalId, "LOCK", true, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            try {
                lockOrUnlockArtifact(connectionProperties, packageExternalId, artifactExternalId, "LOCK", false, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            } catch (HttpClientErrorException ex) {
                if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                    log.warn("artifact {} is already locked", artifactExternalId);
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
            requestBody.put("fileName", FILE_NAME);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("simpleUploader", request.getBundledModel(), ContentType.DEFAULT_BINARY, FILE_NAME);
            entityBuilder.addTextBody("_charset_", "UTF-8");
            entityBuilder.addTextBody("simpleUploader-data", requestBody.toString(), ContentType.APPLICATION_JSON);

            org.apache.http.HttpEntity entity = entityBuilder.build();
            uploadArtifactRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            uploadArtifactRequest.setEntity(entity);

            HttpClient client = restTemplateWrapper.getHttpClient();

            uploadArtifactResponse = client.execute(uploadArtifactRequest);

            if (uploadArtifactResponse.getStatusLine().getStatusCode() == 201) {
                JSONObject jsonObject = new JSONObject(IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
                if (!request.isUploadDraftVersion()) {
                    setVersionToArtifact(
                        connectionProperties,
                        packageExternalId,
                        artifactExternalId,
                        !isBlank(request.getNewArtifactVersion()) ? request.getNewArtifactVersion() : jsonObject.getString("bundleVersion"),
                        userApiCsrfToken,
                        request.getComment(),
                        restTemplateWrapper.getRestTemplate()
                    );
                }
            } else {
                throw new RuntimeException("Couldn't execute artifact uploading:\n" + IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            }

        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            if (uploadArtifactResponse != null) {
                HttpClientUtils.closeQuietly(uploadArtifactResponse);
            }
            if (locked) {
                lockOrUnlockArtifact(connectionProperties, packageExternalId, artifactExternalId, "UNLOCK", false, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            }
        }
    }

    private void lockOrUnlockArtifact(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String webdav,
        boolean lockinfo,
        String userApiCsrfToken,
        RestTemplate restTemplate
    ) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(API_LOCK_AND_UNLOCK_ARTIFACT);
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", "true");
        }
        uriBuilder.queryParam("webdav", webdav);

        if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
            uriBuilder.port(connectionProperties.getPort());
        }

        URI lockOrUnlockArtifactUri = uriBuilder
                .buildAndExpand(packageExternalId, artifactExternalId)
                .encode()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                lockOrUnlockArtifactUri,
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        if (!OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("Couldn't lock or unlock artifact\n" + responseEntity.getBody());
        }

    }

    private void setVersionToArtifact(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String version,
        String userApiCsrfToken,
        String comment,
        RestTemplate restTemplate
    ) {
        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(API_LOCK_AND_UNLOCK_ARTIFACT)
                    .queryParam("notifications", "true")
                    .queryParam("webdav", "CHECKIN");

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
                uriBuilder.port(connectionProperties.getPort());
            }

            URI lockOrUnlockArtifactUri = uriBuilder
                    .buildAndExpand(packageExternalId, artifactExternalId)
                    .encode()
                    .toUri();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("comment", !isBlank(comment) ? comment : "");
            requestBody.put("semanticVersion", version);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    lockOrUnlockArtifactUri,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            if (!OK.equals(responseEntity.getStatusCode())) {
                throw new RuntimeException("Couldn't set version to Artifact:\n" + responseEntity.getBody());

            }

        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while setting version Artifact: " + ex.getMessage(), ex);
        }
    }

}
