package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiSharedMessageMappingClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateSharedMessageMappingRequest;
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
public class SharedMessageMappingUtils {

    public static final String API_TEST_SHARED_MESSAGE_MAPPING_NAME = "FigafApiTestMessageMapping";
    public static final String API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME = "FigafApiTestDummyMessageMapping";

    private final PackageUtils packageUtils;
    private final CpiSharedMessageMappingClient cpiSharedMessageMappingClient;

    public CpiArtifact findTestSharedMessageMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findSharedMessageMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_SHARED_MESSAGE_MAPPING_NAME
        );
    }

    public CpiArtifact findDummySharedMessageMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findSharedMessageMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME
        );
    }

    public CpiArtifact createDummySharedMessageMappingInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMapping.zip")
        );
        return createSharedMessageMapping(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummySharedMessageMapping(RequestContext requestContext) throws IOException {
        CpiArtifact sharedMessageMapping = findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        if (sharedMessageMapping == null) {
            sharedMessageMapping = createDummySharedMessageMappingInTestPackage(requestContext);
        }
        return sharedMessageMapping;
    }

    public void deleteSharedMessageMapping(RequestContext requestContext, CpiArtifact sharedMessageMapping) {
        cpiSharedMessageMappingClient.deleteSharedMessageMapping(
            sharedMessageMapping.getPackageExternalId(),
            sharedMessageMapping.getExternalId(),
            sharedMessageMapping.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findSharedMessageMappingIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String sharedMessageMappingName
    ) {
        List<CpiArtifact> artifacts = cpiSharedMessageMappingClient.getSharedMessageMappingByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> sharedMessageMappingName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createSharedMessageMapping(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String sharedMessageMappingName,
        byte[] payload
    ) {
        CreateOrUpdateSharedMessageMappingRequest createMessageMappingRequest = CreateOrUpdateSharedMessageMappingRequest.builder()
            .id(sharedMessageMappingName)
            .name(sharedMessageMappingName)
            .description("Message mapping for api tests")
            .build();
        cpiSharedMessageMappingClient.createSharedMessageMapping(
            requestContext,
            packageExternalId,
            createMessageMappingRequest,
            payload
        );
        return findSharedMessageMappingIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            sharedMessageMappingName
        );
    }

}
