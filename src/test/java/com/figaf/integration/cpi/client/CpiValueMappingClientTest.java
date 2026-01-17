package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateValueMappingRequest;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.ValueMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static com.figaf.integration.cpi.utils.ValueMappingUtils.API_TEST_DUMMY_VALUE_MAPPING_NAME;
import static com.figaf.integration.cpi.utils.ValueMappingUtils.API_TEST_VALUE_MAPPING_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiValueMappingClientTest {

    private static CpiValueMappingClient cpiValueMappingClient;
    private static PackageUtils packageUtils;
    private static ValueMappingUtils valueMappingUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiValueMappingClient = new CpiValueMappingClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        valueMappingUtils = new ValueMappingUtils(packageUtils, cpiValueMappingClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getValueMappingByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> valueMapping = cpiValueMappingClient.getValueMappingsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(valueMapping).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadValueMapping(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact valueMapping = valueMappingUtils.findTestValueMappingInTestPackageIfExist(requestContext);
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_VALUE_MAPPING_NAME).isNotNull();

        byte[] valueMappingPayload = cpiValueMappingClient.downloadValueMapping(
            requestContext,
            valueMapping.getPackageExternalId(),
            valueMapping.getExternalId()
        );
        assertThat(valueMappingPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteValueMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact valueMapping = valueMappingUtils.findDummyValueMappingInTestPackageIfExist(requestContext);
        assertThat(valueMapping).as("value mapping %s already exist", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();

        valueMapping = valueMappingUtils.createDummyValueMappingInTestPackage(requestContext);
        assertThat(valueMapping).as("value mapping %s was not created", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();
        valueMappingUtils.deleteValueMapping(requestContext, valueMapping);
        valueMapping = valueMappingUtils.findDummyValueMappingInTestPackageIfExist(requestContext);
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateValueMapping(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact valueMapping = valueMappingUtils.getOrCreateDummyValueMapping(requestContext);
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();

        String valueMappingExternalId = valueMapping.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyValueMappingUpdated.zip")
        );
        UpdateValueMappingRequest updateValueMappingRequest = UpdateValueMappingRequest.builder()
            .id(valueMappingExternalId)
            .name(API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .description("Value Mapping for api tests")
            .packageExternalId(valueMapping.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiValueMappingClient.updateValueMapping(requestContext, updateValueMappingRequest);

        valueMappingUtils.deleteValueMapping(requestContext, valueMapping);
        valueMapping = valueMappingUtils.findDummyValueMappingInTestPackageIfExist(requestContext);
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployValueMappingAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact valueMapping = valueMappingUtils.getOrCreateDummyValueMapping(requestContext);
        assertThat(valueMapping).as("value mapping %s wasn't found", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNotNull();

        String valueMappingExternalId = valueMapping.getExternalId();
        String taskId = cpiValueMappingClient.deployValueMapping(
            requestContext,
            valueMapping.getPackageExternalId(),
            valueMappingExternalId,
            null
        );
        assertThat(taskId).isNotBlank();

        String status = cpiValueMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        valueMappingUtils.deleteValueMapping(requestContext, valueMapping);
        valueMapping = valueMappingUtils.findDummyValueMappingInTestPackageIfExist(requestContext);
        assertThat(valueMapping).as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployValueMappingWithRuntimeProfileAndCheckDeploymentStatus(
        AgentTestData agentTestData
    ) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact valueMapping = valueMappingUtils.getOrCreateDummyValueMapping(requestContext);
        assertThat(valueMapping)
            .as("value mapping %s wasn't found", API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .isNotNull();

        String valueMappingExternalId = valueMapping.getExternalId();
        String runtimeProfile = "edge-suseedge";
        String taskId = cpiValueMappingClient.deployValueMapping(
            requestContext,
            valueMapping.getPackageExternalId(),
            valueMappingExternalId,
            runtimeProfile
        );

        assertThat(taskId).isNotBlank();
        String status = cpiValueMappingClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();
        valueMappingUtils.deleteValueMapping(
            requestContext,
            valueMapping
        );

        valueMapping = valueMappingUtils.findDummyValueMappingInTestPackageIfExist(
            requestContext
        );
        assertThat(valueMapping)
            .as("value mapping %s was not deleted", API_TEST_DUMMY_VALUE_MAPPING_NAME)
            .isNull();
    }
}