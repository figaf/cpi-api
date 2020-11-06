package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
class IntegrationContentClientTest {

    private static IntegrationContentClient integrationContentClient;

    @BeforeAll
    static void setUp() {
        integrationContentClient = new IntegrationContentClient(
                "https://accounts.sap.com/saml2/idp/sso",
                new HttpClientsFactory()
        );
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllIntegrationRuntimeArtifacts(AgentTestData agentTestData) {
        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(agentTestData.createRequestContext());
        log.debug("{} integration runtime artifacts were found", integrationRuntimeArtifacts.size());
        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }


    @Test
    void test_parallelAuthorization() throws InterruptedException {
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