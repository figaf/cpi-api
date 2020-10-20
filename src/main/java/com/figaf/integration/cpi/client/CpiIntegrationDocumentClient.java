package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.wrapper.CommonClientWrapper;
import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiIntegrationDocument;
import com.figaf.integration.cpi.response_parser.CpiIntegrationDocumentParser;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class CpiIntegrationDocumentClient extends CommonClientWrapper {

    private static final String API_FILE_DOCUMENTS_META_DATA = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Files?$format=json";
    private static final String API_URL_DOCUMENTS = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')/Urls?$format=json";
    private static final String API_FILE_DOCUMENT = "/itspaces/odata/1.0/workspace.svc/ContentEntities.Files('%s')/$value?attachment=false";

    public CpiIntegrationDocumentClient(String ssoUrl) {
        super(ssoUrl);
    }

    public List<CpiIntegrationDocument> getDocumentsByPackage(
        CommonClientWrapperEntity commonClientWrapperEntity,
        String packageTechnicalName,
        String packageDisplayedName,
        String packagePackageExternalId,
        String documentType
    ) {
        log.debug("#getDocumentsByPackage(CommonClientWrapperEntity commonClientWrapperEntity, String packageTechnicalName, String packageDisplayedName, String packagePackageExternalId, TrackedObjectType documentType): " +
            "{}, {}, {}, {}, {}", commonClientWrapperEntity, packageTechnicalName, packageDisplayedName, packagePackageExternalId, documentType);

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
            commonClientWrapperEntity,
            path,
            body -> CpiIntegrationDocumentParser.buildCpiIntegrationDocuments(documentType, body)
        );
    }

    public byte[] downloadFileDocument(CommonClientWrapperEntity commonClientWrapperEntity, String documentTechnicalName) {
        log.debug("#downloadFileDocument(CommonClientWrapperEntity commonClientWrapperEntity, String documentTechnicalName): {}, {}", commonClientWrapperEntity, documentTechnicalName);
        String path = String.format(API_FILE_DOCUMENT, documentTechnicalName);
        return executeGet(commonClientWrapperEntity, path, resolvedBody -> resolvedBody, byte[].class);
    }

    public byte[] downloadUrlDocument(String url) {
        log.debug("#downloadUrlDocument(String url): {}", url);
        return url.getBytes(StandardCharsets.UTF_8);
    }

}
