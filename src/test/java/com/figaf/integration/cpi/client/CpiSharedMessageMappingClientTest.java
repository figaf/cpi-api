package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateSharedMessageMappingRequest;
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
class CpiSharedMessageMappingClientTest extends CpiRuntimeArtifactClientTest {

    private static final String API_TEST_SHARED_MESSAGE_MAPPING_NAME = "FigafApiTestMessageMapping";
    private static final String API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME = "FigafApiTestDummyMessageMapping";

    private static CpiSharedMessageMappingClient cpiSharedMessageMappingClient;

    @BeforeAll
    static void setUp() {
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiSharedMessageMappingClient = new CpiSharedMessageMappingClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSharedMessageMappingByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
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
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact messageMapping = findSharedMessageMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_SHARED_MESSAGE_MAPPING_NAME
        );
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        byte[] messageMappingPayload = cpiSharedMessageMappingClient.downloadSharedMessageMapping(
            requestContext,
            integrationPackage.getExternalId(),
            messageMapping.getExternalId()
        );
        assertThat(messageMappingPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteSharedMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact messageMapping = findSharedMessageMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME
        );
        assertThat(messageMapping).as("message mapping %s already exist", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();

        messageMapping = createDummySharedMessageMapping(requestContext, integrationPackage.getExternalId());
        assertThat(messageMapping).as("message mapping %s was not created", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        cpiSharedMessageMappingClient.deleteSharedMessageMapping(
            integrationPackage.getExternalId(),
            messageMapping.getExternalId(),
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME,
            requestContext
        );

        messageMapping = findSharedMessageMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME
        );
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateSharedMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact messageMapping = getOrCreateDummySharedMessageMapping(requestContext, packageExternalId);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMappingUpdated.zip")
        );
        CreateOrUpdateSharedMessageMappingRequest createMessageMappingRequest = CreateOrUpdateSharedMessageMappingRequest.builder()
            .id(messageMappingExternalId)
            .name(API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME)
            .description("Message Mapping for api tests")
            .build();
        cpiSharedMessageMappingClient.updateSharedMessageMapping(
            requestContext,
            packageExternalId,
            messageMappingExternalId,
            createMessageMappingRequest,
            payload
        );

        cpiSharedMessageMappingClient.deleteSharedMessageMapping(
            packageExternalId,
            messageMappingExternalId,
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME,
            requestContext
        );
        messageMapping = findSharedMessageMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deploySharedMessageMappingAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact messageMapping = getOrCreateDummySharedMessageMapping(requestContext, packageExternalId);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        String taskId = cpiSharedMessageMappingClient.deploySharedMessageMapping(
            requestContext,
            packageExternalId,
            messageMappingExternalId
        );
        assertThat(taskId).isNotBlank();

        String status = cpiSharedMessageMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        cpiSharedMessageMappingClient.deleteSharedMessageMapping(
            packageExternalId,
            messageMappingExternalId,
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME,
            requestContext
        );
        messageMapping = findSharedMessageMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    private CpiArtifact findSharedMessageMappingIfExist(
        RequestContext requestContext,
        String packageExternalId,
        String sharedMessageMappingName
    ) {
        List<CpiArtifact> artifacts = cpiSharedMessageMappingClient.getSharedMessageMappingByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> sharedMessageMappingName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummySharedMessageMapping(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMapping.zip")
        );
        CreateOrUpdateSharedMessageMappingRequest createMessageMappingRequest = CreateOrUpdateSharedMessageMappingRequest.builder()
            .id(API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME)
            .name(API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME)
            .description("Message mapping for api tests")
            .build();
        cpiSharedMessageMappingClient.createSharedMessageMapping(
            requestContext,
            packageExternalId,
            createMessageMappingRequest,
            payload
        );
        return findSharedMessageMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME);
    }

    private CpiArtifact getOrCreateDummySharedMessageMapping(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact messageMapping = findSharedMessageMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME);
        if (messageMapping == null) {
            messageMapping = createDummySharedMessageMapping(requestContext, packageExternalId);
        }
        return messageMapping;
    }

}