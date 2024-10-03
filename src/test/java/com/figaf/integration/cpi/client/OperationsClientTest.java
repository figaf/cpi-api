package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.monitoring.RuntimeLocationsResponse;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class OperationsClientTest {

    private static OperationsClient operationsClient;

    @BeforeAll
    static void setUp() {
        operationsClient = new OperationsClient(new HttpClientsFactory());
    }


    @Test
    void test_getRuntimeLocationsForIntegrationSuiteAgent_usingIntegrationSuiteUrl() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();

        RuntimeLocationsResponse runtimeLocationsResponse = operationsClient.getRuntimeLocations(requestContextForWebApiWithIntegrationSuiteUrl);

        assertThat(runtimeLocationsResponse).as("response should contain at least one runtime").isNotNull();
    }
}