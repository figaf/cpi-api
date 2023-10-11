package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.configuration.CpiConfigurations;
import com.figaf.integration.cpi.response_parser.ConfigurationsParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.figaf.integration.cpi.utils.CpiApiConstants.IS_URL_KEY_SUFFIX;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class ConfigurationsClient extends CpiBaseClient {

    private static final String API_CONFIGURATIONS = "/api/1.0/configurations";

    public ConfigurationsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public CpiConfigurations getConfigurations(RequestContext requestContext) {
        log.debug("#getConfigurations(RequestContext requestContext): {}", requestContext);
        // to avoid side effect
        requestContext = requestContext.clone();

        String path = "/itspaces" + API_CONFIGURATIONS;
        ResponseHandlerCallback<CpiConfigurations, String> extractParamsFunction = ConfigurationsParser::buildCpiNonIsConfigurations;

        if (requestContext.isIntegrationSuite()) {
            path = API_CONFIGURATIONS;
            requestContext.setRestTemplateWrapperKey(requestContext.getRestTemplateWrapperKey() + IS_URL_KEY_SUFFIX);
            requestContext.getConnectionProperties().setHost(requestContext.getIntegrationSuiteUrl());
            extractParamsFunction = ConfigurationsParser::buildCpiIsConfigurations;
        }

        return executeGet(requestContext, path, extractParamsFunction);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
