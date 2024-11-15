package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactFromPublicApi;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateIFlowRequest;
import com.figaf.integration.cpi.entity.runtime_artifacts.DeployedArtifact;
import com.figaf.integration.cpi.entity.runtime_artifacts.IFlowRuntimeData;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.utils.IFlowUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.EDGE_RUNTIME_LOCATION_ID;
import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.IFlowUtils.*;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiIntegrationFlowClientTest {

    private static CpiIntegrationFlowClient cpiIntegrationFlowClient;
    private static IntegrationContentWebApiClient integrationContentWebApiClient;
    private static PackageUtils packageUtils;
    private static IFlowUtils iFlowUtils;

    @BeforeAll
    static void setUp() {
        HttpClientsFactory httpClientsFactory = new HttpClientsFactory();
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(httpClientsFactory);
        cpiIntegrationFlowClient = new CpiIntegrationFlowClient(httpClientsFactory);
        integrationContentWebApiClient = new IntegrationContentWebApiClient(httpClientsFactory);
        packageUtils = new PackageUtils(integrationPackageClient);
        iFlowUtils = new IFlowUtils(packageUtils, cpiIntegrationFlowClient);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getIFlowsByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> iFlows = cpiIntegrationFlowClient.getIFlowsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(iFlows).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadIFlow(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.findTestIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_IFLOW_NAME).isNotNull();

        byte[] iFlowPayload = cpiIntegrationFlowClient.downloadIFlow(
            requestContext,
            iFlow.getPackageExternalId(),
            iFlow.getExternalId()
        );
        assertThat(iFlowPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteIFlow(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s already exist", API_TEST_DUMMY_IFLOW_NAME).isNull();

        iFlow = iFlowUtils.createDummyIFlowInTestPackage(requestContext);
        assertThat(iFlow).as("iFlow %s was not created", API_TEST_DUMMY_IFLOW_NAME).isNotNull();
        iFlowUtils.deleteIFlow(requestContext, iFlow);
        iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateIFlow(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.getOrCreateDummyIFlow(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        String iFlowExternalId = iFlow.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyIFlowUpdated.zip")
        );
        UpdateIFlowRequest updateIFlowRequest = UpdateIFlowRequest.builder()
            .id(iFlowExternalId)
            .name(API_TEST_DUMMY_IFLOW_NAME)
            .description("IFlow for api tests")
            .bundledModel(payload)
            .packageExternalId(iFlow.getPackageExternalId())
            .comment("Comment")
            .build();
        cpiIntegrationFlowClient.updateIFlow(requestContext, updateIFlowRequest);

        iFlowUtils.deleteIFlow(requestContext, iFlow);
        iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployIFlowAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.getOrCreateDummyIFlow(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        String iFlowExternalId = iFlow.getExternalId();
        String taskId = cpiIntegrationFlowClient.deployIFlow(
            requestContext,
            iFlow.getPackageExternalId(),
            iFlowExternalId,
            API_TEST_DUMMY_IFLOW_NAME
        );
        assertThat(taskId).isNotBlank();

        String status = cpiIntegrationFlowClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        DeployedArtifact deployedArtifactInfo = cpiIntegrationFlowClient.getDeployedArtifactInfo(requestContext, iFlow.getTechnicalName());
        assertThat(deployedArtifactInfo).isNotNull();

        iFlowUtils.deleteIFlow(requestContext, iFlow);
        iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployIFlowViaPublicApiAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.getOrCreateDummyIFlow(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        String taskId = cpiIntegrationFlowClient.deployIFlowViaPublicApi(
            requestContext,
            iFlow.getTechnicalName()
        );
        assertThat(taskId).isNotBlank();

        String status = cpiIntegrationFlowClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        iFlowUtils.deleteIFlow(requestContext, iFlow);
        iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_setTraceLogLevelForIFlows(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        cpiIntegrationFlowClient.setTraceLogLevelForIFlows(requestContext, singletonList(new IFlowRuntimeData(API_TEST_IFLOW_NAME, null)));
        DeployedArtifact deployedArtifactInfo = cpiIntegrationFlowClient.getDeployedArtifactInfo(requestContext, API_TEST_IFLOW_NAME);
        IntegrationContent integrationRuntimeArtifact = integrationContentWebApiClient.getIntegrationRuntimeArtifact(requestContext, deployedArtifactInfo.getId());
        assertThat(integrationRuntimeArtifact.getLogConfiguration().isTraceEnabled()).isTrue();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_setTraceLogLevelForIFlowsDeployedInEdge(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        requestContext.setRuntimeLocationId(EDGE_RUNTIME_LOCATION_ID);
        cpiIntegrationFlowClient.setTraceLogLevelForIFlows(requestContext, singletonList(new IFlowRuntimeData(API_TEST_EDGE_IFLOW_NAME, EDGE_RUNTIME_LOCATION_ID)));
        DeployedArtifact deployedArtifactInfo = cpiIntegrationFlowClient.getDeployedArtifactInfo(requestContext, API_TEST_EDGE_IFLOW_NAME);
        IntegrationContent integrationRuntimeArtifact = integrationContentWebApiClient.getIntegrationRuntimeArtifact(requestContext, deployedArtifactInfo.getId());
        assertThat(integrationRuntimeArtifact.getLogConfiguration().isTraceEnabled()).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getIFlowByTechnicalName(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifactFromPublicApi cpiArtifactFromPublicApi = cpiIntegrationFlowClient.getIFlowByTechnicalName(requestContext, API_TEST_IFLOW_NAME);
        assertThat(cpiArtifactFromPublicApi).isNotNull();
        assertThat(cpiArtifactFromPublicApi.getTechnicalName()).isEqualTo(API_TEST_IFLOW_NAME);
    }

}