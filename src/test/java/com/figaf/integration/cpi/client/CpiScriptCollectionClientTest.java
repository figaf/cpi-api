package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateScriptCollectionRequest;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.ScriptCollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static com.figaf.integration.cpi.utils.ScriptCollectionUtils.API_TEST_DUMMY_SCRIPT_COLLECTION_NAME;
import static com.figaf.integration.cpi.utils.ScriptCollectionUtils.API_TEST_SCRIPT_COLLECTION_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiScriptCollectionClientTest {

    private static CpiScriptCollectionClient cpiScriptCollectionClient;
    private static PackageUtils packageUtils;
    private static ScriptCollectionUtils scriptCollectionUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiScriptCollectionClient = new CpiScriptCollectionClient(integrationPackageClient, new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        scriptCollectionUtils = new ScriptCollectionUtils(packageUtils, cpiScriptCollectionClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getScriptCollectionByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> scriptCollection = cpiScriptCollectionClient.getScriptCollectionsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(scriptCollection).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadScriptCollection(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact scriptCollection = scriptCollectionUtils.findTestScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_SCRIPT_COLLECTION_NAME).isNotNull();

        byte[] scriptCollectionPayload = cpiScriptCollectionClient.downloadScriptCollection(
            requestContext,
            scriptCollection.getPackageExternalId(),
            scriptCollection.getExternalId()
        );
        assertThat(scriptCollectionPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteScriptCollection(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact scriptCollection = scriptCollectionUtils.findDummyScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s already exist", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();

        scriptCollection = scriptCollectionUtils.createDummyScriptCollectionInTestPackage(requestContext);
        assertThat(scriptCollection).as("script collection %s was not created", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();
        scriptCollectionUtils.deleteScriptCollection(requestContext, scriptCollection);
        scriptCollection = scriptCollectionUtils.findDummyScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateScriptCollection(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact scriptCollection = scriptCollectionUtils.getOrCreateDummyScriptCollection(requestContext);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        String scriptCollectionExternalId = scriptCollection.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyScriptCollectionUpdated.zip")
        );
        UpdateScriptCollectionRequest updateScriptCollectionRequest = UpdateScriptCollectionRequest.builder()
            .id(scriptCollectionExternalId)
            .name(API_TEST_DUMMY_SCRIPT_COLLECTION_NAME)
            .description("Script Collection for api tests")
            .packageExternalId(scriptCollection.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiScriptCollectionClient.updateScriptCollection(requestContext, updateScriptCollectionRequest);

        scriptCollectionUtils.deleteScriptCollection(requestContext, scriptCollection);
        scriptCollection = scriptCollectionUtils.findDummyScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployScriptCollectionAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact scriptCollection = scriptCollectionUtils.getOrCreateDummyScriptCollection(requestContext);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        String scriptCollectionExternalId = scriptCollection.getExternalId();
        String taskId = cpiScriptCollectionClient.deployScriptCollection(
            requestContext,
            scriptCollection.getPackageExternalId(),
            scriptCollectionExternalId,
            scriptCollection.getTechnicalName()
        );
        assertThat(taskId).isNotBlank();

        String status = cpiScriptCollectionClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        scriptCollectionUtils.deleteScriptCollection(requestContext, scriptCollection);
        scriptCollection = scriptCollectionUtils.findDummyScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

}