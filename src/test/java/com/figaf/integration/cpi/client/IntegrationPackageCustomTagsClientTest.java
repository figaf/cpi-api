package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.tags.CustomTag;
import com.figaf.integration.cpi.utils.PackageUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class IntegrationPackageCustomTagsClientTest {

    private static IntegrationPackageCustomTagsClient integrationPackageCustomTagsClient;
    private static PackageUtils packageUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        integrationPackageCustomTagsClient = new IntegrationPackageCustomTagsClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateValueOfCustomTag(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        String nameOfTag = "LOB";
        String delimiterSeparatedTagValues = "test12";

        integrationPackageCustomTagsClient.updateCustomTags(requestContext, integrationPackage.getTechnicalName(), nameOfTag, delimiterSeparatedTagValues);
        List<CustomTag> customTags = integrationPackageCustomTagsClient.getCustomTags(requestContext, integrationPackage.getTechnicalName());

        Optional<CustomTag> tag = customTags.stream().filter(customTag -> customTag.getName().equalsIgnoreCase(nameOfTag)).findFirst();
        String customTagNotFoundError = String.format("custom tag %s is not present", nameOfTag);
        assertThat(tag).as(customTagNotFoundError).isNotEmpty();
        String customTagFailedToUpdateError = String.format("custom tag %s didnt update with new value %s", nameOfTag, delimiterSeparatedTagValues);
        assertThat(tag.get().getValue()).as(customTagFailedToUpdateError).isEqualTo(delimiterSeparatedTagValues);
    }
}