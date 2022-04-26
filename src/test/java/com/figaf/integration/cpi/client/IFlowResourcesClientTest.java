package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactReference;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.utils.IFlowUtils;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashSet;
import java.util.Set;

import static com.figaf.integration.cpi.utils.IFlowUtils.API_TEST_DUMMY_IFLOW_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class IFlowResourcesClientTest {

    private static IFlowResourcesClient iFlowResourcesClient;
    private static IFlowUtils iFlowUtils;

    @BeforeAll
    static void setUp() {
        iFlowResourcesClient = new IFlowResourcesClient(new HttpClientsFactory());
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        iFlowUtils = new IFlowUtils(
            new PackageUtils(integrationPackageClient),
            new CpiIntegrationFlowClient(integrationPackageClient, new HttpClientsFactory())
        );
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getIFlowResources(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact iFlow = iFlowUtils.getOrCreateDummyIFlow(requestContext);
        assertThat(iFlow).as("iFlow %s wasn't found", API_TEST_DUMMY_IFLOW_NAME).isNotNull();

        ArtifactResources allResources = iFlowResourcesClient.getIFlowResources(
            requestContext,
            iFlow.getPackageExternalId(),
            iFlow.getExternalId()
        );
        Set<ArtifactResource> expectedResources = getExpectedResources();
        Set<ArtifactReference> expectedReferences = getExpectedReferences();

        assertEquals(allResources.getResourceList().size(), expectedResources.size());
        allResources.getResourceList().forEach(resource -> assertThat(expectedResources).contains(resource));

        assertEquals(allResources.getReferenceList().size(), expectedReferences.size());
        allResources.getReferenceList().forEach(reference -> assertThat(expectedReferences).contains(reference));

        iFlowUtils.deleteIFlow(requestContext, iFlow);
        iFlow = iFlowUtils.findDummyIFlowInTestPackageIfExist(requestContext);
        assertThat(iFlow).as("iFlow %s was not deleted", API_TEST_DUMMY_IFLOW_NAME).isNull();
    }

    private Set<ArtifactResource> getExpectedResources() {
        Set<ArtifactResource> expectedResources = new HashSet<>();
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("FigafApiTestDummyMessageMapping")
                .resourceLocation("mapping")
                .resourceType("mmap")
                .resourceExtension("mmap")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_MAPPINGS}")
                .build()
        );
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("receiver-default")
                .resourceLocation("mapping")
                .resourceType("xslt")
                .resourceExtension("xslt")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_MAPPINGS}")
                .build()
        );
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
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("Address")
                .resourceLocation("xsd")
                .resourceType("xsd")
                .resourceExtension("xsd")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_SCHEMAS}")
                .build()
        );
        return expectedResources;
    }

    private Set<ArtifactReference> getExpectedReferences() {
        Set<ArtifactReference> expectedReferences = new HashSet<>();
        expectedReferences.add(
            ArtifactReference.builder()
                .packageName("FigafApiTestPackage")
                .packageTechnicalName("FigafApiTestPackage")
                .type("ScriptCollection")
                .resourceTypes(new Object[0])
                .resources(new Object[0])
                .name("FigafApiTestScriptCollection")
                .bundleSymbolicName("FigafApiTestScriptCollection")
                .build()
        );
        expectedReferences.add(
            ArtifactReference.builder()
                .packageName("FigafApiTestPackage")
                .packageTechnicalName("FigafApiTestPackage")
                .type("MessageMapping")
                .resourceTypes(new Object[0])
                .resources(new Object[0])
                .name("FigafApiTestMessageMapping")
                .bundleSymbolicName("FigafApiTestMessageMapping")
                .build()
        );
        return expectedReferences;
    }

}