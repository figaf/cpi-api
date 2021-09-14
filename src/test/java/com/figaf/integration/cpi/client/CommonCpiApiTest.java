package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdatePackageRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
class CommonCpiApiTest {

    private static IntegrationContentClient integrationContentClient;
    private static IntegrationPackageClient integrationPackageClient;

    @BeforeAll
    static void setUp() {
        integrationContentClient = new IntegrationContentClient(new HttpClientsFactory());
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
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

}