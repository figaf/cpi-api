package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.configuration.CpiConfigurations;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * IMPORTANT! Result of configurations API depends on user that is used to execute it!
 * So, that test may fail just because user doesn't have permissions to see statistics for particular part of the IS
 * @author Kostas Charalambous
 * @author Ilya Nesterov
 */
@Slf4j
class ConfigurationsClientTest {

    private static ConfigurationsClient configurationsClient;

    @BeforeAll
    static void setUp() {
        configurationsClient = new ConfigurationsClient(new HttpClientsFactory());
    }

    @Test
    void test_getConfigurationsForIntegrationSuiteAgent_usingIntegrationSuiteUrl() {
        AgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        CpiConfigurations cpiConfigurations = configurationsClient.getConfigurations(requestContext);

        assertThat(cpiConfigurations.getTenantBuildNumber()).isNotBlank();
        assertThat(cpiConfigurations.getCloudIntegrationBuildNumber()).isNotBlank();
        assertThat(cpiConfigurations.getCloudIntegrationRunTimeBuildNumber()).isNotBlank();
        assertThat(cpiConfigurations.getIntegrationAdvisorBuildNumber()).isNotBlank();
        //assertThat(cpiConfigurations.getApiManagementBuildNumber()).isNotBlank();
    }

    @Test
    void test_getConfigurationsForIntegrationSuiteAgent_usingCloudIntegrationUrl() {
        AgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        CpiConfigurations cpiConfigurations = configurationsClient.getConfigurations(requestContext);

        assertThat(cpiConfigurations.getCloudIntegrationBuildNumber()).isNotBlank();
        assertThat(cpiConfigurations.getCloudIntegrationRunTimeBuildNumber()).isNotBlank();
    }

    @Test
    void test_getConfigurationsForNonIntegrationSuiteAgent() {
        AgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForNeo();
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        CpiConfigurations cpiConfigurations = configurationsClient.getConfigurations(requestContext);

        assertThat(cpiConfigurations.getTenantBuildNumber()).isNotBlank();
    }
}
