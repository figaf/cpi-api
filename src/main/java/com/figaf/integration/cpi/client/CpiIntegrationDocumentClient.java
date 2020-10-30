package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiIntegrationDocument;
import com.figaf.integration.cpi.response_parser.CpiIntegrationDocumentParser;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class CpiIntegrationDocumentClient extends BaseClient {

    private static final String API_FILE_DOCUMENTS_META_DATA = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Files?$format=json";
    private static final String API_URL_DOCUMENTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Urls?$format=json";
    private static final String API_FILE_DOCUMENT = "/itspaces/odata/1.0/workspace.svc/ContentEntities.Files('%s')/$value?attachment=false";

    public CpiIntegrationDocumentClient(String ssoUrl) {
        super(ssoUrl);
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

}
