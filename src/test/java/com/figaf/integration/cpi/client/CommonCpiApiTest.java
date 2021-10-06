package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.SetUtils.hashSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
class CommonCpiApiTest {

    private static final String API_TEST_PACKAGE_NAME = "FigafApiTestPackage";
    private static final String API_DELETE_TEST_PACKAGE_NAME = "FigafApiDeleteTestPackage";
    private static final String API_TEST_IFLOW_NAME = "FigafApiTestIFlow";
    private static final String API_TEST_VALUE_MAPPING_NAME = "FigafApiTestValueMapping";

    private static IntegrationContentClient integrationContentClient;
    private static IntegrationPackageClient integrationPackageClient;
    private static CpiIntegrationFlowClient cpiIntegrationFlowClient;

    @BeforeAll
    static void setUp() {
        integrationContentClient = new IntegrationContentClient(new HttpClientsFactory());
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiIntegrationFlowClient = new CpiIntegrationFlowClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_privateApiRead(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(requestContext, null);
        log.debug("{} integrationPackages were found", integrationPackages.size());
        assertThat(integrationPackages).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_publicApiRead(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);
        log.debug("{} integrationRuntimeArtifacts were found", integrationRuntimeArtifacts.size());
        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPublicApiRead2(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<CpiExternalConfiguration> cpiExternalConfigurations = integrationContentClient.getCpiExternalConfigurations(requestContext, "Figaf_iflow_1");
        log.debug("{} cpiExternalConfigurations were found", cpiExternalConfigurations.size());
        assertThat(cpiExternalConfigurations).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPrivateApiWrite(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(requestContext, "TechnicalName eq 'Figaftest2'");
        assertThat(integrationPackages).isNotEmpty();
        IntegrationPackage integrationPackage = integrationPackages.get(0);

        CreateOrUpdatePackageRequest createOrUpdatePackageRequest = new CreateOrUpdatePackageRequest();
        createOrUpdatePackageRequest.setTechnicalName(integrationPackage.getTechnicalName());
        createOrUpdatePackageRequest.setDisplayName(integrationPackage.getDisplayedName());
        createOrUpdatePackageRequest.setShortDescription("Updated at " + new Date());
        createOrUpdatePackageRequest.setVendor(integrationPackage.getVendor());
        createOrUpdatePackageRequest.setVersion(integrationPackage.getVersion());

        integrationPackageClient.updateIntegrationPackage(requestContext, integrationPackage.getExternalId(), createOrUpdatePackageRequest);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPublicApiWrite(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        int numberOfUploadedConfigurations = integrationContentClient.uploadCpiExternalConfiguration(
                requestContext,
                "Figaf_iflow_1",
                Collections.singletonList(
                        new CpiExternalConfiguration("endpoint", "/figaf_iflow_3", "xsd:string")
                )
        );
        assertThat(numberOfUploadedConfigurations).isEqualTo(1);
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_with_parallel_authorization() throws InterruptedException {
        List<IntegrationContent> integrationRuntimeArtifacts1 = new ArrayList<>();
        List<IntegrationContent> integrationRuntimeArtifacts2 = new ArrayList<>();
        Thread thread1 = new Thread(() -> {
            AgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForCf1();
            integrationRuntimeArtifacts1.addAll(integrationContentClient.getAllIntegrationRuntimeArtifacts(agentTestData.createRequestContext(agentTestData.getTitle())));
        });
        Thread thread2 = new Thread(() -> {
            AgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForCf1();
            integrationRuntimeArtifacts2.addAll(integrationContentClient.getAllIntegrationRuntimeArtifacts(agentTestData.createRequestContext(agentTestData.getTitle())));
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        log.debug("{} integration runtime artifacts were found in first request", integrationRuntimeArtifacts1.size());
        assertThat(integrationRuntimeArtifacts1).isNotEmpty();

        log.debug("{} integration runtime artifacts were found in second request", integrationRuntimeArtifacts2.size());
        assertThat(integrationRuntimeArtifacts2).isNotEmpty();

        assertThat(integrationRuntimeArtifacts1.size()).isEqualTo(integrationRuntimeArtifacts2.size());

    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_privatePackageApiDelete(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = getOrCreatePackage(requestContext, API_DELETE_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_DELETE_TEST_PACKAGE_NAME).isNotNull();
        integrationPackageClient.deletePackage(API_DELETE_TEST_PACKAGE_NAME, requestContext);
        integrationPackage = findPackageByNameIfExist(requestContext, API_DELETE_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't deleted", API_DELETE_TEST_PACKAGE_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_privateArtifactApiDelete(AgentTestData agentTestData) throws IOException {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = getOrCreatePackage(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact iFlow = getOrCreateDummyIFlow(requestContext, integrationPackage.getExternalId());
        assertThat(iFlow).as("IFlow %s wasn't found", API_TEST_IFLOW_NAME).isNotNull();

        CpiArtifact valueMapping = getOrCreateDummyValueMapping(requestContext, integrationPackage.getExternalId());
        assertThat(valueMapping).as("ValueMapping %s wasn't found", API_TEST_VALUE_MAPPING_NAME).isNotNull();

        cpiIntegrationFlowClient.deleteArtifact(
            integrationPackage.getExternalId(),
            iFlow.getExternalId(),
            API_TEST_IFLOW_NAME,
            requestContext
        );

        cpiIntegrationFlowClient.deleteArtifact(
            integrationPackage.getExternalId(),
            valueMapping.getExternalId(),
            API_TEST_VALUE_MAPPING_NAME,
            requestContext
        );

        iFlow = findDummyIFlowIfExist(requestContext);
        assertThat(iFlow).as("IFlow %s wasn't deleted", API_TEST_IFLOW_NAME).isNull();

        valueMapping = findDummyValueMappingIfExist(requestContext);
        assertThat(valueMapping).as("ValueMapping %s wasn't deleted", API_TEST_VALUE_MAPPING_NAME).isNull();
    }

    private IntegrationPackage findPackageByNameIfExist(RequestContext requestContext, String packageName) {
        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(
            requestContext,
            format("TechnicalName eq '%s'", packageName)
        );
        if (isNotEmpty(integrationPackages)) {
            return integrationPackages.get(0);
        }
        return null;
    }

    private IntegrationPackage createDummyPackage(RequestContext requestContext, String packageName) {
        CreateOrUpdatePackageRequest createOrUpdatePackageRequest = new CreateOrUpdatePackageRequest();
        createOrUpdatePackageRequest.setTechnicalName(packageName);
        createOrUpdatePackageRequest.setDisplayName(packageName);
        createOrUpdatePackageRequest.setShortDescription("Package for cpi api tests");
        integrationPackageClient.createIntegrationPackage(requestContext, createOrUpdatePackageRequest);
        return findPackageByNameIfExist(requestContext, packageName);
    }

    private IntegrationPackage getOrCreatePackage(RequestContext requestContext, String packageName) {
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, packageName);
        if (integrationPackage == null) {
            integrationPackage = createDummyPackage(requestContext, packageName);
        }
        return integrationPackage;
    }

    private CpiArtifact findDummyIFlowIfExist(RequestContext requestContext) {
        List<CpiArtifact> artifacts = cpiIntegrationFlowClient.getArtifactsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            API_TEST_IFLOW_NAME,
            hashSet("CPI_IFLOW")
        );

        return artifacts.stream().filter(cpiArtifact -> API_TEST_IFLOW_NAME.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyIFlow(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestIFlow.zip")
        );
        CreateOrUpdateIFlowRequest createIFlowRequest = new CreateOrUpdateIFlowRequest();
        createIFlowRequest.setId(API_TEST_IFLOW_NAME);
        createIFlowRequest.setName(API_TEST_IFLOW_NAME);
        createIFlowRequest.setDescription("IFlow for api tests");
        cpiIntegrationFlowClient.createIntegrationFlow(
            requestContext,
            packageExternalId,
            createIFlowRequest,
            payload
        );
        return findDummyIFlowIfExist(requestContext);
    }

    private CpiArtifact getOrCreateDummyIFlow(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact iFlow = findDummyIFlowIfExist(requestContext);
        if (iFlow == null) {
            iFlow = createDummyIFlow(requestContext, packageExternalId);
        }
        return iFlow;
    }

    private CpiArtifact findDummyValueMappingIfExist(RequestContext requestContext) {
        List<CpiArtifact> artifacts = cpiIntegrationFlowClient.getArtifactsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            API_TEST_VALUE_MAPPING_NAME,
            hashSet("VALUE_MAPPING")
        );

        return artifacts.stream().filter(cpiArtifact -> API_TEST_VALUE_MAPPING_NAME.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyValueMapping(
        RequestContext requestContext,
        String packageExternalId
    ) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestValueMapping.zip")
        );
        CreateOrUpdateValueMappingRequest createValueMappingRequest = new CreateOrUpdateValueMappingRequest();
        createValueMappingRequest.setId(API_TEST_VALUE_MAPPING_NAME);
        createValueMappingRequest.setName(API_TEST_VALUE_MAPPING_NAME);
        createValueMappingRequest.setDescription("Value Mapping for api tests");
        cpiIntegrationFlowClient.createValueMapping(
            requestContext,
            packageExternalId,
            createValueMappingRequest,
            payload
        );
        return findDummyValueMappingIfExist(requestContext);
    }

    private CpiArtifact getOrCreateDummyValueMapping(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact valueMapping = findDummyValueMappingIfExist(requestContext);
        if (valueMapping == null) {
            valueMapping = createDummyValueMapping(requestContext, packageExternalId);
        }
        return valueMapping;
    }

}