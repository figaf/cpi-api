package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.EDGE_RUNTIME_LOCATION_ID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
class IntegrationContentClientTest {

    private static IntegrationContentClient integrationContentClient;
    private static IntegrationContentWebApiClient integrationContentWebApiClient;

    @BeforeAll
    static void setUp() {
        integrationContentClient = new IntegrationContentClient(new HttpClientsFactory());
        integrationContentWebApiClient = new IntegrationContentWebApiClient(new HttpClientsFactory());
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
        requestContext.setRuntimeLocationId(EDGE_RUNTIME_LOCATION_ID);

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forEdgeRuntime_withIntegrationSuiteUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();
        requestContext.setRuntimeLocationId(EDGE_RUNTIME_LOCATION_ID);

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forDefaultRuntime_withCloudIntegrationUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentWebApiClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @Test
    void test_getAllIntegrationRuntimeArtifacts_usingWebApi_forDefaultRuntime_withIntegrationSuiteUrl() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();

        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentWebApiClient.getAllIntegrationRuntimeArtifacts(requestContext);

        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }
}
