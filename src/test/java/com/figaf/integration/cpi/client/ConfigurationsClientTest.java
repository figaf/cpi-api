package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.configuration.CpiConfigurations;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.StringUtils;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;


/**
 * @author Kostas Charalambous
 */
@Slf4j
class ConfigurationsClientTest {

    private static ConfigurationsClient configurationsClient;

    @BeforeAll
    static void setUp() {
        configurationsClient = new ConfigurationsClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getConfigurations(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        CpiConfigurations cpiConfigurations = configurationsClient.getConfigurations(requestContext);

        boolean isAnyBuildNumberNotEmpty =
                StringUtils.isNotBlank(cpiConfigurations.getIntegrationSuiteTenantGlobalBuildNumber()) ||
                StringUtils.isNotBlank(cpiConfigurations.getCloudIntegrationBuildNumber()) ||
                StringUtils.isNotBlank(cpiConfigurations.getCloudIntegrationRunTimeBuildNumber()) ||
                StringUtils.isNotBlank(cpiConfigurations.getIntegrationAdvisorBuildNumber()) ||
                StringUtils.isNotBlank(cpiConfigurations.getApiManagementBuildNumber());
        Assertions.assertTrue(isAnyBuildNumberNotEmpty, "All build numbers of cpiConfigurations are empty");
    }
}
