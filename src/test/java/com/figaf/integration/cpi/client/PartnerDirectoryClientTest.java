package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.partner_directory.*;
import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
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
class PartnerDirectoryClientTest {

    private static PartnerDirectoryClient partnerDirectoryClient;
    private PartnerDirectoryParameter partnerDirectoryParameterForDataCleaning;
    private RequestContext requestContextForDataCleaning;

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

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createBinaryParameter(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        BinaryParameterCreationRequest binaryParameterCreationRequest = new BinaryParameterCreationRequest(
            "test_param_to_check_uniqueness",
            "21341235",
            "ewogdmFsdWUgOiAidGVzdCIKfQ==",
            "json"
        );

        partnerDirectoryClient.createBinaryParameter(binaryParameterCreationRequest, requestContext);
        PartnerDirectoryParameter partnerDirectoryParameter = partnerDirectoryClient.retrieveBinaryParameter(
            binaryParameterCreationRequest.getId(),
            binaryParameterCreationRequest.getPid(),
            requestContext
        );

        this.partnerDirectoryParameterForDataCleaning = partnerDirectoryParameter;
        this.requestContextForDataCleaning = requestContext;
        assertThat(partnerDirectoryParameter.getId()).as("binary parameter doesnt exist").isEqualTo(binaryParameterCreationRequest.getId());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createStringParameter(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        StringParameterCreationRequest stringParameterCreationRequest = new StringParameterCreationRequest(
            "test_string_param_to_check_uniqueness",
            "213241235",
            "test"
        );

        partnerDirectoryClient.createStringParameter(stringParameterCreationRequest, requestContext);
        PartnerDirectoryParameter partnerDirectoryParameter = partnerDirectoryClient.retrieveStringParameter(
            stringParameterCreationRequest.getId(),
            stringParameterCreationRequest.getPid(),
            requestContext
        );

        this.partnerDirectoryParameterForDataCleaning = partnerDirectoryParameter;
        this.requestContextForDataCleaning = requestContext;
        assertThat(partnerDirectoryParameter.getId()).as("string parameter doesnt exist").isEqualTo(stringParameterCreationRequest.getId());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateBinaryParameters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        BinaryParameterCreationRequest binaryParameterCreationRequest = new BinaryParameterCreationRequest(
            "test_param_to_check_uniqueness",
            "21341235",
            "ewogdmFsdWUgOiAidGVzdCIKfQ==",
            "json"
        );
        partnerDirectoryClient.createBinaryParameter(binaryParameterCreationRequest, requestContext);
        PartnerDirectoryParameter partnerDirectoryParameterBeforeUpdate = partnerDirectoryClient.retrieveBinaryParameter(
            binaryParameterCreationRequest.getId(),
            binaryParameterCreationRequest.getPid(),
            requestContext
        );
        this.partnerDirectoryParameterForDataCleaning = partnerDirectoryParameterBeforeUpdate;
        this.requestContextForDataCleaning = requestContext;
        BinaryParameterUpdateRequest binaryParameterUpdateRequest = new BinaryParameterUpdateRequest(
            "T2JqZWN0IFR5cGU7UGFja2FnZSBOYW1lOw==",
            "xml"
        );

        partnerDirectoryClient.updateBinaryParameter(
            partnerDirectoryParameterBeforeUpdate.getId(),
            partnerDirectoryParameterBeforeUpdate.getPid(),
            binaryParameterUpdateRequest,
            requestContext
        );
        PartnerDirectoryParameter partnerDirectoryParameterAfterUpdate = partnerDirectoryClient.retrieveBinaryParameter(
            partnerDirectoryParameterBeforeUpdate.getId(),
            partnerDirectoryParameterBeforeUpdate.getPid(),
            requestContext
        );

        assertThat(partnerDirectoryParameterAfterUpdate.getValue()).as("binary parameter value wasn't updated").isEqualTo(binaryParameterUpdateRequest.getValue());
        assertThat(partnerDirectoryParameterAfterUpdate.getContentType()).as("binary parameter value wasn't updated").isEqualTo(binaryParameterUpdateRequest.getContentType());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateStringParameter(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        StringParameterCreationRequest stringParameterCreationRequest = new StringParameterCreationRequest(
            "test_string_param_to_check_uniqueness",
            "213241235",
            "test"
        );
        partnerDirectoryClient.createStringParameter(stringParameterCreationRequest, requestContext);
        PartnerDirectoryParameter partnerDirectoryParameterBeforeUpdate = partnerDirectoryClient.retrieveStringParameter(
            stringParameterCreationRequest.getId(),
            stringParameterCreationRequest.getPid(),
            requestContext
        );
        this.partnerDirectoryParameterForDataCleaning = partnerDirectoryParameterBeforeUpdate;
        this.requestContextForDataCleaning = requestContext;
        StringParameterUpdateRequest stringParameterUpdateRequest = new StringParameterUpdateRequest("testUpdate");

        partnerDirectoryClient.updateStringParameter(
            partnerDirectoryParameterBeforeUpdate.getId(),
            partnerDirectoryParameterBeforeUpdate.getPid(),
            stringParameterUpdateRequest,
            requestContext
        );
        PartnerDirectoryParameter partnerDirectoryParameterAfterUpdate = partnerDirectoryClient.retrieveStringParameter(
            partnerDirectoryParameterBeforeUpdate.getId(),
            partnerDirectoryParameterBeforeUpdate.getPid(),
            requestContext
        );

        assertThat(partnerDirectoryParameterAfterUpdate.getValue())
            .as("string parameter value wasn't updated")
            .isEqualTo(stringParameterUpdateRequest.getValue());
    }

    @AfterEach
    void tearDown() {
        if (!Optional.ofNullable(this.partnerDirectoryParameterForDataCleaning).isPresent()) {
            return;
        }
        try {
            if (this.partnerDirectoryParameterForDataCleaning.getType().equals(TypeOfParam.BINARY_PARAMETER)) {
                partnerDirectoryClient.deleteBinaryParameter(
                    this.partnerDirectoryParameterForDataCleaning.getId(),
                    this.partnerDirectoryParameterForDataCleaning.getPid(),
                    requestContextForDataCleaning
                );
            } else {
                partnerDirectoryClient.deleteStringParameter(
                    this.partnerDirectoryParameterForDataCleaning.getId(),
                    this.partnerDirectoryParameterForDataCleaning.getPid(),
                    requestContextForDataCleaning
                );
            }
            log.debug("Cleaned up test data for parameter ID: {}", partnerDirectoryParameterForDataCleaning.getId());
        } catch (Exception e) {
            log.error("Failed to clean up test data for parameter ID: {}", partnerDirectoryParameterForDataCleaning.getId(), e);
        }
        this.partnerDirectoryParameterForDataCleaning = null;
        this.requestContextForDataCleaning = null;
    }
}