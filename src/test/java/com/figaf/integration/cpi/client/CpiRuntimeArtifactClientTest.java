package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public abstract class CpiRuntimeArtifactClientTest {

    protected static final String API_TEST_PACKAGE_NAME = "FigafApiTestPackage";

    protected static IntegrationPackageClient integrationPackageClient;

    protected IntegrationPackage findPackageByNameIfExist(RequestContext requestContext, String packageName) {
        List<IntegrationPackage> integrationPackages = integrationPackageClient.getIntegrationPackages(
            requestContext,
            format("TechnicalName eq '%s'", packageName)
        );
        if (isNotEmpty(integrationPackages)) {
            return integrationPackages.get(0);
        }
        return null;
    }

}
