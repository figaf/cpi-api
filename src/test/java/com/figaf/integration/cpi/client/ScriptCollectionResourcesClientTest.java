package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.ScriptCollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.figaf.integration.cpi.utils.ScriptCollectionUtils.API_TEST_DUMMY_SCRIPT_COLLECTION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class ScriptCollectionResourcesClientTest {

    private static ScriptCollectionResourcesClient scriptCollectionResourcesClient;
    private static ScriptCollectionUtils scriptCollectionUtils;

    @BeforeAll
    static void setUp() {
        scriptCollectionResourcesClient = new ScriptCollectionResourcesClient(new HttpClientsFactory());
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        scriptCollectionUtils = new ScriptCollectionUtils(
            new PackageUtils(integrationPackageClient),
            new CpiScriptCollectionClient(integrationPackageClient, new HttpClientsFactory())
        );
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getScriptCollectionResources(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact scriptCollection = scriptCollectionUtils.getOrCreateDummyScriptCollection(requestContext);
        assertThat(scriptCollection).as("script collection %s wasn't found", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNotNull();

        List<ArtifactResource> resources = scriptCollectionResourcesClient.getScriptCollectionResources(
            requestContext,
            scriptCollection.getPackageExternalId(),
            scriptCollection.getExternalId()
        );
        Set<ArtifactResource> expectedResources = getExpectedResources();
        assertEquals(resources.size(), expectedResources.size());
        resources.forEach(resource -> assertThat(expectedResources).contains(resource));

        scriptCollectionUtils.deleteScriptCollection(requestContext, scriptCollection);
        scriptCollection = scriptCollectionUtils.findDummyScriptCollectionInTestPackageIfExist(requestContext);
        assertThat(scriptCollection).as("script collection %s was not deleted", API_TEST_DUMMY_SCRIPT_COLLECTION_NAME).isNull();
    }

    private Set<ArtifactResource> getExpectedResources() {
        Set<ArtifactResource> expectedResources = new HashSet<>();
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("firstGroovy")
                .resourceLocation("script")
                .resourceType("groovy")
                .resourceExtension("groovy")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_SCRIPTS}")
                .build()
        );
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("Firstjs")
                .resourceLocation("script")
                .resourceType("js")
                .resourceExtension("js")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_SCRIPTS}")
                .build()
        );
        return expectedResources;
    }

}