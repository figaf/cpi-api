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
            /*
            It's required to update restTemplateWrapperKey because requests to the main web api host and IS host don't have compatible session.
            In other words, when they are executed 1 by 1, for the second request it requires to process authentication flow fully
            e2e to get it successfully processed.
            But it won't work in that way because of the fix for "waiting" authentication attempts that skips authentication
            for all threads except the first. See comments in BaseClient.makeAuthRequestsWithLock.

            It's more efficient to keep a different session context for requests to IS host
             */
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
