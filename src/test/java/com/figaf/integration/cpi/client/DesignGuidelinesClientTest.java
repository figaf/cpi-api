package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.design_guidelines.DesignGuidelines;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.utils.IFlowUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static com.figaf.integration.cpi.utils.IFlowUtils.API_TEST_IFLOW_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class DesignGuidelinesClientTest {

    private static DesignGuidelinesClient designGuidelinesClient;
    private static IFlowUtils iFlowUtils;

    @BeforeAll
    static void setUp() {
        designGuidelinesClient = new DesignGuidelinesClient(new HttpClientsFactory());
        CpiIntegrationFlowClient cpiIntegrationFlowClient = new CpiIntegrationFlowClient(new HttpClientsFactory());
        PackageUtils packageUtils = new PackageUtils(new IntegrationPackageClient(new HttpClientsFactory()));
        iFlowUtils = new IFlowUtils(packageUtils, cpiIntegrationFlowClient);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_executeDesignGuidelines(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.findTestIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_IFLOW_NAME).isNotNull();

        DesignGuidelines designGuidelines = designGuidelinesClient.executeDesignGuidelines(
            requestContext,
            iFlow.getPackageExternalId(),
            iFlow.getExternalId(),
            iFlow.getTechnicalName()
        );

        assertThat(designGuidelines).as("failed to execute Design Guidelines").isNotNull();
    }
}