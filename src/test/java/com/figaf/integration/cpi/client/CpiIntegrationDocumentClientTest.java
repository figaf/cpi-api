package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.cpi_api_package.document.AdditionalAttributes;
import com.figaf.integration.cpi.entity.cpi_api_package.document.FileMetaData;
import com.figaf.integration.cpi.entity.cpi_api_package.document.FileUploadRequest;
import com.figaf.integration.cpi.entity.cpi_api_package.document.UrlUploadRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiIntegrationDocument;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class CpiIntegrationDocumentClientTest {

    private static CpiIntegrationDocumentClient cpiIntegrationDocumentClient;
    private static PackageUtils packageUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiIntegrationDocumentClient = new CpiIntegrationDocumentClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_uploadAndDeleteFile(AgentTestData agentTestData) throws IOException {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        String documentType = "FILE_DOCUMENT";

        List<CpiIntegrationDocument> cpiIntegrationDocuments = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);

        File testFileToUpload = ResourceUtils.getFile("classpath:upload-test-files-to-sap-cpi-package/testUploadFileToPackage.png");
        Optional<CpiIntegrationDocument> cpiIntegrationDocument = cpiIntegrationDocuments.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getFileName().equals(testFileToUpload.getName())).findFirst();
        assertThat(cpiIntegrationDocument).as("file %s already exists", cpiIntegrationDocument.orElse(new CpiIntegrationDocument()).getFileName()).isEmpty();
        FileMetaData fileMetaData = FileMetaData
                .builder()
                .name(FilenameUtils.removeExtension(testFileToUpload.getName()))
                .description("automated upload")
                .fileName(testFileToUpload.getName())
                .type("File")
                .build();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream in = Files.newInputStream(testFileToUpload.toPath())) {
            int read;
            while ((read = in.read()) != -1) {
                byteArrayOutputStream.write(read);
            }
        }

        FileUploadRequest fileUploadRequest = FileUploadRequest.builder().file(byteArrayOutputStream.toByteArray()).fileMetaData(fileMetaData).build();
        cpiIntegrationDocumentClient.uploadFile(requestContext, integrationPackage.getExternalId(), fileUploadRequest);

        List<CpiIntegrationDocument> cpiIntegrationDocumentsAfterCreation = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);
        Optional<CpiIntegrationDocument> cpiIntegrationDocumentAfterCreation = cpiIntegrationDocumentsAfterCreation.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getFileName().equals(testFileToUpload.getName())).findFirst();
        assertThat(cpiIntegrationDocumentAfterCreation).as("file %s wasn't created", cpiIntegrationDocumentAfterCreation.get().getFileName()).isNotEmpty();

        cpiIntegrationDocumentClient.deleteDocument(requestContext, documentType, cpiIntegrationDocumentAfterCreation.get().getTechnicalName());

        List<CpiIntegrationDocument> cpiIntegrationDocumentsAfterDeletion = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);
        Optional<CpiIntegrationDocument> cpiIntegrationDocumentAfterDeletion = cpiIntegrationDocumentsAfterDeletion.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getFileName().equals(testFileToUpload.getName())).findFirst();
        assertThat(cpiIntegrationDocumentAfterDeletion).as("file %s exists after deletion", cpiIntegrationDocumentAfterDeletion.orElse(new CpiIntegrationDocument()).getFileName()).isEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_uploadAndDeleteUrl(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        String documentType = "URL_DOCUMENT";

        List<CpiIntegrationDocument> cpiIntegrationDocuments = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);
        AdditionalAttributes additionalAttributes = AdditionalAttributes.builder().url(Lists.newArrayList("https://www.google.com")).build();
        UrlUploadRequest urlUploadRequest = UrlUploadRequest.builder().type("Url").name("URL").description("url description").additionalAttrs(additionalAttributes).build();
        Optional<CpiIntegrationDocument> cpiIntegrationDocument = cpiIntegrationDocuments.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getDisplayedName().equals(urlUploadRequest.getName())).findFirst();
        assertThat(cpiIntegrationDocument).as("url %s already exists", cpiIntegrationDocument.orElse(new CpiIntegrationDocument()).getDisplayedName()).isEmpty();

        cpiIntegrationDocumentClient.uploadUrl(requestContext, integrationPackage.getExternalId(), urlUploadRequest);

        List<CpiIntegrationDocument> cpiIntegrationDocumentsAfterCreation = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);
        Optional<CpiIntegrationDocument> cpiIntegrationDocumentAfterCreation = cpiIntegrationDocumentsAfterCreation.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getDisplayedName().equals(urlUploadRequest.getName())).findFirst();
        assertThat(cpiIntegrationDocumentAfterCreation).as("url %s wasn't created", cpiIntegrationDocumentAfterCreation.get().getDisplayedName()).isNotEmpty();

        cpiIntegrationDocumentClient.deleteDocument(requestContext, documentType, cpiIntegrationDocumentAfterCreation.get().getTechnicalName());

        List<CpiIntegrationDocument> cpiIntegrationDocumentsAfterDeletion = cpiIntegrationDocumentClient.getDocumentsByPackage(requestContext, integrationPackage.getTechnicalName(), integrationPackage.getDisplayedName(), integrationPackage.getExternalId(), documentType);
        Optional<CpiIntegrationDocument> cpiIntegrationDocumentAfterDeletion = cpiIntegrationDocumentsAfterDeletion.stream().filter(cpiIntegrationDocumentInner -> cpiIntegrationDocumentInner.getDisplayedName().equals(urlUploadRequest.getName())).findFirst();
        assertThat(cpiIntegrationDocumentAfterDeletion).as("url %s exists after deletion", cpiIntegrationDocumentAfterDeletion.orElse(new CpiIntegrationDocument()).getDisplayedName()).isEmpty();

    }

}