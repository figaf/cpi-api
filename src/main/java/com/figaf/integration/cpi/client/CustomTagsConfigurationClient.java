package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.tags.CustomTagsConfiguration;
import com.figaf.integration.cpi.entity.tags.RetrieveCustomTagsResponse;
import com.figaf.integration.cpi.response_parser.ResponseStatusHandler;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class CustomTagsConfigurationClient extends BaseClient {

    private static final String API_CREATE_CUSTOM_TAGS_URL = "/api/v1/CustomTagConfigurations?Overwrite=true";

    private static final String API_GET_CUSTOM_TAGS_URL = "/api/v1/CustomTagConfigurations('CustomTags')/$value";


    public CustomTagsConfigurationClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CustomTagsConfiguration> getCustomTagsConfiguration(RequestContext requestContext) {
        log.debug("start getCustomTagsConfiguration");
        return executeMethodPublicApi(
                requestContext,
                API_GET_CUSTOM_TAGS_URL,
                "",
                HttpMethod.GET,
                responseEntity -> {
                    ResponseStatusHandler.handleResponseStatus(responseEntity, "getCustomTagsConfiguration");
                    RetrieveCustomTagsResponse retrieveCustomTagsResponse = new Gson().fromJson(responseEntity.getBody(), RetrieveCustomTagsResponse.class);
                    if (Optional.ofNullable(retrieveCustomTagsResponse).isPresent() && Optional.ofNullable(retrieveCustomTagsResponse.getCustomTagsConfiguration()).isPresent()) {
                        return retrieveCustomTagsResponse.getCustomTagsConfiguration();
                    }
                    throw new ClientIntegrationException("getCustomTagsConfiguration returned empty tags");
                }
        );
    }

    public void createCustomTagsConfiguration(RequestContext requestContext, List<CustomTagsConfiguration> customTagConfigurations) {
        log.debug("start createCustomTags");
        JSONArray customTags = new JSONArray();
        customTagConfigurations.forEach(customTagConfiguration -> {
            Map<String, Object> customTagsConfigurationAttributes = new HashMap<>();
            customTagsConfigurationAttributes.put("tagName", customTagConfiguration.getTagName());
            customTagsConfigurationAttributes.put("isMandatory", customTagConfiguration.isMandatory());
            customTags.put(customTagsConfigurationAttributes);
        });
        JSONObject customTagsConfiguration = new JSONObject().put("customTagsConfiguration", customTags);
        JSONObject requestCustomTagsConfiguration = new JSONObject().put("CustomTagsConfigurationContent", Base64.getEncoder().encodeToString(customTagsConfiguration.toString().getBytes(StandardCharsets.UTF_8)));

        executeMethodPublicApi(
                requestContext,
                API_CREATE_CUSTOM_TAGS_URL,
                requestCustomTagsConfiguration.toString(),
                HttpMethod.POST,
                responseEntity -> {
                    ResponseStatusHandler.handleResponseStatus(responseEntity, "createCustomTags");
                    return responseEntity.getBody();
                }
        );
    }

}
