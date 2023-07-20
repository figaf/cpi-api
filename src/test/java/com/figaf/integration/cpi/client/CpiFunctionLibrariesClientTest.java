package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateFunctionLibrariesRequest;
import com.figaf.integration.cpi.utils.FunctionLibrariesUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.FunctionLibrariesUtils.API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME;
import static com.figaf.integration.cpi.utils.FunctionLibrariesUtils.API_TEST_FUNCTION_LIBRARIES_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
class CpiFunctionLibrariesClientTest {

    private static CpiFunctionLibrariesClient cpiFunctionLibrariesClient;
    private static PackageUtils packageUtils;
    private static FunctionLibrariesUtils functionLibrariesUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiFunctionLibrariesClient = new CpiFunctionLibrariesClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        functionLibrariesUtils = new FunctionLibrariesUtils(packageUtils, cpiFunctionLibrariesClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getFunctionLibrariesByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> functionLibrariesList = cpiFunctionLibrariesClient.getFunctionLibrariesByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(functionLibrariesList).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadFunctionLibraries(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact functionLibraries = functionLibrariesUtils.findTestFunctionLibrariesInTestPackageIfExist(requestContext);
        assertThat(functionLibraries).as("function libraries %s wasn't found", API_TEST_FUNCTION_LIBRARIES_NAME).isNotNull();

        byte[] functionLibrariesPayload = cpiFunctionLibrariesClient.downloadArtifact(
            requestContext,
            functionLibraries.getPackageExternalId(),
            functionLibraries.getExternalId()
        );
        assertThat(functionLibrariesPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteFunctionLibraries(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact functionLibraries = functionLibrariesUtils.findDummyFunctionLibrariesInTestPackageIfExist(requestContext);
        assertThat(functionLibraries).as("function libraries %s already exist", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNull();

        functionLibraries = functionLibrariesUtils.createDummyFunctionLibrariesInTestPackage(requestContext);
        assertThat(functionLibraries).as("function libraries %s was not created", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNotNull();
        functionLibrariesUtils.deleteFunctionLibraries(requestContext, functionLibraries);
        functionLibraries = functionLibrariesUtils.findDummyFunctionLibrariesInTestPackageIfExist(requestContext);
        assertThat(functionLibraries).as("function libraries %s was not deleted", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateFunctionLibraries(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact functionLibraries = functionLibrariesUtils.getOrCreateDummyFunctionLibraries(requestContext);
        assertThat(functionLibraries).as("function libraries %s wasn't found", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNotNull();

        String functionLibrariesExternalId = functionLibraries.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyFunctionLibrariesUpdated.zip")
        );
        UpdateFunctionLibrariesRequest updateFunctionLibrariesRequest = UpdateFunctionLibrariesRequest.builder()
            .id(functionLibrariesExternalId)
            .name(API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME)
            .description("Function Libraries for api tests")
            .packageExternalId(functionLibraries.getPackageExternalId())
            .bundledModel(payload)
            .build();
        cpiFunctionLibrariesClient.updateArtifact(requestContext, updateFunctionLibrariesRequest);

        functionLibrariesUtils.deleteFunctionLibraries(requestContext, functionLibraries);
        functionLibraries = functionLibrariesUtils.findDummyFunctionLibrariesInTestPackageIfExist(requestContext);
        assertThat(functionLibraries).as("function libraries %s was not deleted", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployFunctionLibrariesAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact functionLibraries = functionLibrariesUtils.getOrCreateDummyFunctionLibraries(requestContext);
        assertThat(functionLibraries).as("function libraries %s wasn't found", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNotNull();

        String functionLibrariesExternalId = functionLibraries.getExternalId();
        String taskId = cpiFunctionLibrariesClient.deployFunctionLibraries(
            requestContext,
            functionLibraries.getPackageExternalId(),
            functionLibrariesExternalId,
            functionLibraries.getTechnicalName()
        );
        assertThat(taskId).isNotBlank();

        String status = cpiFunctionLibrariesClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        functionLibrariesUtils.deleteFunctionLibraries(requestContext, functionLibraries);
        functionLibraries = functionLibrariesUtils.findDummyFunctionLibrariesInTestPackageIfExist(requestContext);
        assertThat(functionLibraries).as("function libraries %s was not deleted", API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME).isNull();
    }

}