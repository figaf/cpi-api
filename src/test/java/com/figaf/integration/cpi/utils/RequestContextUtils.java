package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;

import static com.figaf.integration.cpi.utils.Constants.CLOUD_INTEGRATION_RUNTIME_LOCATION_ID;

public class RequestContextUtils {

    public static RequestContext createRequestContextForWebApiWithIntegrationSuiteUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = integrationSuiteAgentTestData.createRequestContext(integrationSuiteAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost("figafpartner-1.integrationsuite.cfapps.eu10-003.hana.ondemand.com");
        requestContext.setRestTemplateWrapperKey(integrationSuiteAgentTestData.getTitle() + "_IS");
        requestContext.setDefaultRuntimeLocationId(CLOUD_INTEGRATION_RUNTIME_LOCATION_ID);
        requestContext.setRuntimeLocationId(CLOUD_INTEGRATION_RUNTIME_LOCATION_ID);
        return requestContext;
    }

    public static RequestContext createRequestContextForWebApiWithCloudIntegrationUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = integrationSuiteAgentTestData.createRequestContext(
            integrationSuiteAgentTestData.getTitle()
        );
        requestContext.setDefaultRuntimeLocationId(CLOUD_INTEGRATION_RUNTIME_LOCATION_ID);
        requestContext.setRuntimeLocationId(CLOUD_INTEGRATION_RUNTIME_LOCATION_ID);
        return requestContext;
    }
}
