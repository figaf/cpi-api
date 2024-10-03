package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.partner_directory.*;
import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kostas Charalambous
 */
@Slf4j
class PartnerDirectoryClientTest {

    private static PartnerDirectoryClient partnerDirectoryClient;
    private ParameterDataForCleaning parameterDataForCleaning;

    @BeforeAll
    static void setUp() {
        partnerDirectoryClient = new PartnerDirectoryClient(new HttpClientsFactory());
    }

    @AfterEach
    void tearDown() {
        if (!Optional.ofNullable(this.parameterDataForCleaning).isPresent()) {
            return;
        }
        try {
            if (this.parameterDataForCleaning.getTypeOfParam().equals(TypeOfParam.BINARY_PARAMETER)) {
                partnerDirectoryClient.deleteBinaryParameter(
                    this.parameterDataForCleaning.getId(),
                    this.parameterDataForCleaning.getPid(),
                    this.parameterDataForCleaning.requestContext
                );
            } else {
                partnerDirectoryClient.deleteStringParameter(
                    this.parameterDataForCleaning.getId(),
                    this.parameterDataForCleaning.getPid(),
                    this.parameterDataForCleaning.requestContext
                );
            }
            log.debug("Cleaned up test data for parameter ID: {}", parameterDataForCleaning.getId());
        } catch (Exception e) {
            log.error("Failed to clean up test data for parameter ID: {}", parameterDataForCleaning.getId(), e);
        }
        this.parameterDataForCleaning = null;
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrieveBinaryParametersByFilter(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<PartnerDirectoryParameter> binaryParameters = partnerDirectoryClient.retrieveBinaryParametersMetadata(requestContext, new PartnerDirectoryParameterFilterRequest("SAPERP~Invoice01~ERP"));

        assertThat(binaryParameters).as("binaryParameters fetched by filter shouldn't be empty").isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrievePartners(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Partner> partners = partnerDirectoryClient.retrievePartners(requestContext);

        assertThat(partners).as("partners shouldn't be empty").isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrieveBinaryParameters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<PartnerDirectoryParameter> binaryParameters = partnerDirectoryClient.retrieveBinaryParametersMetadata(requestContext, new PartnerDirectoryParameterFilterRequest());

        assertThat(binaryParameters).as("binaryParameters shouldn't be empty").isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_retrieveStringParameters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<PartnerDirectoryParameter> stringParameters = partnerDirectoryClient.retrieveStringParameters(requestContext, new PartnerDirectoryParameterFilterRequest());

        assertThat(stringParameters).as("stringParameters shouldn't be empty").isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createBinaryParameter(AgentTestData agentTestData) throws IOException {
        File testFileForCreation = ResourceUtils.getFile("classpath:partner-directory/test-creation.json");
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        BinaryParameterCreationRequest binaryParameterCreationRequest = new BinaryParameterCreationRequest(
            "test_param_to_check_uniqueness",
            "21341235",
            FileUtils.readFileToByteArray(testFileForCreation),
            "json"
        );
        this.parameterDataForCleaning = new ParameterDataForCleaning(
            binaryParameterCreationRequest.getId(),
            binaryParameterCreationRequest.getPid(),
            TypeOfParam.BINARY_PARAMETER,
            requestContext
        );

        PartnerDirectoryParameter createdPartnerDirectoryParameter = partnerDirectoryClient.createBinaryParameter(binaryParameterCreationRequest, requestContext);

        assertTrue(Optional.ofNullable(createdPartnerDirectoryParameter).isPresent(), "binary parameter doesn't exist");

        assertEquals(binaryParameterCreationRequest.getId(), createdPartnerDirectoryParameter.getId(),
            "ID of the binary parameter does not match the expected ID");
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
        this.parameterDataForCleaning = new ParameterDataForCleaning(
            stringParameterCreationRequest.getId(),
            stringParameterCreationRequest.getPid(),
            TypeOfParam.STRING_PARAMETER,
            requestContext
        );

        PartnerDirectoryParameter createdPartnerDirectoryParameter = partnerDirectoryClient.createStringParameter(stringParameterCreationRequest, requestContext);

        assertTrue(Optional.ofNullable(createdPartnerDirectoryParameter).isPresent(), "string parameter doesn't exist");
        assertThat(createdPartnerDirectoryParameter.getId())
            .as("ID of the string parameter does not match the expected ID")
            .isEqualTo(stringParameterCreationRequest.getId());

    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateBinaryParameter(AgentTestData agentTestData) throws IOException {
        File testFileForCreation = ResourceUtils.getFile("classpath:partner-directory/test-creation.json");
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        BinaryParameterCreationRequest binaryParameterCreationRequest = new BinaryParameterCreationRequest(
            "test_param_to_check_uniqueness",
            "21341235",
            FileUtils.readFileToByteArray(testFileForCreation),
            "json"
        );

        this.parameterDataForCleaning = new ParameterDataForCleaning(
            binaryParameterCreationRequest.getId(),
            binaryParameterCreationRequest.getPid(),
            TypeOfParam.BINARY_PARAMETER,
            requestContext
        );

        PartnerDirectoryParameter createdPartnerDirectoryParameter = partnerDirectoryClient.createBinaryParameter(binaryParameterCreationRequest, requestContext);

        assertTrue(Optional.ofNullable(createdPartnerDirectoryParameter).isPresent(), "binary parameter doesn't exist");

        File testFileToUpdate = ResourceUtils.getFile("classpath:partner-directory/test-update.xml");
        BinaryParameterUpdateRequest binaryParameterUpdateRequest = new BinaryParameterUpdateRequest(
            FileUtils.readFileToByteArray(testFileToUpdate),
            "xml"
        );

        partnerDirectoryClient.updateBinaryParameter(
            createdPartnerDirectoryParameter.getId(),
            createdPartnerDirectoryParameter.getPid(),
            binaryParameterUpdateRequest,
            requestContext
        );

        Optional<PartnerDirectoryParameter> optionalPartnerDirectoryParameterAfterUpdate = partnerDirectoryClient.retrieveBinaryParameter(
            createdPartnerDirectoryParameter.getId(),
            createdPartnerDirectoryParameter.getPid(),
            requestContext
        );

        optionalPartnerDirectoryParameterAfterUpdate.ifPresent(partnerDirectoryParameterAfterUpdate -> {
            assertArrayEquals(
                Base64.getDecoder().decode(partnerDirectoryParameterAfterUpdate.getValue().getBytes(StandardCharsets.UTF_8)),
                binaryParameterUpdateRequest.getValue(),
                "binary parameter value wasn't updated"
            );
            assertEquals(
                partnerDirectoryParameterAfterUpdate.getContentType(),
                binaryParameterUpdateRequest.getContentType(),
                "binary parameter content type wasn't updated"
            );
        });

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
        this.parameterDataForCleaning = new ParameterDataForCleaning(
            stringParameterCreationRequest.getId(),
            stringParameterCreationRequest.getPid(),
            TypeOfParam.STRING_PARAMETER,
            requestContext
        );

        PartnerDirectoryParameter createdPartnerDirectoryParameter = partnerDirectoryClient.createStringParameter(stringParameterCreationRequest, requestContext);
        assertTrue(Optional.ofNullable(createdPartnerDirectoryParameter).isPresent(), "created string parameter doesn't exist");

        StringParameterUpdateRequest stringParameterUpdateRequest = new StringParameterUpdateRequest("testUpdate");
        partnerDirectoryClient.updateStringParameter(
            createdPartnerDirectoryParameter.getId(),
            createdPartnerDirectoryParameter.getPid(),
            stringParameterUpdateRequest,
            requestContext
        );

        Optional<PartnerDirectoryParameter> optionalPartnerDirectoryParameterAfterUpdate = partnerDirectoryClient.retrieveStringParameter(
            createdPartnerDirectoryParameter.getId(),
            createdPartnerDirectoryParameter.getPid(),
            requestContext
        );

        assertThat(optionalPartnerDirectoryParameterAfterUpdate).as("string parameter after update doesn't exist").isPresent();

        optionalPartnerDirectoryParameterAfterUpdate.ifPresent(partnerDirectoryParameterAfterUpdate -> assertThat(partnerDirectoryParameterAfterUpdate.getValue())
            .as("string parameter value wasn't updated")
            .isEqualTo(stringParameterUpdateRequest.getValue()));

    }

    @AllArgsConstructor
    @Setter
    @Getter
    public static class ParameterDataForCleaning {

        private String id;
        private String pid;
        private TypeOfParam typeOfParam;
        private RequestContext requestContext;
    }
}