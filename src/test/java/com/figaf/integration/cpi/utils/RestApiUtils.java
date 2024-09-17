package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.CpiRestAndSoapApiClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateRestOrSoapApiRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static com.figaf.integration.cpi.utils.PackageUtils.API_TEST_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Klochkov Sergey
 */
@AllArgsConstructor
@Slf4j
public class RestApiUtils {

    public static final String API_TEST_REST_API_NAME = "FigafApiTestRestApi";
    public static final String API_TEST_DUMMY_REST_API_NAME = "FigafApiTestDummyRestApi";

    private final PackageUtils packageUtils;
    private final CpiRestAndSoapApiClient cpiRestAndSoapApiClient;

    public CpiArtifact findTestRestApiInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findRestApiIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_REST_API_NAME
        );
    }

    public CpiArtifact findDummyRestApiInTestPackageIfExist(RequestContext requestContext) {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        return findRestApiIfExist(
            requestContext,
            API_TEST_PACKAGE_NAME,
            API_TEST_PACKAGE_NAME,
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_REST_API_NAME
        );
    }

    public CpiArtifact createDummyRestApiInTestPackage(RequestContext requestContext) throws IOException {
        IntegrationPackage integrationPackage = packageUtils.findTestPackageIfExist(requestContext);
        assertThat(integrationPackage).as("Package %s wasn't found", API_TEST_PACKAGE_NAME).isNotNull();
        byte[] payload = IOUtils.toByteArray(
            this.getClass().getClassLoader().getResource("client/FigafApiTestDummyRestApi.zip")
        );
        return createRestApi(
            requestContext,
            integrationPackage.getTechnicalName(),
            integrationPackage.getDisplayedName(),
            integrationPackage.getExternalId(),
            API_TEST_DUMMY_REST_API_NAME,
            payload
        );
    }

    public CpiArtifact getOrCreateDummyRestApi(RequestContext requestContext) throws IOException {
        CpiArtifact restApi = findDummyRestApiInTestPackageIfExist(requestContext);
        if (restApi == null) {
            restApi = createDummyRestApiInTestPackage(requestContext);
        }
        return restApi;
    }

    public void deleteRestApi(RequestContext requestContext, CpiArtifact restApi) {
        cpiRestAndSoapApiClient.deleteRestOrSoapApi(
            restApi.getPackageExternalId(),
            restApi.getExternalId(),
            restApi.getTechnicalName(),
            requestContext
        );
    }

    private CpiArtifact findRestApiIfExist(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String restApiName
    ) {
        List<CpiArtifact> artifacts = cpiRestAndSoapApiClient.getRestApiObjectsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId
        );

        return artifacts.stream().filter(cpiArtifact -> restApiName.equals(cpiArtifact.getTechnicalName()))
            .findFirst().orElse(null);
    }

    private CpiArtifact createRestApi(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        String restApiName,
        byte[] payload
    ) {
        CreateRestOrSoapApiRequest createOrUpdateRestApiRequest = CreateRestOrSoapApiRequest.builder()
            .id(restApiName)
            .name(restApiName)
            .description("Rest Api for api tests")
            .packageExternalId(packageExternalId)
            .packageTechnicalName(API_TEST_PACKAGE_NAME)
            .bundledModel(payload)
            .build();
        cpiRestAndSoapApiClient.createRestApi(requestContext, createOrUpdateRestApiRequest);
        return findRestApiIfExist(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            restApiName
        );
    }

}
