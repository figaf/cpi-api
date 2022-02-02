package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiIntegrationFlowClientTest extends CpiRuntimeArtifactClientTest {

    private static final String API_TEST_IFLOW_NAME = "FigafApiTestIFlow";
    private static final String API_TEST_DUMMY_IFLOW_NAME = "FigafApiTestDummyIFlow";

    private static CpiIntegrationFlowClient cpiIntegrationFlowClient;

    @BeforeAll
    static void setUp() {
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiIntegrationFlowClient = new CpiIntegrationFlowClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getIFlowsByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> iFlows = cpiIntegrationFlowClient.getIFlowsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(iFlows).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadIFlow(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact iFlow = findIFlowIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_IFLOW_NAME
        );
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_IFLOW_NAME).isNotNull();

        byte[] iFlowPayload = cpiIntegrationFlowClient.downloadIFlow(
            requestContext,
            integrationPackage.getExternalId(),
            iFlow.getExternalId()
        );
        assertThat(iFlowPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteIFlow(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact iFlow = findIFlowIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IFLOW_NAME
        );
        assertThat(iFlow).as("iFlow %s already exist", API_TEST_DUMMY_IFLOW_NAME).isNull();

        iFlow = createDummyIFlow(requestContext, integrationPackage.getExternalId());
        assertThat(iFlow).as("iFlow %s was not created", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        cpiIntegrationFlowClient.deleteIFlow(
            integrationPackage.getExternalId(),
            iFlow.getExternalId(),
            API_TEST_DUMMY_IFLOW_NAME,
            requestContext
        );

        iFlow = findIFlowIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IFLOW_NAME
        );
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateIFlow(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact iFlow = getOrCreateDummyIFlow(requestContext, packageExternalId);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        String iFlowExternalId = iFlow.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyIFlowUpdated.zip")
        );
        CreateOrUpdateIFlowRequest createIFlowRequest = CreateOrUpdateIFlowRequest.builder()
            .id(iFlowExternalId)
            .name(API_TEST_DUMMY_IFLOW_NAME)
            .description("IFlow for api tests")
            .build();
        cpiIntegrationFlowClient.updateIFlow(
            requestContext,
            packageExternalId,
            iFlowExternalId,
            createIFlowRequest,
            payload
        );

        cpiIntegrationFlowClient.deleteIFlow(packageExternalId, iFlowExternalId, API_TEST_DUMMY_IFLOW_NAME, requestContext);
        iFlow = findIFlowIfExist(requestContext, packageExternalId, API_TEST_DUMMY_IFLOW_NAME);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployIFlowAndCheckDeployStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact iFlow = getOrCreateDummyIFlow(requestContext, packageExternalId);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        String iFlowExternalId = iFlow.getExternalId();
        String taskId = cpiIntegrationFlowClient.deployIFlow(
            requestContext,
            packageExternalId,
            iFlowExternalId,
            API_TEST_DUMMY_IFLOW_NAME
        );
        assertThat(taskId).isNotBlank();

        String status = cpiIntegrationFlowClient.checkDeployStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        cpiIntegrationFlowClient.deleteIFlow(packageExternalId, iFlowExternalId, API_TEST_DUMMY_IFLOW_NAME, requestContext);
        iFlow = findIFlowIfExist(requestContext, packageExternalId, API_TEST_DUMMY_IFLOW_NAME);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_setTraceLogLevelForIFlows(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        cpiIntegrationFlowClient.setTraceLogLevelForIFlows(requestContext, singletonList(API_TEST_IFLOW_NAME));
    }

    private CpiArtifact findIFlowIfExist(
        RequestContext requestContext,
        String packageExternalId,
        String iFlowName
    ) {
        List<CpiArtifact> artifacts = cpiIntegrationFlowClient.getIFlowsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> iFlowName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyIFlow(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyIFlow.zip")
        );
        CreateOrUpdateIFlowRequest createIFlowRequest = CreateOrUpdateIFlowRequest.builder()
            .id(API_TEST_DUMMY_IFLOW_NAME)
            .name(API_TEST_DUMMY_IFLOW_NAME)
            .description("IFlow for api tests")
            .build();
        cpiIntegrationFlowClient.createIFlow(
            requestContext,
            packageExternalId,
            createIFlowRequest,
            payload
        );
        return findIFlowIfExist(requestContext, packageExternalId, API_TEST_DUMMY_IFLOW_NAME);
    }

    private CpiArtifact getOrCreateDummyIFlow(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact iFlow = findIFlowIfExist(requestContext, packageExternalId, API_TEST_DUMMY_IFLOW_NAME);
        if (iFlow == null) {
            iFlow = createDummyIFlow(requestContext, packageExternalId);
        }
        return iFlow;
    }

}