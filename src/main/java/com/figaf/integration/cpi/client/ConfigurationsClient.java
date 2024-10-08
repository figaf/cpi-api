package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.configuration.CpiConfigurations;
import com.figaf.integration.cpi.response_parser.ConfigurationsParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

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

        String path;
        if (isIntegrationSuiteHost(requestContext.getConnectionProperties().getHost())) {
            path = API_CONFIGURATIONS;
        } else {
            path = "/itspaces" + API_CONFIGURATIONS;
        }

        return executeGet(requestContext, path, ConfigurationsParser::parseConfigurationsFromJsonString);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
