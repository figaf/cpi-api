package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdatePackageRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.runtime_artifacts.CpiExternalConfiguration;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
@Slf4j
class CommonCpiApiTest {

    private static final String API_DELETE_TEST_PACKAGE_NAME = "FigafApiDeleteTestPackage";

    private static IntegrationContentClient integrationContentClient;
    private static CpiIntegrationFlowExternalConfigurationsClient cpiIntegrationFlowExternalConfigurationsClient;
    private static IntegrationPackageClient integrationPackageClient;

    @BeforeAll
    static void setUp() {
        integrationContentClient = new IntegrationContentClient(new HttpClientsFactory());
        cpiIntegrationFlowExternalConfigurationsClient = new CpiIntegrationFlowExternalConfigurationsClient(new HttpClientsFactory());
        integrationPackageClient = new IntegrationPackageClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_privateApiRead(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(requestContext, null);
        log.debug("{} integrationPackages were found", integrationPackages.size());
        assertThat(integrationPackages).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_publicApiRead(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<IntegrationContent> integrationRuntimeArtifacts = integrationContentClient.getAllIntegrationRuntimeArtifacts(requestContext);
        log.debug("{} integrationRuntimeArtifacts were found", integrationRuntimeArtifacts.size());
        assertThat(integrationRuntimeArtifacts).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPublicApiRead2(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<CpiExternalConfiguration> cpiExternalConfigurations = cpiIntegrationFlowExternalConfigurationsClient.getCpiExternalConfigurations(requestContext, "Figaf_iflow_1");
        log.debug("{} cpiExternalConfigurations were found", cpiExternalConfigurations.size());
        assertThat(cpiExternalConfigurations).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPrivateApiWrite(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(requestContext, format("TechnicalName eq '%s'", API_TEST_PACKAGE_NAME));
        assertThat(integrationPackages).isNotEmpty();
        IntegrationPackage integrationPackage = integrationPackages.get(0);

        CreateOrUpdatePackageRequest createOrUpdatePackageRequest = new CreateOrUpdatePackageRequest();
        createOrUpdatePackageRequest.setTechnicalName(integrationPackage.getTechnicalName());
        createOrUpdatePackageRequest.setDisplayName(integrationPackage.getDisplayedName());
        createOrUpdatePackageRequest.setShortDescription("Updated at " + new Date());
        createOrUpdatePackageRequest.setVendor(integrationPackage.getVendor());
        createOrUpdatePackageRequest.setVersion(integrationPackage.getVersion());

        integrationPackageClient.updateIntegrationPackage(requestContext, integrationPackage.getExternalId(), createOrUpdatePackageRequest);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_testPublicApiWrite(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        int numberOfUploadedConfigurations = cpiIntegrationFlowExternalConfigurationsClient.uploadCpiExternalConfiguration(
            requestContext,
            "Figaf_iflow_1",
            Collections.singletonList(
                new CpiExternalConfiguration("endpoint", "/figaf_iflow_3", "xsd:string")
            )
        );
        assertThat(numberOfUploadedConfigurations).isEqualTo(1);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_privatePackageApiDelete(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        IntegrationPackage integrationPackage = getOrCreatePackage(requestContext, API_DELETE_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't found", API_DELETE_TEST_PACKAGE_NAME).isNotNull();
        integrationPackageClient.deletePackage(API_DELETE_TEST_PACKAGE_NAME, requestContext);
        integrationPackage = findPackageByNameIfExist(requestContext, API_DELETE_TEST_PACKAGE_NAME);
        assertThat(integrationPackage).as("Package %s wasn't deleted", API_DELETE_TEST_PACKAGE_NAME).isNull();
    }

    private IntegrationPackage findPackageByNameIfExist(RequestContext requestContext, String packageName) {
        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(
            requestContext,
            format("TechnicalName eq '%s'", packageName)
        );
        if (isNotEmpty(integrationPackages)) {
            return integrationPackages.get(0);
        }
        return null;
    }

    private IntegrationPackage createDummyPackage(RequestContext requestContext, String packageName) {
        CreateOrUpdatePackageRequest createOrUpdatePackageRequest = new CreateOrUpdatePackageRequest();
        createOrUpdatePackageRequest.setTechnicalName(packageName);
        createOrUpdatePackageRequest.setDisplayName(packageName);
        createOrUpdatePackageRequest.setShortDescription("Package for cpi api tests");
        integrationPackageClient.createIntegrationPackage(requestContext, createOrUpdatePackageRequest);
        return findPackageByNameIfExist(requestContext, packageName);
    }

    private IntegrationPackage getOrCreatePackage(RequestContext requestContext, String packageName) {
        IntegrationPackage integrationPackage = findPackageByNameIfExist(requestContext, packageName);
        if (integrationPackage == null) {
            integrationPackage = createDummyPackage(requestContext, packageName);
        }
        return integrationPackage;
    }

}