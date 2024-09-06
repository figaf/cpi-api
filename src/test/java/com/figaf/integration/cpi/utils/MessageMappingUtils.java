package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiMessageMappingClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateMessageMappingRequest;
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
public class MessageMappingUtils {

    public static final String API_TEST_MESSAGE_MAPPING_NAME = "FigafApiTestMessageMapping";
    public static final String API_TEST_DUMMY_MESSAGE_MAPPING_NAME = "FigafApiTestDummyMessageMapping";

    private final PackageUtils packageUtils;
    private final CpiMessageMappingClient cpiMessageMappingClient;

    public CpiArtifact findTestMessageMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findMessageMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_MESSAGE_MAPPING_NAME
        );
    }

    public CpiArtifact findDummyMessageMappingInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findMessageMappingIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_MESSAGE_MAPPING_NAME
        );
    }

    public CpiArtifact createDummyMessageMappingInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyMessageMapping.zip")
        );
        return createMessageMapping(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_MESSAGE_MAPPING_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyMessageMapping(RequestContext requestContext) throws IOException {
        CpiArtifact messageMapping = findDummyMessageMappingInTestPackageIfExist(requestContext);
        if (messageMapping == null) {
            messageMapping = createDummyMessageMappingInTestPackage(requestContext);
        }
        return messageMapping;
    }

    public void deleteMessageMapping(RequestContext requestContext, CpiArtifact messageMapping) {
        cpiMessageMappingClient.deleteMessageMapping(
            messageMapping.getPackageExternalId(),
            messageMapping.getExternalId(),
            messageMapping.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findMessageMappingIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String messageMappingName
    ) {
        List<CpiArtifact> artifacts = cpiMessageMappingClient.getMessageMappingsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> messageMappingName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createMessageMapping(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String messageMappingName,
        byte[] payload
    ) {
        CreateMessageMappingRequest createMessageMappingRequest = CreateMessageMappingRequest.builder()
            .id(messageMappingName)
            .name(messageMappingName)
            .description("Message mapping for api tests")
            .packageExternalId(packageExternalId)
            .packageTechnicalName(API_TEST_PACKAGE_NAME)
            .bundledModel(payload)
            .build();
        cpiMessageMappingClient.createMessageMapping(requestContext, createMessageMappingRequest);
        return findMessageMappingIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            messageMappingName
        );
    }

}
