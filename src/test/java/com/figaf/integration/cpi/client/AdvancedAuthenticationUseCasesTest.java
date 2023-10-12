package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
public class AdvancedAuthenticationUseCasesTest {

    private static ConfigurationsClient configurationsClient;
    private static IntegrationPackageClient integrationPackageClient;

    @BeforeAll
    static void setUp() {
        HttpClientsFactory httpClientsFactory = new HttpClientsFactory();
        configurationsClient = new ConfigurationsClient(httpClientsFactory);
        integrationPackageClient = new IntegrationPackageClient(httpClientsFactory);
    }

    @Test
    void test_parallelAccessToWebApiWithDifferentUrlsForIntegrationSuite() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = requestContextForWebApiWithIntegrationSuiteUrl();
        RequestContext requestContextForWebApiWithCloudIntegrationUrl = requestContextForWebApiWithCloudIntegrationUrl();

        List<Supplier<Object>> requestCallbacks = Arrays.asList(
            () -> configurationsClient.getConfigurations(requestContextForWebApiWithIntegrationSuiteUrl),
            () -> integrationPackageClient.getIntegrationPackages(
                    requestContextForWebApiWithCloudIntegrationUrl,
                    format("TechnicalName eq '%s'", "FigafApiTestPackage")
            ),
            () -> configurationsClient.getConfigurations(requestContextForWebApiWithIntegrationSuiteUrl),
            () -> integrationPackageClient.getIntegrationPackages(
                requestContextForWebApiWithCloudIntegrationUrl,
                format("TechnicalName eq '%s'", "FigafApiTestPackage")
            ),
            () -> configurationsClient.getConfigurations(requestContextForWebApiWithIntegrationSuiteUrl),
            () -> integrationPackageClient.getIntegrationPackages(
                requestContextForWebApiWithCloudIntegrationUrl,
                format("TechnicalName eq '%s'", "FigafApiTestPackage")
            ),
            () -> configurationsClient.getConfigurations(requestContextForWebApiWithIntegrationSuiteUrl),
            () -> integrationPackageClient.getIntegrationPackages(
                requestContextForWebApiWithCloudIntegrationUrl,
                format("TechnicalName eq '%s'", "FigafApiTestPackage")
            )
        );

        long fetchedObjectsCount = requestCallbacks.parallelStream()
            .map(Supplier::get)
            .count();
        assertThat(fetchedObjectsCount).isEqualTo(8);
    }

    private RequestContext requestContextForWebApiWithIntegrationSuiteUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = integrationSuiteAgentTestData.createRequestContext(integrationSuiteAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost("figafpartner-1.integrationsuite.cfapps.eu10-003.hana.ondemand.com");
        requestContext.setRestTemplateWrapperKey(integrationSuiteAgentTestData.getTitle() + "_IS");
        return requestContext;
    }

    private RequestContext requestContextForWebApiWithCloudIntegrationUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        return integrationSuiteAgentTestData.createRequestContext(
            integrationSuiteAgentTestData.getTitle()
        );
    }
}
