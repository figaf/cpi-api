package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateScriptCollectionRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiScriptCollectionClientTest extends CpiRuntimeArtifactClientTest {

    private static final String API_TEST_SCRIPT_COLLECTION_NAME = "FigafApiTestScriptCollection";
    private static final String API_TEST_DUMMY_SCRIPT_COLLECTION_NAME = "FigafApiTestDummyScriptCollection";

    private static CpiScriptCollectionClient cpiScriptCollectionClient;

    @BeforeAll
    static void setUp() {
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiScriptCollectionClient = new CpiScriptCollectionClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getScriptCollectionByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> scriptCollection = cpiScriptCollectionClient.getScriptCollectionsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(scriptCollection).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadScriptCollection(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact scriptCollection = findScriptCollectionIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_SCRIPT_COLLECTION_NAME
        );
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_SCRIPT_COLLECTION_NAME).isNotNull();

        byte[] scriptCollectionPayload = cpiScriptCollectionClient.downloadScriptCollection(
            requestContext,
            integrationPackage.getExternalId(),
            scriptCollection.getExternalId()
        );
        assertThat(scriptCollectionPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteScriptCollection(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact scriptCollection = findScriptCollectionIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME
        );
        assertThat(scriptCollection).as("script collection %s already exist", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();

        scriptCollection = createDummyScriptCollection(requestContext, integrationPackage.getExternalId());
        assertThat(scriptCollection).as("script collection %s was not created", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        cpiScriptCollectionClient.deleteScriptCollection(
            integrationPackage.getExternalId(),
            scriptCollection.getExternalId(),
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME,
            requestContext
        );

        scriptCollection = findScriptCollectionIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME
        );
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateScriptCollection(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact scriptCollection = getOrCreateDummyScriptCollection(requestContext, packageExternalId);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        String scriptCollectionExternalId = scriptCollection.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyScriptCollectionUpdated.zip")
        );
        CreateOrUpdateScriptCollectionRequest createScriptCollectionRequest = CreateOrUpdateScriptCollectionRequest.builder()
            .id(scriptCollectionExternalId)
            .name(API_TEST_DUMMY_SCRIPT_COLLECTION_NAME)
            .description("Script Collection for api tests")
            .build();
        cpiScriptCollectionClient.updateScriptCollection(
            requestContext,
            packageExternalId,
            scriptCollectionExternalId,
            createScriptCollectionRequest,
            payload
        );

        cpiScriptCollectionClient.deleteScriptCollection(
            packageExternalId,
            scriptCollectionExternalId,
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME,
            requestContext
        );
        scriptCollection = findScriptCollectionIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SCRIPT_COLLECTION_NAME);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployScriptCollectionAndCheckDeployStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact scriptCollection = getOrCreateDummyScriptCollection(requestContext, packageExternalId);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        String scriptCollectionExternalId = scriptCollection.getExternalId();
        String taskId = cpiScriptCollectionClient.deployScriptCollection(
            requestContext,
            packageExternalId,
            scriptCollectionExternalId,
            scriptCollection.getTechnicalName()
        );
        assertThat(taskId).isNotBlank();

        String status = cpiScriptCollectionClient.checkDeployStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        cpiScriptCollectionClient.deleteScriptCollection(
            packageExternalId,
            scriptCollectionExternalId,
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME,
            requestContext
        );
        scriptCollection = findScriptCollectionIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SCRIPT_COLLECTION_NAME);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    private CpiArtifact findScriptCollectionIfExist(
        RequestContext requestContext,
        String packageExternalId,
        String scriptCollectionName
    ) {
        List<CpiArtifact> artifacts = cpiScriptCollectionClient.getScriptCollectionsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> scriptCollectionName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyScriptCollection(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyScriptCollection.zip")
        );
        CreateOrUpdateScriptCollectionRequest createScriptCollectionRequest = CreateOrUpdateScriptCollectionRequest.builder()
            .id(API_TEST_DUMMY_SCRIPT_COLLECTION_NAME)
            .name(API_TEST_DUMMY_SCRIPT_COLLECTION_NAME)
            .description("Script Collection for api tests")
            .build();
        cpiScriptCollectionClient.createScriptCollection(
            requestContext,
            packageExternalId,
            createScriptCollectionRequest,
            payload
        );
        return findScriptCollectionIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SCRIPT_COLLECTION_NAME);
    }

    private CpiArtifact getOrCreateDummyScriptCollection(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact scriptCollection = findScriptCollectionIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SCRIPT_COLLECTION_NAME);
        if (scriptCollection == null) {
            scriptCollection = createDummyScriptCollection(requestContext, packageExternalId);
        }
        return scriptCollection;
    }

}