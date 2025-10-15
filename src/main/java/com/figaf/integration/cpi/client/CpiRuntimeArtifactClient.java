package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.AdditionalAttributes;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateCpiArtifactRequest;
import com.figaf.integration.cpi.entity.lock.Locker;
import com.figaf.integration.cpi.entity.runtime_artifacts.VersionHistoryRecord;
import com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser;
import com.figaf.integration.cpi.version.CpiObjectVersionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.figaf.integration.cpi.response_parser.CpiRuntimeArtifactParser.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.SetUtils.hashSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Arsenii Istlentev
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiRuntimeArtifactClient extends CpiBaseClient {

    private static final String API_PACKAGES = "/itspaces/odata/1.0/workspace.svc/ContentPackages";
    private static final String API_ARTIFACTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Artifacts?$format=json";
    private static final String API_ARTIFACT_DEPLOY_STATUS = "/itspaces/api/1.0/deploystatus/%s";
    private static final String API_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s";
    private static final String API_DOWNLOAD_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s";
    private static final String API_UPDATE_ARTIFACT = "/itspaces/api/1.0/workspace/%s/artifacts";
    private static final String API_VERSION_HISTORY_ARTIFACT = "/api/1.0/workspace/%s/artifacts/%s?versionhistory=true&webdav=REPORT";
    private static final String API_LOCK_AND_UNLOCK_ARTIFACT = "itspaces/api/1.0/workspace/{0}/artifacts/{1}";
    private static final String FILE_NAME = "model.zip";
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public CpiRuntimeArtifactClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    @Override
    protected Logger getLogger() {
        return log;
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
        String path = format(API_ARTIFACTS, packageTechnicalName);
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

    public AdditionalAttributes getArtifactAdditionalAttributes(
        String packageExternalId,
        String artifactExternalId,
        RequestContext requestContext
    ) {
        String path = format(API_ARTIFACT, packageExternalId, artifactExternalId);
        return executeGet(
            requestContext,
            path,
            CpiRuntimeArtifactParser::retrieveAdditionalAttributes
        );
    }

    public byte[] downloadArtifact(
        RequestContext requestContext,
        String packageExternalId,
        String artifactExternalId
    ) {
        try {
            String path = String.format(API_DOWNLOAD_ARTIFACT, packageExternalId, artifactExternalId, artifactExternalId);
            return executeGet(
                requestContext,
                path,
                resolvedBody -> resolvedBody,
                byte[].class
            );
        } catch (Exception ex) {
            String errorMessage = "Could not download artifact";
            if (ex.getMessage().contains("\"errorCode\":\"ARTIFACT_ACCESS_DENIED_DOWNLOAD\"")) {
                errorMessage += " because user does not have access to it. Most likely access is restricted by Access Policies";
            }
            throw new ClientIntegrationException(errorMessage, ex);
        }
    }

    public void updateArtifact(RequestContext requestContext, CreateOrUpdateCpiArtifactRequest createOrUpdateCpiArtifactRequest) {
        log.debug("#updateArtifact: requestContext={}, createOrUpdateCpiArtifactRequest={}", requestContext, createOrUpdateCpiArtifactRequest);
        executeMethod(
            requestContext,
            String.format(API_UPDATE_ARTIFACT, createOrUpdateCpiArtifactRequest.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> {
                uploadArtifact(
                    requestContext.getConnectionProperties(),
                    createOrUpdateCpiArtifactRequest,
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );
    }

    public List<VersionHistoryRecord> getArtifactVersionsHistory(RequestContext requestContext, String packageExternalId, String externalId) {
        String path;
        if (isIntegrationSuiteHost(requestContext.getConnectionProperties().getHost())) {
            path = String.format(API_VERSION_HISTORY_ARTIFACT, packageExternalId, externalId);
        } else {
            path = "/itspaces" + String.format(API_VERSION_HISTORY_ARTIFACT, packageExternalId, externalId);
        }
        return executeMethod(
            requestContext,
            path,
            (url, token, restTemplateWrapper) -> getArtifactVersionsHistory(url, token, restTemplateWrapper.getRestTemplate())
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
            format(API_ARTIFACT, packageExternalId, artifactExternalId),
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
        boolean locked = false;
        String packageExternalId = request.getPackageExternalId();
        try {
            Locker.lockPackage(connectionProperties, packageExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate());
            locked = true;

            HttpPost uploadArtifactRequest = new HttpPost(uploadArtifactUri);

            JSONObject requestBody = new JSONObject();
            requestBody.put("id", request.getId());
            requestBody.put("name", request.getName());
            requestBody.put("packageId", request.getPackageTechnicalName());
            //description property is mandatory for artifact creation but null value is not allowed for Value Mappings
            requestBody.put("description", StringUtils.defaultString(request.getDescription()));
            requestBody.put("type", request.getType());
            requestBody.put("additionalAttrs", new JSONObject(request.getAdditionalAttrs()));
            uploadArtifactRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            if (request.getBundledModel() != null && request.getBundledModel().length > 0) {
                requestBody.put("fileName", FILE_NAME);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.LEGACY)
                    .addBinaryBody("payload", request.getBundledModel(), ContentType.DEFAULT_BINARY, FILE_NAME)
                    .addTextBody("_charset_", "UTF-8", ContentType.TEXT_PLAIN)
                    .addTextBody(textBodyAttrName, requestBody.toString(), ContentType.APPLICATION_JSON);
                uploadArtifactRequest.setEntity(entityBuilder.build());
            } else {
                StringEntity jsonEntity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
                uploadArtifactRequest.setEntity(jsonEntity);
            }

            HttpClient client = restTemplateWrapper.getHttpClient();

            client.execute(uploadArtifactRequest, uploadArtifactResponse -> {
                checkArtifactUploadStatusCodeAndThrowErrorIfNotSuccessful(uploadArtifactResponse);
                return null;
            });
        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
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
                    String.format("Couldn't execute artifact deployment:%n Code: %d, Message: %s",
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

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.LEGACY)
                .addBinaryBody("simpleUploader", request.getBundledModel(), ContentType.DEFAULT_BINARY, FILE_NAME)
                .addTextBody("_charset_", "UTF-8")
                .addTextBody("simpleUploader-data", requestBody.toString(), ContentType.APPLICATION_JSON);

            uploadArtifactRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            uploadArtifactRequest.setEntity(entityBuilder.build());

            HttpClient client = restTemplateWrapper.getHttpClient();

            client.execute(uploadArtifactRequest, uploadArtifactResponse -> {
                checkArtifactUploadStatusCodeAndThrowErrorIfNotSuccessful(uploadArtifactResponse);

                JSONObject jsonObject = new JSONObject(IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), UTF_8));
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
                return null;
            });
        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while uploading artifact " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading artifact: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                Locker.unlockCpiObject(connectionProperties, packageExternalId, artifactExternalId, userApiCsrfToken, restTemplateWrapper.getRestTemplate(), API_LOCK_AND_UNLOCK_ARTIFACT);
            }
        }
    }

    private void checkArtifactUploadStatusCodeAndThrowErrorIfNotSuccessful(ClassicHttpResponse uploadArtifactResponse) throws IOException {
        if (List.of(200,201).contains(uploadArtifactResponse.getCode())) {
            return;
        }

        String responseBody = IOUtils.toString(uploadArtifactResponse.getEntity().getContent(), UTF_8);
        String errorMsg;
        if (uploadArtifactResponse.getCode() == 403 && StringUtils.isBlank(responseBody)) {
            errorMsg = format("Couldn't execute artifact upload successfully. API responded with status code %d, " +
                    "but 200 or 201 was expected.%nResponse body is empty. The problem may be related to access policies configured for IFlow. " +
                    "Check if Figaf application user that executes request (S-user / P-user / role assignment in Custom IdP) has enough access permissions. " +
                    "If access policy roles had been added recently, wait for ~10 min and then try again. Recreate the session if it's cached: " +
                    "in Figaf Tool execute `Reset http client forcibly` from CPI agent page.",
                uploadArtifactResponse.getCode()
            );
        } else {
            errorMsg = format("Couldn't execute artifact upload successfully. API responded with status code %d, but 200 or 201 was expected.%nResponse body: %s",
                uploadArtifactResponse.getCode(),
                responseBody
            );
        }
        throw new ClientIntegrationException(errorMsg);
    }

    //we use this method as a reading operation although in SAP is a PUT operation
    private List<VersionHistoryRecord> getArtifactVersionsHistory(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                TypeReference<List<VersionHistoryRecord>> versionHistoryRecordsRef = new TypeReference<>() {
                };
                return ObjectMapperFactory.getJsonObjectMapper().readValue(responseEntity.getBody(), versionHistoryRecordsRef);
            } else {
                throw new ClientIntegrationException(format(
                    "Couldn't execute versions history. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while executing versions history: " + ex.getMessage(), ex);
        }
    }
}
