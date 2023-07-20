package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiFunctionLibrariesClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateFunctionLibrariesRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@AllArgsConstructor
@Slf4j
public class FunctionLibrariesUtils {

    public static final String API_TEST_FUNCTION_LIBRARIES_NAME = "FigafApiTestFunctionLibraries";
    public static final String API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME = "FigafApiTestDummyFunctionLibraries";

    private final PackageUtils packageUtils;
    private final CpiFunctionLibrariesClient cpiFunctionLibrariesClient;

    public CpiArtifact findTestFunctionLibrariesInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findFunctionLibrariesIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_FUNCTION_LIBRARIES_NAME
        );
    }

    public CpiArtifact findDummyFunctionLibrariesInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findFunctionLibrariesIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME
        );
    }

    public CpiArtifact createDummyFunctionLibrariesInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyFunctionLibrariesUpdated.zip")
        );
        return createFunctionLibraries(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_FUNCTION_LIBRARIES_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyFunctionLibraries(RequestContext requestContext) throws IOException {
        CpiArtifact functionLibraries = findDummyFunctionLibrariesInTestPackageIfExist(requestContext);
        if (functionLibraries == null) {
            functionLibraries = createDummyFunctionLibrariesInTestPackage(requestContext);
        }
        return functionLibraries;
    }

    public void deleteFunctionLibraries(RequestContext requestContext, CpiArtifact functionLibraries) {
        cpiFunctionLibrariesClient.deleteArtifact(
            functionLibraries.getPackageExternalId(),
            functionLibraries.getExternalId(),
            functionLibraries.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findFunctionLibrariesIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String functionLibrariesName
    ) {
        List<CpiArtifact> artifacts = cpiFunctionLibrariesClient.getFunctionLibrariesByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> functionLibrariesName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createFunctionLibraries(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String functionLibrariesName,
        byte[] payload
    ) {
        CreateFunctionLibrariesRequest createFunctionLibrariesRequest = CreateFunctionLibrariesRequest.builder()
            .id(functionLibrariesName)
            .name(functionLibrariesName)
            .description("Function libraries for api tests")
            .packageExternalId(packageExternalId)
            .bundledModel(payload)
            .build();
        cpiFunctionLibrariesClient.createFunctionLibraries(requestContext, createFunctionLibrariesRequest);
        return findFunctionLibrariesIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            functionLibrariesName
        );
    }

}
