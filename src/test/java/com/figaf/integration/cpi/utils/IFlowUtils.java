package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiIntegrationFlowClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateIFlowRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@AllArgsConstructor
@Slf4j
public class IFlowUtils {

    public static final String API_TEST_IFLOW_NAME = "FigafApiTestIFlow";
    public static final String API_TEST_DUMMY_IFLOW_NAME = "FigafApiTestDummyIFlow";

    private final PackageUtils packageUtils;
    private final CpiIntegrationFlowClient cpiIntegrationFlowClient;

    public CpiArtifact findTestIFlowInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findIFlowIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_IFLOW_NAME
        );
    }

    public CpiArtifact findDummyIFlowInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findIFlowIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IFLOW_NAME
        );
    }

    public CpiArtifact createDummyIFlowInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyIFlow.zip")
        );
        return createIFlow(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IFLOW_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyIFlow(RequestContext requestContext) throws IOException {
        CpiArtifact iFlow = findDummyIFlowInTestPackageIfExist(requestContext);
        if (iFlow == null) {
            iFlow = createDummyIFlowInTestPackage(requestContext);
        }
        return iFlow;
    }

    public void deleteIFlow(RequestContext requestContext, CpiArtifact iFlow) {
        cpiIntegrationFlowClient.deleteIFlow(
            iFlow.getPackageExternalId(),
            iFlow.getExternalId(),
            iFlow.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findIFlowIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String iFlowName
    ) {
        List<CpiArtifact> artifacts = cpiIntegrationFlowClient.getIFlowsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> iFlowName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createIFlow(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String iFlowName,
        byte[] payload
    ) {
        CreateOrUpdateIFlowRequest createIFlowRequest = CreateOrUpdateIFlowRequest.builder()
            .id(iFlowName)
            .name(iFlowName)
            .description("IFlow for api tests")
            .build();
        cpiIntegrationFlowClient.createIFlow(
            requestContext,
            packageExternalId,
            createIFlowRequest,
            payload
        );
        return findIFlowIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            iFlowName
        );
    }

}
