package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.tags.CustomTagsConfiguration;
import com.figaf.integration.cpi.entity.tags.RetrieveCustomTagsResponse;
import com.figaf.integration.cpi.client.response.ResponseStatusHandler;
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

    private static final String API_CREATE_CUSTOM_TAGS = "/api/v1/CustomTagConfigurations?Overwrite=true";
    private static final String API_GET_CUSTOM_TAGS = "/api/v1/CustomTagConfigurations('CustomTags')/$value";
    private static final String GET_CUSTOM_TAGS_CONFIGURATION_OPERATION = "getCustomTagsConfiguration";
    private static final String IS_MANDATORY = "isMandatory";
    private static final String CUSTOM_TAGS_CONFIGURATION = "customTagsConfiguration";
    private static final String CUSTOM_TAGS_CONFIGURATION_CONTENT = "CustomTagsConfigurationContent";
    private static final String TAG_NAME = "tagName";
    private static final String CREATE_CUSTOM_TAGS_CONFIGURATION_OPERATION = "createCustomTagsConfiguration";
    private static final String CUSTOM_TAGS_CONFIGURATION_IS_EMPTY_ERROR_MESSAGE = "getCustomTagsConfiguration returned empty tags";

    public CustomTagsConfigurationClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CustomTagsConfiguration> getCustomTagsConfiguration(RequestContext requestContext) {
        log.debug("start getCustomTagsConfiguration");
        return executeMethodPublicApi(
                requestContext,
                API_GET_CUSTOM_TAGS,
                "",
                HttpMethod.GET,
                responseEntity -> {
                    ResponseStatusHandler.handleResponseStatus(responseEntity, GET_CUSTOM_TAGS_CONFIGURATION_OPERATION);
                    RetrieveCustomTagsResponse retrieveCustomTagsResponse = new Gson().fromJson(responseEntity.getBody(), RetrieveCustomTagsResponse.class);
                    if (Optional.ofNullable(retrieveCustomTagsResponse).isPresent() && Optional.ofNullable(retrieveCustomTagsResponse.getCustomTagsConfiguration()).isPresent()) {
                        return retrieveCustomTagsResponse.getCustomTagsConfiguration();
                    }
                    throw new ClientIntegrationException(CUSTOM_TAGS_CONFIGURATION_IS_EMPTY_ERROR_MESSAGE);
                }
        );
    }

    public void createCustomTagsConfiguration(RequestContext requestContext, List<CustomTagsConfiguration> customTagConfigurations) {
        log.debug("start createCustomTags");
        JSONArray customTags = new JSONArray();
        customTagConfigurations.forEach(customTagConfiguration -> {
            Map<String, Object> customTagsConfigurationAttributes = new HashMap<>();
            customTagsConfigurationAttributes.put(TAG_NAME, customTagConfiguration.getTagName());
            customTagsConfigurationAttributes.put(IS_MANDATORY, customTagConfiguration.isMandatory());
            customTags.put(customTagsConfigurationAttributes);
        });
        JSONObject customTagsConfiguration = new JSONObject().put(CUSTOM_TAGS_CONFIGURATION, customTags);
        JSONObject requestCustomTagsConfiguration = new JSONObject().put(CUSTOM_TAGS_CONFIGURATION_CONTENT, Base64.getEncoder().encodeToString(customTagsConfiguration.toString().getBytes(StandardCharsets.UTF_8)));

        executeMethodPublicApi(
                requestContext,
                API_CREATE_CUSTOM_TAGS,
                requestCustomTagsConfiguration.toString(),
                HttpMethod.POST,
                responseEntity -> {
                    ResponseStatusHandler.handleResponseStatus(responseEntity, CREATE_CUSTOM_TAGS_CONFIGURATION_OPERATION);
                    return responseEntity.getBody();
                }
        );
    }

}
