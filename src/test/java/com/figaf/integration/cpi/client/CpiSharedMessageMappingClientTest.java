package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateSharedMessageMappingRequest;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.SharedMessageMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static com.figaf.integration.cpi.utils.SharedMessageMappingUtils.API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME;
import static com.figaf.integration.cpi.utils.SharedMessageMappingUtils.API_TEST_SHARED_MESSAGE_MAPPING_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiSharedMessageMappingClientTest {

    private static CpiSharedMessageMappingClient cpiSharedMessageMappingClient;
    private static PackageUtils packageUtils;
    private static SharedMessageMappingUtils sharedMessageMappingUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiSharedMessageMappingClient = new CpiSharedMessageMappingClient(integrationPackageClient, new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        sharedMessageMappingUtils = new SharedMessageMappingUtils(packageUtils, cpiSharedMessageMappingClient);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSharedMessageMappingByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> messageMapping = cpiSharedMessageMappingClient.getSharedMessageMappingByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(messageMapping).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadSharedMessageMapping(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = sharedMessageMappingUtils.findTestSharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        byte[] messageMappingPayload = cpiSharedMessageMappingClient.downloadSharedMessageMapping(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMapping.getExternalId()
        );
        assertThat(messageMappingPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteSharedMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = sharedMessageMappingUtils.findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s already exist", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();

        messageMapping = sharedMessageMappingUtils.createDummySharedMessageMappingInTestPackage(requestContext);
        assertThat(messageMapping).as("message mapping %s was not created", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();
        sharedMessageMappingUtils.deleteSharedMessageMapping(requestContext, messageMapping);
        messageMapping = sharedMessageMappingUtils.findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateSharedMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = sharedMessageMappingUtils.getOrCreateDummySharedMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMappingUpdated.zip")
        );
        UpdateSharedMessageMappingRequest updateMessageMappingRequest = UpdateSharedMessageMappingRequest.builder()
            .id(messageMappingExternalId)
            .name(API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME)
            .description("Message Mapping for api tests")
            .packageExternalId(messageMapping.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiSharedMessageMappingClient.updateSharedMessageMapping(requestContext, updateMessageMappingRequest);

        sharedMessageMappingUtils.deleteSharedMessageMapping(requestContext, messageMapping);
        messageMapping = sharedMessageMappingUtils.findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deploySharedMessageMappingAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = sharedMessageMappingUtils.getOrCreateDummySharedMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        String taskId = cpiSharedMessageMappingClient.deploySharedMessageMapping(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMappingExternalId
        );
        assertThat(taskId).isNotBlank();

        String status = cpiSharedMessageMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        sharedMessageMappingUtils.deleteSharedMessageMapping(requestContext, messageMapping);
        messageMapping = sharedMessageMappingUtils.findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

}