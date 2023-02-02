package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.*;
import com.figaf.integration.cpi.utils.PackageUtils;
import com.figaf.integration.cpi.utils.RestApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static com.figaf.integration.cpi.utils.RestApiUtils.API_TEST_DUMMY_REST_API_NAME;
import static com.figaf.integration.cpi.utils.RestApiUtils.API_TEST_REST_API_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@Slf4j
class CpiRestApiClientTest {

    private static CpiRestApiClient cpiRestApiClient;
    private static PackageUtils packageUtils;
    private static RestApiUtils restApiUtils;

    @BeforeAll
    static void setUp() {
        IntegrationPackageClient integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
        cpiRestApiClient = new CpiRestApiClient(new HttpClientsFactory());
        packageUtils = new PackageUtils(integrationPackageClient);
        restApiUtils = new RestApiUtils(packageUtils, cpiRestApiClient);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRestApiByPackage(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();

        List<CpiArtifact> restApis = cpiRestApiClient.getRestApiObjectsByPackage(
                requestContext,
                API_TEST_PACKAGE_NAME,
                API_TEST_PACKAGE_NAME,
                integrationPackage.getExternalId()
        );
        assertThat(restApis).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_downloadRestApi(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact restApi = restApiUtils.findTestRestApiInTestPackageIfExist(requestContext);
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_REST_API_NAME).isNotNull();

        byte[] restApiPayload = cpiRestApiClient.downloadRestApi(
                requestContext,
                restApi.getPackageExternalId(),
                restApi.getExternalId()
        );
        assertThat(restApiPayload).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteRestApi(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact restApi = restApiUtils.findDummyRestApiInTestPackageIfExist(requestContext);
        assertThat(restApi).as("rest api %s already exist", API_TEST_DUMMY_REST_API_NAME).isNull();

        restApi = restApiUtils.createDummyRestApiInTestPackage(requestContext);
        assertThat(restApi).as("rest api %s was not created", API_TEST_DUMMY_REST_API_NAME).isNotNull();
        restApiUtils.deleteRestApi(requestContext, restApi);
        restApi = restApiUtils.findDummyRestApiInTestPackageIfExist(requestContext);
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_updateRestApi(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact restApi = restApiUtils.getOrCreateDummyRestApi(requestContext);
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_DUMMY_REST_API_NAME).isNotNull();

        String restApiExternalId = restApi.getExternalId();
        byte[] payload = IOUtils.toByteArray(
                this.getClass().getClassLoader().getResource("client/FigafApiTestDummyRestApiUpdated.zip")
        );
        UpdateRestApiRequest updateRestApiRequest = UpdateRestApiRequest.builder()
                .id(restApiExternalId)
                .name(API_TEST_DUMMY_REST_API_NAME)
                .description("Rest Api for api tests")
                .packageExternalId(restApi.getPackageExternalId())
                .bundledModel(payload)
                .build();
        cpiRestApiClient.updateRestApi(requestContext, updateRestApiRequest);

        restApiUtils.deleteRestApi(requestContext, restApi);
        restApi = restApiUtils.findDummyRestApiInTestPackageIfExist(requestContext);
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_deployRestApiAndCheckDeploymentStatus(AgentTestData agentTestData) throws Exception {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        CpiArtifact restApi = restApiUtils.getOrCreateDummyRestApi(requestContext);
        assertThat(restApi).as("rest api %s wasn't found", API_TEST_DUMMY_REST_API_NAME).isNotNull();

        String restApiExternalId = restApi.getExternalId();
        String taskId = cpiRestApiClient.deployRestApi(
                requestContext,
                restApi.getPackageExternalId(),
                restApiExternalId,
                API_TEST_DUMMY_REST_API_NAME
        );
        assertThat(taskId).isNotBlank();

        String status = cpiRestApiClient.checkDeploymentStatus(requestContext, taskId);
        assertThat(status).isNotBlank();

        restApiUtils.deleteRestApi(requestContext, restApi);
        restApi = restApiUtils.findDummyRestApiInTestPackageIfExist(requestContext);
        assertThat(restApi).as("rest api %s was not deleted", API_TEST_DUMMY_REST_API_NAME).isNull();
    }

}