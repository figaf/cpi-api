package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateValueMappingRequest;
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
class CpiValueMappingClientTest extends CpiRuntimeArtifactClientTest {

    private static final String API_TEST_VALUE_MAPPING_NAME = "FigafApiTestValueMapping";
    private static final String API_TEST_DUMMY_VALUE_MAPPING_NAME = "FigafApiTestDummyValueMapping";

    private static CpiValueMappingClient cpiValueMappingClient;

    @BeforeAll
    static void setUp() {
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiValueMappingClient = new CpiValueMappingClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getValueMappingByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> valueMapping = cpiValueMappingClient.getValueMappingsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(valueMapping).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadValueMapping(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact valueMapping = findValueMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_VALUE_MAPPING_NAME
        );
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_VALUE_MAPPING_NAME).isNotNull();

        byte[] valueMappingPayload = cpiValueMappingClient.downloadValueMapping(
            requestContext,
            integrationPackage.getExternalId(),
            valueMapping.getExternalId()
        );
        assertThat(valueMappingPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteValueMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact valueMapping = findValueMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_VALUE_MAPPING_NAME
        );
        assertThat(valueMapping).as("value mapping %s already exist", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();

        valueMapping = createDummyValueMapping(requestContext, integrationPackage.getExternalId());
        assertThat(valueMapping).as("value mapping %s was not created", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();

        cpiValueMappingClient.deleteValueMapping(
            integrationPackage.getExternalId(),
            valueMapping.getExternalId(),
            API_TEST_DUMMY_VALUE_MAPPING_NAME,
            requestContext
        );

        valueMapping = findValueMappingIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_VALUE_MAPPING_NAME
        );
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateValueMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact valueMapping = getOrCreateDummyValueMapping(requestContext, packageExternalId);
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();

        String valueMappingExternalId = valueMapping.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyValueMappingUpdated.zip")
        );
        CreateOrUpdateValueMappingRequest createValueMappingRequest = CreateOrUpdateValueMappingRequest.builder()
            .id(valueMappingExternalId)
            .name(API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .description("Value Mapping for api tests")
            .build();
        cpiValueMappingClient.updateValueMapping(
            requestContext,
            packageExternalId,
            valueMappingExternalId,
            createValueMappingRequest,
            payload
        );

        cpiValueMappingClient.deleteValueMapping(packageExternalId, valueMappingExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME, requestContext);
        valueMapping = findValueMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME);
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployValueMappingAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact valueMapping = getOrCreateDummyValueMapping(requestContext, packageExternalId);
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();

        String valueMappingExternalId = valueMapping.getExternalId();
        String taskId = cpiValueMappingClient.deployValueMapping(
            requestContext,
            packageExternalId,
            valueMappingExternalId
        );
        assertThat(taskId).isNotBlank();

        String status = cpiValueMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        cpiValueMappingClient.deleteValueMapping(packageExternalId, valueMappingExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME, requestContext);
        valueMapping = findValueMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME);
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    private CpiArtifact findValueMappingIfExist(
        RequestContext requestContext,
        String packageExternalId,
        String valueMappingName
    ) {
        List<CpiArtifact> artifacts = cpiValueMappingClient.getValueMappingsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> valueMappingName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyValueMapping(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyValueMapping.zip")
        );
        CreateOrUpdateValueMappingRequest createValueMappingRequest = CreateOrUpdateValueMappingRequest.builder()
            .id(API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .name(API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .description("Value Mapping for api tests")
            .build();
        cpiValueMappingClient.createValueMapping(
            requestContext,
            packageExternalId,
            createValueMappingRequest,
            payload
        );
        return findValueMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME);
    }

    private CpiArtifact getOrCreateDummyValueMapping(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact valueMapping = findValueMappingIfExist(requestContext, packageExternalId, API_TEST_DUMMY_VALUE_MAPPING_NAME);
        if (valueMapping == null) {
            valueMapping = createDummyValueMapping(requestContext, packageExternalId);
        }
        return valueMapping;
    }

}