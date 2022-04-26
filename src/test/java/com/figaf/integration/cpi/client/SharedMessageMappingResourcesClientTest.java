package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.SharedMessageMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.figaf.integration.cpi.utils.SharedMessageMappingUtils.API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class SharedMessageMappingResourcesClientTest {

    private static SharedMessageMappingResourcesClient sharedMessageMappingResourcesClient;
    private static SharedMessageMappingUtils sharedMessageMappingUtils;

    @BeforeAll
    static void setUp() {
        sharedMessageMappingResourcesClient = new SharedMessageMappingResourcesClient(new HttpClientsFactory());
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        sharedMessageMappingUtils = new SharedMessageMappingUtils(
            new PackageUtils(integrationPackageClient),
            new CpiSharedMessageMappingClient(integrationPackageClient, new HttpClientsFactory())
        );
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSharedMessageMappingResources(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = sharedMessageMappingUtils.getOrCreateDummySharedMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNotNull();

        List<ArtifactResource> resources = sharedMessageMappingResourcesClient.getSharedMessageMappingResources(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMapping.getExternalId()
        ).getResourceList();
        Set<ArtifactResource> expectedResources = getExpectedResources();
        assertEquals(resources.size(), expectedResources.size());
        resources.forEach(resource -> assertThat(expectedResources).contains(resource));

        sharedMessageMappingUtils.deleteSharedMessageMapping(requestContext, messageMapping);
        messageMapping = sharedMessageMappingUtils.findDummySharedMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_SHARED_MESSAGE_MAPPING_NAME).isNull();
    }

    private Set<ArtifactResource> getExpectedResources() {
        Set<ArtifactResource> expectedResources = new HashSet<>();
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("Address")
                .resourceLocation("xsd")
                .resourceType("xsd")
                .resourceExtension("xsd")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_SCHEMAS}")
                .build()
        );
        expectedResources.add(
            ArtifactResource.builder()
                .resourceName("FigafApiTestDummyMessageMapping")
                .resourceLocation("mapping")
                .resourceType("mmap")
                .resourceExtension("mmap")
                .resourceCategory("{com.sap.it.spc.myproj.i18n>CATEGORY_MAPPINGS}")
                .build()
        );
        return expectedResources;
    }

}