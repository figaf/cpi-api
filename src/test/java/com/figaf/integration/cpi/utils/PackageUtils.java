package com.figaf.integration.cpi.utils;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.cpi.client.IntegrationPackageClient;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author Klochkov Sergey
 */
@AllArgsConstructor
@Slf4j
public class PackageUtils {

    public static final String API_TEST_PACKAGE_NAME = "FigafApiTestPackage";

    private final IntegrationPackageClient integrationPackageClient;

    public IntegrationPackage findTestPackageIfExist(RequestContext requestContext) {
        return findPackageByNameIfExist(requestContext, API_TEST_PACKAGE_NAME);
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

}
