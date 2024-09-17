package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateImportedArchivesRequest;
import com.figaf.integration.cpi.utils.ImportedArchivesUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.ImportedArchivesUtils.API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME;
import static com.figaf.integration.cpi.utils.ImportedArchivesUtils.API_TEST_IMPORTED_ARCHIVES_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
class CpiImportedArchivesClientTest {

    private static CpiImportedArchivesClient cpiImportedArchivesClient;
    private static PackageUtils packageUtils;
    private static ImportedArchivesUtils importedArchivesUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiImportedArchivesClient = new CpiImportedArchivesClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        importedArchivesUtils = new ImportedArchivesUtils(packageUtils, cpiImportedArchivesClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getImportedArchivesByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> importedArchivesList = cpiImportedArchivesClient.getImportedArchivesByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(importedArchivesList).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadImportedArchives(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact importedArchives = importedArchivesUtils.findTestImportedArchivesInTestPackageIfExist(requestContext);
        assertThat(importedArchives).as("imported archives %s wasn't found", API_TEST_IMPORTED_ARCHIVES_NAME).isNotNull();

        byte[] importedArchivesPayload = cpiImportedArchivesClient.downloadArtifact(
            requestContext,
            importedArchives.getPackageExternalId(),
            importedArchives.getExternalId()
        );
        assertThat(importedArchivesPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteImportedArchives(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact importedArchives = importedArchivesUtils.findDummyImportedArchivesInTestPackageIfExist(requestContext);
        assertThat(importedArchives).as("imported archives %s already exist", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNull();

        importedArchives = importedArchivesUtils.createDummyImportedArchivesInTestPackage(requestContext);
        assertThat(importedArchives).as("imported archives %s was not created", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNotNull();
        importedArchivesUtils.deleteImportedArchives(requestContext, importedArchives);
        importedArchives = importedArchivesUtils.findDummyImportedArchivesInTestPackageIfExist(requestContext);
        assertThat(importedArchives).as("imported archives %s was not deleted", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateImportedArchives(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact importedArchives = importedArchivesUtils.getOrCreateDummyImportedArchives(requestContext);
        assertThat(importedArchives).as("imported archives %s wasn't found", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNotNull();

        String importedArchivesExternalId = importedArchives.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyImportedArchivesUpdated.zip")
        );
        UpdateImportedArchivesRequest updateImportedArchivesRequest = UpdateImportedArchivesRequest.builder()
            .id(importedArchivesExternalId)
            .name(API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME)
            .description("Imported archives for api tests")
            .packageExternalId(importedArchives.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiImportedArchivesClient.updateArtifact(requestContext, updateImportedArchivesRequest);

        importedArchivesUtils.deleteImportedArchives(requestContext, importedArchives);
        importedArchives = importedArchivesUtils.findDummyImportedArchivesInTestPackageIfExist(requestContext);
        assertThat(importedArchives).as("imported archives %s was not deleted", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployImportedArchivesAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact importedArchives = importedArchivesUtils.getOrCreateDummyImportedArchives(requestContext);
        assertThat(importedArchives).as("imported archives %s wasn't found", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNotNull();

        String importedArchivesExternalId = importedArchives.getExternalId();
        String taskId = cpiImportedArchivesClient.deployImportedArchives(
            requestContext,
            importedArchives.getPackageExternalId(),
            importedArchivesExternalId,
            importedArchives.getTechnicalName()
        );
        assertThat(taskId).isNotBlank();

        String status = cpiImportedArchivesClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        importedArchivesUtils.deleteImportedArchives(requestContext, importedArchives);
        importedArchives = importedArchivesUtils.findDummyImportedArchivesInTestPackageIfExist(requestContext);
        assertThat(importedArchives).as("imported archives %s was not deleted", API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME).isNull();
    }

}