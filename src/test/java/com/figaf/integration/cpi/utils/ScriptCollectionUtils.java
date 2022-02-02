package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiScriptCollectionClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdateScriptCollectionRequest;
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
public class ScriptCollectionUtils {

    public static final String API_TEST_SCRIPT_COLLECTION_NAME = "FigafApiTestScriptCollection";
    public static final String API_TEST_DUMMY_SCRIPT_COLLECTION_NAME = "FigafApiTestDummyScriptCollection";

    private final PackageUtils packageUtils;
    private final CpiScriptCollectionClient cpiScriptCollectionClient;

    public CpiArtifact findTestScriptCollectionInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findScriptCollectionIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_SCRIPT_COLLECTION_NAME
        );
    }

    public CpiArtifact findDummyScriptCollectionInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findScriptCollectionIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME
        );
    }

    public CpiArtifact createDummyScriptCollectionInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyScriptCollectionUpdated.zip")
        );
        return createScriptCollection(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_SCRIPT_COLLECTION_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyScriptCollection(RequestContext requestContext) throws IOException {
        CpiArtifact scriptCollection = findDummyScriptCollectionInTestPackageIfExist(requestContext);
        if (scriptCollection == null) {
            scriptCollection = createDummyScriptCollectionInTestPackage(requestContext);
        }
        return scriptCollection;
    }

    public void deleteScriptCollection(RequestContext requestContext, CpiArtifact scriptCollection) {
        cpiScriptCollectionClient.deleteScriptCollection(
            scriptCollection.getPackageExternalId(),
            scriptCollection.getExternalId(),
            scriptCollection.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findScriptCollectionIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String scriptCollectionName
    ) {
        List<CpiArtifact> artifacts = cpiScriptCollectionClient.getScriptCollectionsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> scriptCollectionName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createScriptCollection(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String scriptCollectionName,
        byte[] payload
    ) {
        CreateOrUpdateScriptCollectionRequest createScriptCollectionRequest = CreateOrUpdateScriptCollectionRequest.builder()
            .id(scriptCollectionName)
            .name(scriptCollectionName)
            .description("Script Collection for api tests")
            .build();
        cpiScriptCollectionClient.createScriptCollection(
            requestContext,
            packageExternalId,
            createScriptCollectionRequest,
            payload
        );
        return findScriptCollectionIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            scriptCollectionName
        );
    }

}
