package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
}