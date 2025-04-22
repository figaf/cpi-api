package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.AdditionalAttributes;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.runtime_artifacts.VersionHistoryRecord;
import com.figaf.integration.cpi.utils.IFlowUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CpiRuntimeArtifactClientTest {

    private static CpiRuntimeArtifactClient cpiRuntimeArtifactClient;
    private static CpiIntegrationFlowClient cpiIntegrationFlowClient;
    private static PackageUtils packageUtils;
    private static IFlowUtils iFlowUtils;


    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiRuntimeArtifactClient = new CpiRuntimeArtifactClient(new HttpClientsFactory());
        cpiIntegrationFlowClient = new CpiIntegrationFlowClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        iFlowUtils = new IFlowUtils(packageUtils, cpiIntegrationFlowClient);
    }


    @Test
    void test_getArtifactVersionsHistory() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContextForWebApiWithIntegrationSuiteUrl);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> iFlows = cpiIntegrationFlowClient.getIFlowsByPackage(
                requestContextForWebApiWithIntegrationSuiteUrl,
                API_TEST_PACKAGE_NAME,
                API_TEST_PACKAGE_NAME,
                integrationPackage.getExternalId()
        );

        iFlows.forEach(iFlow -> {
            List<VersionHistoryRecord> versionHistoryRecords = cpiRuntimeArtifactClient.getArtifactVersionsHistory(
                    requestContextForWebApiWithIntegrationSuiteUrl,
                    integrationPackage.getExternalId(),
                    iFlow.getExternalId()
            );
            assertNotNull(versionHistoryRecords,
                    String.format("Version history records for iFlow '%s' should not be null", iFlow.getDisplayedName()));

        });
    }

    @Test
    void test_getArtifactAdditionalAttributes() {
        RequestContext requestContext = RequestContextUtils.createRequestContextForWebApiWithCloudIntegrationUrl();
        CpiArtifact testIFlow = iFlowUtils.findTestIFlowInTestPackageIfExist(requestContext);

        AdditionalAttributes additionalAttributes = cpiIntegrationFlowClient.getArtifactAdditionalAttributes(
            testIFlow.getPackageExternalId(),
            testIFlow.getExternalId(),
            requestContext
        );

        assertNotNull(additionalAttributes);
    }
}
