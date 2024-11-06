package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        integrationContentClient = new IntegrationContentClient(new HttpClientsFactory());
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingPublicApi() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forEdgeRuntime_withCloudIntegrationUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();
        requestContext.setRuntimeLocationId("azureedge3");

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forEdgeRuntime_withIntegrationSuiteUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();
        requestContext.setRuntimeLocationId("azureedge3");

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forDefaultRuntime_withCloudIntegrationUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();
        requestContext.setRuntimeLocationId("cloudintegration");

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forDefaultRuntime_withIntegrationSuiteUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();
        requestContext.setRuntimeLocationId("cloudintegration");

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }
}
