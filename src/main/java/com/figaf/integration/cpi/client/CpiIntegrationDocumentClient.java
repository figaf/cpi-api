package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.cpi_api_package.document.FileUploadRequest;
import com.figaf.integration.cpi.entity.cpi_api_package.document.UrlUploadRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiIntegrationDocument;
import com.figaf.integration.cpi.response_parser.CpiIntegrationDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class CpiIntegrationDocumentClient extends BaseClient {

    private static final String API_FILE_DOCUMENTS_META_DATA = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Files?$format=json";
    private static final String API_URL_DOCUMENTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Urls?$format=json";
    private static final String API_FILE_DOCUMENT = "/itspaces/odata/1.0/workspace.svc/ContentEntities.Files('%s')/$value?attachment=false";
    private static final String API_DOCUMENT_UPLOAD = "/itspaces/api/1.0/package/%s/documents";
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";
    private static final String API_FILE_DELETE = "/itspaces/odata/1.0/workspace.svc/ContentEntities.Files('%s')";
    private static final String API_URL_DELETE = "/itspaces/odata/1.0/workspace.svc/ContentEntities.Urls('%s')";

    public CpiIntegrationDocumentClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CpiIntegrationDocument> getDocumentsByPackage(
            RequestContext requestContext,
            String packageTechnicalName,
            String packageDisplayedName,
            String packagePackageExternalId,
            String documentType
    ) {
        log.debug("#getDocumentsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, String packagePackageExternalId, TrackedObjectType documentType): " +
                "{}, {}, {}, {}, {}", requestContext, packageTechnicalName, packageDisplayedName, packagePackageExternalId, documentType);

        String path;
        switch (documentType) {
            case "FILE_DOCUMENT":
                path = String.format(API_FILE_DOCUMENTS_META_DATA, packageTechnicalName);
                break;
            case "URL_DOCUMENT":
                path = String.format(API_URL_DOCUMENTS, packageTechnicalName);
                break;
            default:
                throw new IllegalArgumentException("Unexpected object type " + documentType);
        }

        return executeGet(
                requestContext,
                path,
                body -> CpiIntegrationDocumentParser.buildCpiIntegrationDocuments(documentType, body)
        );
    }

    public byte[] downloadFileDocument(RequestContext requestContext, String documentTechnicalName) {
        log.debug("#downloadFileDocument(RequestContext requestContext, String documentTechnicalName): {}, {}", requestContext, documentTechnicalName);
        String path = String.format(API_FILE_DOCUMENT, documentTechnicalName);
        return executeGet(requestContext, path, resolvedBody -> resolvedBody, byte[].class);
    }

    public byte[] downloadUrlDocument(String url) {
        log.debug("#downloadUrlDocument(String url): {}", url);
        return url.getBytes(StandardCharsets.UTF_8);
    }

    public void deleteDocument(RequestContext requestContext, String documentType, String documentTechnicalName) {
        log.debug("#uploadFile(RequestContext requestContext, String documentType, String documentTechnicalName): {}, {} ,{}", requestContext, documentType, documentTechnicalName);
        executeMethod(requestContext,
                getRequestPathPerDocumentType(documentType, documentTechnicalName), (url, token, restTemplateWrapper) -> {
                    deleteDocument(
                            token,
                            url,
                            restTemplateWrapper.getRestTemplate());
                    return null;
                });
    }

    private void deleteDocument(
            String userApiCsrfToken,
            String url,
            RestTemplate restTemplate) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> deleteDocumentResp = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
        if (NO_CONTENT.equals(deleteDocumentResp.getStatusCode())) {
            log.debug(deleteDocumentResp.getBody());
        } else {
            throw new ClientIntegrationException(
                    String.format("Couldn't execute delete document:%n Code: %d, Message: %s",
                            deleteDocumentResp.getStatusCode().value(),
                            deleteDocumentResp.getBody()
                    )
            );
        }
    }

    public void uploadFile(RequestContext requestContext, String packageId, FileUploadRequest fileUploadRequest) {
        log.debug("#uploadFile(RequestContext requestContext, FileDocumentRequest fileDocumentRequest): {}, {}", requestContext, fileUploadRequest);
        executeMethod(requestContext,
                String.format(API_DOCUMENT_UPLOAD, packageId), (url, token, restTemplateWrapper) -> {
                    uploadFile(fileUploadRequest, restTemplateWrapper.getHttpClient(), url, token);
                    return null;
                });
    }

    public void uploadUrl(RequestContext requestContext, String packageId, UrlUploadRequest urlUploadRequest) {
        log.debug("#uploadUrl(RequestContext requestContext, UrlUploadRequest urlUploadRequest): {}, {}", requestContext, urlUploadRequest);
        executeMethod(requestContext,
                String.format(API_DOCUMENT_UPLOAD, packageId), (url, token, restTemplateWrapper) -> {
                    uploadUrl(urlUploadRequest, restTemplateWrapper.getRestTemplate(), url, token);
                    return null;
                });
    }

    private void uploadUrl(
            UrlUploadRequest urlUploadRequest,
            RestTemplate restTemplate,
            String uploadUrlUri,
            String userApiCsrfToken
    ) {
        log.debug("start uploadUrl");

        try {

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<UrlUploadRequest> requestEntity = new org.springframework.http.HttpEntity<>(urlUploadRequest, httpHeaders);
            ResponseEntity<String> uploadUrlResp = restTemplate.postForEntity(uploadUrlUri, requestEntity, String.class);
            if (CREATED.equals(uploadUrlResp.getStatusCode())) {
                log.debug(uploadUrlResp.getBody());
            } else {
                throw new ClientIntegrationException(
                        String.format("Couldn't execute Url upload:%n Code: %d, Message: %s",
                                uploadUrlResp.getStatusCode().value(),
                                uploadUrlResp.getBody()
                        )
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while uploading url " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while uploading url: " + ex.getMessage(), ex);
        }
    }

    private void uploadFile(
            FileUploadRequest fileUploadRequest,
            HttpClient httpClient,
            String uploadFileUri,
            String userApiCsrfToken
    ) {
        log.debug("start uploadFile");
        HttpResponse uploadFileResponse = null;
        try {

            HttpPost uploadFileRequest = new HttpPost(uploadFileUri);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("simpleUploader", fileUploadRequest.getFile(), ContentType.DEFAULT_BINARY, fileUploadRequest.getFileMetaData().getFileName());
            entityBuilder.addTextBody("_charset_", "UTF-8");
            ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String fileMetaData = objectWriter.writeValueAsString(fileUploadRequest.getFileMetaData());
            entityBuilder.addTextBody("simpleUploader-data", fileMetaData, ContentType.APPLICATION_JSON);

            org.apache.http.HttpEntity entity = entityBuilder.build();
            uploadFileRequest.setHeader(X_CSRF_TOKEN, userApiCsrfToken);
            uploadFileRequest.setEntity(entity);
            uploadFileResponse = httpClient.execute(uploadFileRequest);

            if (uploadFileResponse.getStatusLine().getStatusCode() != 201) {
                throw new ClientIntegrationException("Couldn't execute file uploading:\n" + IOUtils.toString(uploadFileResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            }

        } catch (Exception ex) {
            log.error("Error occurred while file document " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while file document: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(uploadFileResponse);
        }
    }

    private String getRequestPathPerDocumentType(String documentType, String documentTechnicalName) {

        switch (documentType) {
            case "FILE_DOCUMENT":
                return String.format(API_FILE_DELETE, documentTechnicalName);
            case "URL_DOCUMENT":
                return String.format(API_URL_DELETE, documentTechnicalName);
            default:
                throw new IllegalArgumentException("Unexpected object type " + documentType);
        }
    }
}
