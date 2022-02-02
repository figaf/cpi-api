package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiRestApiClientTest extends CpiRuntimeArtifactClientTest {

    private static final String API_TEST_REST_API_NAME = "FigafApiTestRestApi";
    private static final String API_TEST_DUMMY_REST_API_NAME = "FigafApiTestDummyRestApi";

    private static CpiRestApiClient cpiRestApiClient;

    @BeforeAll
    static void setUp() {
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiRestApiClient = new CpiRestApiClient(integrationPackageClient, new HttpClientsFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRestApiByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> restApis = cpiRestApiClient.getRestApiObjectsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId()
        );
        assertThat(restApis).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadRestApi(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact restApi = findRestApiIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_REST_API_NAME
        );
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_REST_API_NAME).isNotNull();

        byte[] restApiPayload = cpiRestApiClient.downloadRestApi(
            requestContext,
            integrationPackage.getExternalId(),
            restApi.getExternalId()
        );
        assertThat(restApiPayload).isNotEmpty();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteRestApi(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        CpiArtifact restApi = findRestApiIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_REST_API_NAME
        );
        assertThat(restApi).as("rest api %s already exist", API_TEST_DUMMY_REST_API_NAME).isNull();

        restApi = createDummyRestApi(requestContext, integrationPackage.getExternalId());
        assertThat(restApi).as("rest api %s was not created", API_TEST_DUMMY_REST_API_NAME).isNotNull();

        cpiRestApiClient.deleteRestApi(
            integrationPackage.getExternalId(),
            restApi.getExternalId(),
            API_TEST_DUMMY_REST_API_NAME,
            requestContext
        );

        restApi = findRestApiIfExist(
            requestContext,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_REST_API_NAME
        );
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateRestApi(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact restApi = getOrCreateDummyRestApi(requestContext, packageExternalId);
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_DUMMY_REST_API_NAME).isNotNull();

        String restApiExternalId = restApi.getExternalId();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyRestApiUpdated.zip")
        );
        CreateOrUpdateRestApiRequest createOrUpdateRestApiRequest = CreateOrUpdateRestApiRequest.builder()
            .id(restApiExternalId)
            .name(API_TEST_DUMMY_REST_API_NAME)
            .description("Rest Api for api tests")
            .build();
        cpiRestApiClient.updateRestApi(
            requestContext,
            packageExternalId,
            restApiExternalId,
            createOrUpdateRestApiRequest,
            payload
        );

        cpiRestApiClient.deleteRestApi(packageExternalId, restApiExternalId, API_TEST_DUMMY_REST_API_NAME, requestContext);
        restApi = findRestApiIfExist(requestContext, packageExternalId, API_TEST_DUMMY_REST_API_NAME);
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployRestApiAndCheckDeployStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        String packageExternalId = integrationPackage.getExternalId();
        CpiArtifact restApi = getOrCreateDummyRestApi(requestContext, packageExternalId);
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_DUMMY_REST_API_NAME).isNotNull();

        String restApiExternalId = restApi.getExternalId();
        String taskId = cpiRestApiClient.deployRestApi(
            requestContext,
            packageExternalId,
            restApiExternalId,
            API_TEST_DUMMY_REST_API_NAME
        );
        assertThat(taskId).isNotBlank();

        String status = cpiRestApiClient.checkDeployStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        cpiRestApiClient.deleteRestApi(packageExternalId, restApiExternalId, API_TEST_DUMMY_REST_API_NAME, requestContext);
        restApi = findRestApiIfExist(requestContext, packageExternalId, API_TEST_DUMMY_REST_API_NAME);
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

    private CpiArtifact findRestApiIfExist(
        RequestContext requestContext,
        String packageExternalId,
        String restApiName
    ) {
        List<CpiArtifact> artifacts = cpiRestApiClient.getRestApiObjectsByPackage(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> restApiName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createDummyRestApi(RequestContext requestContext, String packageExternalId) throws IOException {
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyRestApi.zip")
        );
        CreateOrUpdateRestApiRequest createOrUpdateRestApiRequest = CreateOrUpdateRestApiRequest.builder()
            .id(API_TEST_DUMMY_REST_API_NAME)
            .name(API_TEST_DUMMY_REST_API_NAME)
            .description("Rest Api for api tests")
            .build();
        cpiRestApiClient.createRestApi(
            requestContext,
            packageExternalId,
            createOrUpdateRestApiRequest,
            payload
        );
        return findRestApiIfExist(requestContext, packageExternalId, API_TEST_DUMMY_REST_API_NAME);
    }

    private CpiArtifact getOrCreateDummyRestApi(RequestContext requestContext, String packageExternalId) throws IOException {
        CpiArtifact restApi = findRestApiIfExist(requestContext, packageExternalId, API_TEST_DUMMY_REST_API_NAME);
        if (restApi == null) {
            restApi = createDummyRestApi(requestContext, packageExternalId);
        }
        return restApi;
    }

}