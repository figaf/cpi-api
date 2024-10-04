package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.figaf.integration.cpi.response_parser.MessageProcessingLogParser.*;
import static java.lang.String.format;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class MessageProcessingLogEdgeRuntimeClient extends AbstractMessageProcessingLogClient {

    private final String runtimeId;

    public MessageProcessingLogEdgeRuntimeClient(HttpClientsFactory httpClientsFactory, String runtimeId) {
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
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String queryParamsTemplate = expandCustomHeaders ? QUERY_PARAMS + "&$expand=CustomHeaderProperties" : QUERY_PARAMS;
        String queryParams = String.format(queryParamsTemplate, top, skip, filter);
        try {
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams);

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getAttachmentsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ATTACHMENTS, messageGuid);
            JSONArray attachmentsJsonArray = executeGet(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );

            return createMessageProcessingLogAttachmentsForAttachments(attachmentsJsonArray);
        } catch (JSONException ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid) {
        log.debug("#getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES, messageGuid);
            JSONArray attachmentsJsonArray = executeGet(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            return createMessageProcessingLogAttachmentsForPayloads(attachmentsJsonArray);
        } catch (JSONException ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid) {
        try {
            String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ID, messageGuid);
            JSONObject messageProcessingLogsObject = executeGet(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );
            return MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogsObject);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Message processing log is not found by {}: {}", messageGuid, ExceptionUtils.getMessage(ex));
            return null;
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while collecting message processing log : " + ex.getMessage(), ex);
        }
    }

    public String getErrorInformationValue(RequestContext requestContext, String messageGuid) {
        log.debug("#getErrorInformationValue(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {
            String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE, messageGuid);
            return executeGet(
                requestContext,
                resourcePath,
                response -> response,
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while collecting error information value:" + ex.getMessage(), ex);
        }
    }

    public List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid) {
        log.debug("#getCustomHeaderProperties(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_CUSTOM_HEADER, messageGuid);
        try {
            JSONObject messageProcessingLogsObject = executeGet(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );

            JSONArray jsonArray = messageProcessingLogsObject.getJSONArray("results");
            return createCustomHeaderProperties(jsonArray);

        } catch (JSONException ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getRunsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        String resourcePath = format(LOCATION + runtimeId + API_MSG_PROC_LOGS_RUNS, messageGuid);
        try {
            JSONArray runsJsonArray = executeGet(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            return createMessageProcessingLogAttachmentsForRuns(runsJsonArray, messageGuid);
        } catch (JSONException ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }


    public int getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter) {
        log.debug("#getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter): {}, {}", requestContext, filter);
        try {
            String resourcePath = StringUtils.isBlank(filter)
                ? format(LOCATION + runtimeId + API_MSG_PROC_LOGS_COUNT) :
                new URI(null, null, format(LOCATION + runtimeId + API_MSG_PROC_LOGS_COUNT), String.format(FILTER, filter), null).toString();
            return executeGet(
                requestContext,
                resourcePath,
                response -> NumberUtils.isCreatable(response) ? NumberUtils.toInt(response) : 0,
                String.class
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while getting count of Message Processing Logs: " + ex.getMessage(), ex);
        }
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter) {
        log.debug("getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter): {}, {}, {}, {}", requestContext, top, skip, filter);
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOG_CUSTOM_HEADER;
        String queryParams = String.format(QUERY_PARAMS_CUSTOM_HEADER, top, skip, filter);
        try {
            URI uri = new URI(null, null, resourcePath, queryParams, null);
            int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, null);
            return executeGet(
                requestContext, uri.toString(),
                (response) -> MessageProcessingLogParser.buildMessageProcessingLogsResult(response, totalCount)
            );
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while getting response from mpls by custom header: " + ex.getMessage(), ex);
        }
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilterWithSelectedResponseFields(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        String responseFields
    ) {
        log.debug(
            "getMessageProcessingLogsByFilterWithSelectedResponseFields(RequestContext requestContext, int top, int skip, String filter, String responseFields): {}, {}, {}, {}, {}",
            requestContext, top, skip, filter, responseFields
        );
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String queryParams = String.format(QUERY_PARAMS_WITH_SELECT, top, skip, filter, responseFields);
        try {
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams);
        } catch (Exception ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter) {
        log.debug("#getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter): {}, {}", requestContext, filter);
        return getMessageProcessingLogs(requestContext, top, filter);
    }

    private List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, int top, String filter) {
        try {
            String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
            String queryParams = String.format(QUERY_PARAMS_ORDERED, top, filter);
            URI uri = new URI(null, null, resourcePath, queryParams, null);
            JSONArray messageProcessingLogsJsonArray = executeGet(
                requestContext,
                uri.toString(),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            return createMessageProcessingLogsFromArray(messageProcessingLogsJsonArray);

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    private Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsToTotalCount(
        RequestContext requestContext,
        String resourcePath,
        String queryParams
    ) throws URISyntaxException {
        URI uri = new URI(null, null, resourcePath, queryParams, null);
        JSONObject jsonObjectD = executeGet(
            requestContext,
            uri.toString(),
            response -> new JSONObject(response).getJSONObject("d"),
            String.class
        );
        int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, null);
        return extractMplsAndCountFromResponse(jsonObjectD, totalCount);
    }

    private Pair<List<MessageProcessingLog>, Integer> extractMplsAndCountFromResponse(JSONObject jsonObjectD, int totalCount) {
        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();
        int sizeOfMessageProcessingLogs = messageProcessingLogsJsonArray.length();
        for (int ind = 0; ind < sizeOfMessageProcessingLogs; ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);

            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, totalCount);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
