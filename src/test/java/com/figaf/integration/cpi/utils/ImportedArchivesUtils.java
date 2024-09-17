package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiImportedArchivesClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateImportedArchivesRequest;
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
public class ImportedArchivesUtils {

    public static final String API_TEST_IMPORTED_ARCHIVES_NAME = "FigafApiTestImportedArchives";
    public static final String API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME = "FigafApiTestDummyImportedArchives";

    private final PackageUtils packageUtils;
    private final CpiImportedArchivesClient cpiImportedArchivesClient;

    public CpiArtifact findTestImportedArchivesInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findImportedArchivesIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_IMPORTED_ARCHIVES_NAME
        );
    }

    public CpiArtifact findDummyImportedArchivesInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findImportedArchivesIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME
        );
    }

    public CpiArtifact createDummyImportedArchivesInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyImportedArchivesUpdated.zip")
        );
        return createImportedArchives(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_IMPORTED_ARCHIVES_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyImportedArchives(RequestContext requestContext) throws IOException {
        CpiArtifact importedArchives = findDummyImportedArchivesInTestPackageIfExist(requestContext);
        if (importedArchives == null) {
            importedArchives = createDummyImportedArchivesInTestPackage(requestContext);
        }
        return importedArchives;
    }

    public void deleteImportedArchives(RequestContext requestContext, CpiArtifact importedArchives) {
        cpiImportedArchivesClient.deleteArtifact(
            importedArchives.getPackageExternalId(),
            importedArchives.getExternalId(),
            importedArchives.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findImportedArchivesIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String importedArchivesName
    ) {
        List<CpiArtifact> artifacts = cpiImportedArchivesClient.getImportedArchivesByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> importedArchivesName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createImportedArchives(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String importedArchivesName,
        byte[] payload
    ) {
        CreateImportedArchivesRequest createImportedArchivesRequest = CreateImportedArchivesRequest.builder()
            .id(importedArchivesName)
            .name(importedArchivesName)
            .description("Imported archives for api tests")
            .packageExternalId(packageExternalId)
            .packageTechnicalName(API_TEST_PACKAGE_NAME)
            .bundledModel(payload)
            .build();
        cpiImportedArchivesClient.createImportedArchives(requestContext, createImportedArchivesRequest);
        return findImportedArchivesIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            importedArchivesName
        );
    }

}
