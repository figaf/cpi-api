package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateMessageMappingRequest;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.MessageMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static com.figaf.integration.cpi.utils.MessageMappingUtils.API_TEST_DUMMY_MESSAGE_MAPPING_NAME;
import static com.figaf.integration.cpi.utils.MessageMappingUtils.API_TEST_MESSAGE_MAPPING_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiMessageMappingClientTest {

    private static CpiMessageMappingClient cpiMessageMappingClient;
    private static PackageUtils packageUtils;
    private static MessageMappingUtils messageMappingUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiMessageMappingClient = new CpiMessageMappingClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        messageMappingUtils = new MessageMappingUtils(packageUtils, cpiMessageMappingClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMessageMappingByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> messageMapping = cpiMessageMappingClient.getMessageMappingsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(messageMapping).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadMessageMapping(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = messageMappingUtils.findTestMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_MESSAGE_MAPPING_NAME).isNotNull();

        byte[] messageMappingPayload = cpiMessageMappingClient.downloadMessageMapping(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMapping.getExternalId()
        );
        assertThat(messageMappingPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = messageMappingUtils.findDummyMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s already exist", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNull();

        messageMapping = messageMappingUtils.createDummyMessageMappingInTestPackage(requestContext);
        assertThat(messageMapping).as("message mapping %s was not created", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNotNull();
        messageMappingUtils.deleteMessageMapping(requestContext, messageMapping);
        messageMapping = messageMappingUtils.findDummyMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateMessageMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = messageMappingUtils.getOrCreateDummyMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMappingUpdated.zip")
        );
        UpdateMessageMappingRequest updateMessageMappingRequest = UpdateMessageMappingRequest.builder()
            .id(messageMappingExternalId)
            .name(API_TEST_DUMMY_MESSAGE_MAPPING_NAME)
            .description("Message Mapping for api tests")
            .packageExternalId(messageMapping.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiMessageMappingClient.updateMessageMapping(requestContext, updateMessageMappingRequest);

        messageMappingUtils.deleteMessageMapping(requestContext, messageMapping);
        messageMapping = messageMappingUtils.findDummyMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployMessageMappingAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = messageMappingUtils.getOrCreateDummyMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNotNull();

        String messageMappingExternalId = messageMapping.getExternalId();
        String taskId = cpiMessageMappingClient.deployMessageMapping(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMappingExternalId
        );
        assertThat(taskId).isNotBlank();

        String status = cpiMessageMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        messageMappingUtils.deleteMessageMapping(requestContext, messageMapping);
        messageMapping = messageMappingUtils.findDummyMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNull();
    }

}