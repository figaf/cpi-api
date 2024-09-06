package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiValueMappingClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateValueMappingRequest;
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
public class ValueMappingUtils {

    public static final String API_TEST_VALUE_MAPPING_NAME = "FigafApiTestValueMapping";
    public static final String API_TEST_DUMMY_VALUE_MAPPING_NAME = "FigafApiTestDummyValueMapping";

    private final PackageUtils packageUtils;
    private final CpiValueMappingClient cpiValueMappingClient;

    public CpiArtifact findTestValueMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findValueMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_VALUE_MAPPING_NAME
        );
    }

    public CpiArtifact findDummyValueMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findValueMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_VALUE_MAPPING_NAME
        );
    }

    public CpiArtifact createDummyValueMappingInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyValueMapping.zip")
        );
        return createValueMapping(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_VALUE_MAPPING_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyValueMapping(RequestContext requestContext) throws IOException {
        CpiArtifact valueMapping = findDummyValueMappingInTestPackageIfExist(requestContext);
        if (valueMapping == null) {
            valueMapping = createDummyValueMappingInTestPackage(requestContext);
        }
        return valueMapping;
    }

    public void deleteValueMapping(RequestContext requestContext, CpiArtifact valueMapping) {
        cpiValueMappingClient.deleteValueMapping(
            valueMapping.getPackageExternalId(),
            valueMapping.getExternalId(),
            valueMapping.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findValueMappingIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String valueMappingName
    ) {
        List<CpiArtifact> artifacts = cpiValueMappingClient.getValueMappingsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> valueMappingName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createValueMapping(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String valueMappingName,
        byte[] payload
    ) {
        CreateValueMappingRequest createValueMappingRequest = CreateValueMappingRequest.builder()
            .id(valueMappingName)
            .name(valueMappingName)
            .description("Value Mapping for api tests")
            .packageExternalId(packageExternalId)
            .packageTechnicalName(API_TEST_PACKAGE_NAME)
            .bundledModel(payload)
            .build();
        cpiValueMappingClient.createValueMapping(requestContext, createValueMappingRequest);
        return findValueMappingIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            valueMappingName
        );
    }

}
