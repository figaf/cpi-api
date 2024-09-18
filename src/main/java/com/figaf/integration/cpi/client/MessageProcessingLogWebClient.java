package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.*;

import static java.lang.String.format;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class MessageProcessingLogWebClient extends AbstractMessageProcessingLogClient {

    private final static String LOCATION = "/location/";

    private static final String API_MSG_PROC_LOGS = "/api/v1/MessageProcessingLogs";

    private static final String QUERY_PARAMS = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s";

    private static final String API_MSG_PROC_LOGS_CUSTOM_HEADER = "/api/v1/MessageProcessingLogs('%s')/CustomHeaderProperties?$format=json";

    private final String runtimeId;

    public MessageProcessingLogWebClient(HttpClientsFactory httpClientsFactory, String runtimeId) {
        super(httpClientsFactory);
        this.runtimeId = runtimeId;
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        boolean expandCustomHeaders
    ) {
        log.debug(
            "getMessageProcessingLogsByFilter(RequestContext requestContext, int top, int skip, String filter, boolean expandCustomHeaders): {}, {}, {}, {}, {}",
            requestContext, top, skip, filter, expandCustomHeaders
        );
        String resourcePathBuilder = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String queryParamsTemplate = expandCustomHeaders ? QUERY_PARAMS + "&$expand=CustomHeaderProperties" : QUERY_PARAMS;
        String queryParams = String.format(queryParamsTemplate, top, skip, filter);
        try {
            URI uri = new URI(null, null, resourcePathBuilder, queryParams, null);
            JSONObject jsonObjectD = executeGet(
                requestContext,
                uri.toString(),
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );

            return extractMplsAndCountFromResponse(jsonObjectD);

        } catch (Exception ex) {
            log.error("Error occurred while parsing response: {}", ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getAttachmentsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ATTACHMENTS, messageGuid);
            JSONArray attachmentsJsonArray = executeGet(
                requestContext,
                resourcePathBuilder,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );

            return createMessageProcessingLogAttachmentsForAttachments(attachmentsJsonArray);
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid) {
        log.debug("#getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES, messageGuid);
            JSONArray attachmentsJsonArray = executeGet(
                requestContext,
                resourcePathBuilder,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            return createMessageProcessingLogAttachmentsForPayloads(attachmentsJsonArray);
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid) {
        try {
            String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ID, messageGuid);
            JSONObject messageProcessingLogsObject = executeGet(
                requestContext,
                resourcePathBuilder,
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );
            return MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogsObject);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Message processing log is not found by {}: {}", messageGuid, ExceptionUtils.getMessage(ex));
            return null;
        } catch (Exception ex) {
            log.error("Error occurred while collecting message processing log: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting message processing log : " + ex.getMessage(), ex);
        }
    }

    public String getErrorInformationValue(RequestContext requestContext, String messageGuid) {
        log.debug("#getErrorInformationValue(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE, messageGuid);
            return executeGet(
                requestContext,
                resourcePathBuilder,
                response -> response,
                String.class
            );
        } catch (Exception ex) {
            log.error("Error occurred while collecting error information value: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting error information value:" + ex.getMessage(), ex);
        }
    }

    public List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid) {
        log.debug("#getCustomHeaderProperties(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_CUSTOM_HEADER, messageGuid);
        try {
            JSONObject messageProcessingLogsObject = executeGet(
                requestContext,
                resourcePathBuilder,
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );

            JSONArray jsonArray = messageProcessingLogsObject.getJSONArray("results");
            return createCustomHeaderProperties(jsonArray);

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getRunsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        String resourcePathBuilder = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_RUNS, messageGuid);
        try {
            JSONArray runsJsonArray = executeGet(
                requestContext,
                resourcePathBuilder,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            return createMessageProcessingLogAttachmentsForRuns(runsJsonArray, messageGuid);
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
