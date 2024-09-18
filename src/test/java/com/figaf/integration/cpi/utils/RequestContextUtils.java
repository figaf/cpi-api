package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;

public class RequestContextUtils {

    public static RequestContext createRequestContextForWebApiWithIntegrationSuiteUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = integrationSuiteAgentTestData.createRequestContext(integrationSuiteAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost("figafpartner-1.integrationsuite.cfapps.eu10-003.hana.ondemand.com");
        requestContext.setRestTemplateWrapperKey(integrationSuiteAgentTestData.getTitle() + "_IS");
        return requestContext;
    }

    public static RequestContext createRequestContextForWebApiWithCloudIntegrationUrl() {
        AgentTestData integrationSuiteAgentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        return integrationSuiteAgentTestData.createRequestContext(
            integrationSuiteAgentTestData.getTitle()
        );
    }
}
