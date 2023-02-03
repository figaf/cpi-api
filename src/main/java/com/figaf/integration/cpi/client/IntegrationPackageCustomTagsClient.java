package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.tags.CustomTag;
import com.figaf.integration.cpi.client.response.ResponseStatusHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.figaf.integration.cpi.utils.CpiApiUtils.loadXMLFromString;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class IntegrationPackageCustomTagsClient extends BaseClient {

    private static final String API_UPDATE_CUSTOM_TAGS_IN_PACKAGE = "/api/v1/IntegrationPackages('%s')/$links/CustomTags('%s')";
    private static final String API_GET_CUSTOM_TAGS_OF_PACKAGE = "/api/v1/IntegrationPackages('%s')/CustomTags";
    private static final String VALUE = "Value";
    private static final String UPDATE_CUSTOM_TAGS_IN_INTEGRATION_PACKAGE_OPERATION = "updateCustomTagsInIntegrationPackage";
    private static final String GET_CUSTOM_TAGS_OF_INTEGRATION_PACKAGE_OPERATION = "getCustomTagsOfIntegrationPackage";
    private static final String PROPERTIES = "m:properties";
    private static final String UPDATE_CUSTOM_TAGS_IN_INTEGRATION_PACKAGE_MESSAGE = "Tag with name {} has been updated with new value(s) {}";

    public IntegrationPackageCustomTagsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public void updateCustomTags(
        RequestContext requestContext,
        String packageId,
        String nameOfTag,
        String delimiterSeparatedTagValues) {
        log.debug("start updateCustomTags");
        JSONObject requestUpdateTag = new JSONObject().put(VALUE, delimiterSeparatedTagValues);
        String updateCustomTagsUrl = String.format(API_UPDATE_CUSTOM_TAGS_IN_PACKAGE, packageId, nameOfTag);

        executeMethodPublicApi(
            requestContext,
            updateCustomTagsUrl,
            requestUpdateTag.toString(),
            HttpMethod.PUT,
            responseEntity -> {
                ResponseStatusHandler.handleResponseStatus(responseEntity, UPDATE_CUSTOM_TAGS_IN_INTEGRATION_PACKAGE_OPERATION);
                log.debug(UPDATE_CUSTOM_TAGS_IN_INTEGRATION_PACKAGE_MESSAGE, nameOfTag, delimiterSeparatedTagValues);
                return null;
            }
        );
    }

    public List<CustomTag> getCustomTags(RequestContext requestContext, String packageId) {
        log.debug("start getCustomTags");
        String getCustomTagsUrl = String.format(API_GET_CUSTOM_TAGS_OF_PACKAGE, packageId);
        return executeMethodPublicApi(
            requestContext,
            getCustomTagsUrl,
            "",
            HttpMethod.GET,
            responseEntity -> {
                ResponseStatusHandler.handleResponseStatus(responseEntity, GET_CUSTOM_TAGS_OF_INTEGRATION_PACKAGE_OPERATION);
                return parseCustomTagsFromResponse(responseEntity.getBody());
            }
        );
    }

    private List<CustomTag> parseCustomTagsFromResponse(String xml) {
        Document document = loadXMLFromString(xml);
        NodeList entries = document.getElementsByTagName(PROPERTIES);
        return IntStream.range(0, entries.getLength()).mapToObj(index -> {
            String tagName = Optional.ofNullable(entries.item(index).getFirstChild()).isPresent() ? entries.item(index).getFirstChild().getTextContent() : "";
            String tagValue = Optional.ofNullable(entries.item(index).getLastChild()).isPresent() ? entries.item(index).getLastChild().getTextContent() : "";
            return CustomTag.builder().name(tagName).value(tagValue).build();
        }).collect(Collectors.toList());
    }

}
