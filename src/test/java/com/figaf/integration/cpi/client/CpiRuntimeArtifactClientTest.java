package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class CpiRuntimeArtifactClientTest {

    private static CpiRuntimeArtifactClient cpiRuntimeArtifactClient;
    private static CpiIntegrationFlowClient cpiIntegrationFlowClient;
    private static PackageUtils packageUtils;


    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiRuntimeArtifactClient = new CpiRuntimeArtifactClient(new HttpClientsFactory());
        cpiIntegrationFlowClient = new CpiIntegrationFlowClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
    }


    @Test
    void test_executeVersionsHistory() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContextForWebApiWithIntegrationSuiteUrl);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> iFlows = cpiIntegrationFlowClient.getIFlowsByPackage(
            requestContextForWebApiWithIntegrationSuiteUrl,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        CpiArtifact cpiArtifact = iFlows.get(0);
        cpiRuntimeArtifactClient.executeVersionsHistory(
            requestContextForWebApiWithIntegrationSuiteUrl,
            integrationPackage.getExternalId(),
            cpiArtifact.getExternalId()
        );
    }
}
