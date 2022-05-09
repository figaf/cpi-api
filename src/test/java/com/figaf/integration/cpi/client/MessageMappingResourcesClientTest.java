package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.MessageMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.figaf.integration.cpi.utils.MessageMappingUtils.API_TEST_DUMMY_MESSAGE_MAPPING_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class MessageMappingResourcesClientTest {

    private static MessageMappingResourcesClient messageMappingResourcesClient;
    private static MessageMappingUtils messageMappingUtils;

    @BeforeAll
    static void setUp() {
        messageMappingResourcesClient = new MessageMappingResourcesClient(new HttpClientsFactory());
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        messageMappingUtils = new MessageMappingUtils(
            new PackageUtils(integrationPackageClient),
            new CpiMessageMappingClient(integrationPackageClient, new HttpClientsFactory())
        );
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMessageMappingResources(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact messageMapping = messageMappingUtils.getOrCreateDummyMessageMapping(requestContext);
        assertThat(messageMapping).as("message mapping %s wasn't found", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNotNull();

        List<ArtifactResource> resources = messageMappingResourcesClient.getMessageMappingResources(
            requestContext,
            messageMapping.getPackageExternalId(),
            messageMapping.getExternalId()
        ).getResourceList();
        Set<ArtifactResource> expectedResources = getExpectedResources();
        assertEquals(resources.size(), expectedResources.size());
        resources.forEach(resource -> assertThat(expectedResources).contains(resource));

        messageMappingUtils.deleteMessageMapping(requestContext, messageMapping);
        messageMapping = messageMappingUtils.findDummyMessageMappingInTestPackageIfExist(requestContext);
        assertThat(messageMapping).as("message mapping %s was not deleted", API_TEST_DUMMY_MESSAGE_MAPPING_NAME).isNull();
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