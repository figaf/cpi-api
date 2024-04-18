package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.partner_directory.PartnerDirectoryParameter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class PartnerDirectoryClientTest {

    private static PartnerDirectoryClient partnerDirectoryClient;

    @BeforeAll
    static void setUp() {
        partnerDirectoryClient = new PartnerDirectoryClient(new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrieveBinaryParameters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<PartnerDirectoryParameter> binaryParameters = partnerDirectoryClient.retrieveBinaryParameters(requestContext);

        assertThat(binaryParameters).as("binaryParameters shouldn't be empty").isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrieveStringParameters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<PartnerDirectoryParameter> stringParameters = partnerDirectoryClient.retrieveStringParameters(requestContext);

        assertThat(stringParameters).as("stringParameters shouldn't be empty").isNotEmpty();
    }
}