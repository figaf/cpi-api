package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.tags.CustomTagsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class CustomTagsConfigurationClientTest {

    private static CustomTagsConfigurationClient customTagsConfigurationClient;

    @BeforeAll
    static void setUp() {
        customTagsConfigurationClient = new CustomTagsConfigurationClient(new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createCustomTagsConfiguration(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<CustomTagsConfiguration> existingCustomTagConfigurations = customTagsConfigurationClient.getCustomTagsConfiguration(requestContext);
        List<CustomTagsConfiguration> customTags = Lists.list(CustomTagsConfiguration.builder().tagName("revision").isMandatory(false).build(), CustomTagsConfiguration.builder().tagName("LOB").isMandatory(false).build(), CustomTagsConfiguration.builder().tagName("Team").isMandatory(false).build());

        customTagsConfigurationClient.createCustomTagsConfiguration(requestContext, customTags);
        List<CustomTagsConfiguration> customTagConfigurations = customTagsConfigurationClient.getCustomTagsConfiguration(requestContext);

        assertThat(customTagConfigurations).isEqualTo(customTags);

        //revert custom tags to existing state
        customTagsConfigurationClient.createCustomTagsConfiguration(requestContext, existingCustomTagConfigurations);
        List<CustomTagsConfiguration> revertedCustomTags = customTagsConfigurationClient.getCustomTagsConfiguration(requestContext);
        assertThat(existingCustomTagConfigurations).isEqualTo(revertedCustomTags);

    }

}