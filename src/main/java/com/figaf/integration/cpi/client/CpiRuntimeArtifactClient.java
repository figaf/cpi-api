package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import com.figaf.integration.cpi.entity.lock.Locker;
import com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser;
import com.figaf.integration.cpi.version.CpiObjectVersionHandler;
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
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
public class CpiRuntimeArtifactClient extends BaseClient {

    private static final String API_PACKAGES = "/itspaces/odata/1.0/workspace.svc/ContentPackages";
    private static final String API_ARTIFACTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Artifacts?$format=json";
    private static final String API_ARTIFACT_DEPLOY_STATUS = "/itspaces/api/1.0/deploystatus/%s";
    private static final String API_DELETE_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s";
    private static final String API_DOWNLOAD_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s";
    private static final String API_UPDATE_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts";
    private static final String API_LOCK_AND_UNLOCK_ARTIFACT = "itspaces/api/1.0/workspace/{0}/artifacts/{1}";
    private static final String FILE_NAME = "model.zip";
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public CpiRuntimeArtifactClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public String checkDeploymentStatus(RequestContext requestContext, String taskId) {
        String path = String.format(API_ARTIFACT_DEPLOY_STATUS, taskId);
        return executeGet(
            requestContext,
            path,
            CpiRuntimeArtifactParser::retrieveDeployStatus
        );
    }

    public List<CpiArtifact> getArtifactsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            Collections.emptySet()
        );
    }

    public List<CpiArtifact> getArtifactsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        CpiArtifactType artifactType
    ) {
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            hashSet(artifactType)
        );
    }

    protected List<CpiArtifact> getArtifactsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        Set<CpiArtifactType> artifactTypes
    ) {
        String path = String.format(API_ARTIFACTS, packageTechnicalName);
        return executeGet(
            requestContext,
            path,
            body -> buildCpiArtifacts(
                packageTechnicalName,
                packageDisplayedName,
                packageExternalId,
                artifactTypes,
                body
            )
        );
    }

    public byte[] downloadArtifact(
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

    public void updateArtifact(RequestContext requestContext, CreateOrUpdateCpiArtifactRequest request) {
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

    public void deleteArtifact(
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
            Locker.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
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

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(uploadArtifactResponse);
            if (locked) {
                Locker.unlockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
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
            Locker.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplate);
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
                Locker.unlockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplate);
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
            Locker.lockPackage(connectionProperties, packageExternalId, token, restTemplate);
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

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while deleting artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while deleting artifact: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                Locker.unlockPackage(connectionProperties, packageExternalId, token, restTemplate);
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
            Locker.lockCpiObject(connectionProperties, packageExternalId, artifactExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), API_LOCK_AND_UNLOCK_ARTIFACT);
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
                    CpiObjectVersionHandler.setVersionToCpiObject(connectionProperties,
                        packageExternalId,
                        artifactExternalId,
                        !isBlank(request.getNewArtifactVersion()) ? request.getNewArtifactVersion() : jsonObject.getString("bundleVersion"),
                        userApiCsrfToken,
                        request.getComment(),
                        restTemplateWrapper.getRestTemplate(),
                        API_LOCK_AND_UNLOCK_ARTIFACT
                    );
                }
            } else {
                throw new RuntimeException("Couldn't execute artifact uploading:\n" + IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            }

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            if (uploadArtifactResponse != null) {
                HttpClientUtils.closeQuietly(uploadArtifactResponse);
            }
            if (locked) {
                Locker.unlockCpiObject(connectionProperties, packageExternalId, artifactExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), API_LOCK_AND_UNLOCK_ARTIFACT);
            }
        }
    }


}
