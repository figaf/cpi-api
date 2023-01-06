package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.tags.CustomTag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.figaf.integration.cpi.utils.CpiApiUtils.loadXMLFromString;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class IntegrationPackageCustomTagsClient extends BaseClient {

    private static final String API_CREATE_CUSTOM_TAGS_URL = "/api/v1/CustomTagConfigurations";

    private static final String API_UPDATE_CUSTOM_TAGS_URL = "/api/v1/IntegrationPackages('%s')/$links/CustomTags('%s')";

    private static final String API_GET_CUSTOM_TAGS_URL = "/api/v1/IntegrationPackages('%s')/CustomTags";

    public IntegrationPackageCustomTagsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public void createCustomTags(RequestContext requestContext, String nameOfTag) {
        JSONArray customTagsConfiguration = new JSONArray();
        Map<String, Object> customTagsConfigurationAttributes = new HashMap<>();
        customTagsConfigurationAttributes.put("tagName", nameOfTag);
        customTagsConfigurationAttributes.put("isMandatory", true);
        customTagsConfiguration.put(customTagsConfigurationAttributes);
        JSONObject requestBody = new JSONObject().put("CustomTagsConfigurationContent", Base64.getEncoder().encodeToString(customTagsConfiguration.toString().getBytes(StandardCharsets.UTF_8)));

        executeMethodPublicApi(
                requestContext,
                API_CREATE_CUSTOM_TAGS_URL,
                requestBody.toString(),
                HttpMethod.POST,
                responseEntity -> {
                    handleResponseOfCustomTagsOperation(responseEntity);
                    log.debug("Tag with name {} was created", nameOfTag);
                    return null;
                }
        );
    }

    public void updateCustomTags(
            RequestContext requestContext,
            String packageId,
            String nameOfTag,
            String delimiterSeparatedTagValues) {

        JSONObject requestUpdateTag = new JSONObject().put("Value", delimiterSeparatedTagValues);
        String updateCustomTagsUrl = String.format(API_UPDATE_CUSTOM_TAGS_URL, packageId, nameOfTag);

        executeMethodPublicApi(
                requestContext,
                updateCustomTagsUrl,
                requestUpdateTag.toString(),
                HttpMethod.PUT,
                responseEntity -> {
                    handleResponseOfCustomTagsOperation(responseEntity);
                    log.debug("Tag with name {} has been updated with new value(s) {}", nameOfTag, delimiterSeparatedTagValues);
                    return null;
                }
        );
    }

    public List<CustomTag> getCustomTags(RequestContext requestContext, String packageId) {
        String getCustomTagsUrl = String.format(API_GET_CUSTOM_TAGS_URL, packageId);
        return executeMethodPublicApi(
                requestContext,
                getCustomTagsUrl,
                "",
                HttpMethod.GET,
                responseEntity -> {
                    handleResponseOfCustomTagsOperation(responseEntity);
                    return parseCustomTagEntries(responseEntity.getBody());
                }
        );
    }

    private List<CustomTag> parseCustomTagEntries(String xml) {
        Document document = loadXMLFromString(xml);
        NodeList entries = document.getElementsByTagName("m:properties");
        return IntStream.range(0, entries.getLength()).mapToObj(index -> {
            String tagName = Optional.ofNullable(entries.item(index).getFirstChild()).isPresent() ? entries.item(index).getFirstChild().getTextContent() : "";
            String tagValue = Optional.ofNullable(entries.item(index).getLastChild()).isPresent() ? entries.item(index).getLastChild().getTextContent() : "";
            return CustomTag.builder().name(tagName).value(tagValue).build();
        }).collect(Collectors.toList());
    }

    private void handleResponseOfCustomTagsOperation(ResponseEntity<String> responseEntity) {
        switch (responseEntity.getStatusCode().value()) {
            case 200:
            case 201:
            case 202: {
                log.debug("Custom tags operation was successful {}", responseEntity.getBody());
                break;
            }
            default: {
                throw new ClientIntegrationException(String.format(
                        "Custom tags operation failed , Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        responseEntity.getBody())
                );
            }
        }
    }

}
