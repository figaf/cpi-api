package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogRunStepParser;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.figaf.integration.cpi.response_parser.MessageProcessingLogParser.*;
import static java.lang.String.format;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class MessageProcessingLogEdgeRuntimeClient extends AbstractMessageProcessingLogClient {


    public MessageProcessingLogEdgeRuntimeClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
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
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String queryParamsTemplate = expandCustomHeaders
            ? PAGINATION_SORT_FILTER_TEMPLATE + "&$expand=CustomHeaderProperties"
            : PAGINATION_SORT_FILTER_TEMPLATE;
        String queryParams = String.format(queryParamsTemplate, top, skip, filter);
        try {
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams, filter);
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getAttachmentsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);
        try {
            String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_ATTACHMENTS), messageGuid);
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
            String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES), messageGuid);
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
        log.debug("#getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);
        try {
            String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_ID), messageGuid);
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
            String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE), messageGuid);
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
        String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_CUSTOM_HEADER), messageGuid);
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
        String resourcePath = format(createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_RUNS), messageGuid);
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
                ? createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_COUNT) :
                new URI(null, null, createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS_COUNT), String.format(FILTER, filter), null).toString();
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
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOG_CUSTOM_HEADER);
        String queryParams = String.format(PAGINATION_EXPAND_FILTER_TEMPLATE, top, skip, filter);
        try {
            URI uri = new URI(null, null, resourcePath, queryParams, null);
            int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, filter);
            return executeGet(
                requestContext, uri.toString(),
                (response) -> MessageProcessingLogParser.buildMessageProcessingLogsResult(
                    response,
                    totalCount,
                    requestContext.getConnectionProperties(),
                    requestContext.getRuntimeLocationId()
                )
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
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String queryParams = String.format(PAGINATION_SORT_FILTER_SELECT_TEMPLATE, top, skip, filter, responseFields);
        try {
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams, filter);
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter) {
        log.debug("#getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter): {}, {}", requestContext, filter);
        return getMessageProcessingLogs(requestContext, top, filter);
    }

    public byte[] getPersistedAttachment(RequestContext requestContext, String attachmentId) {
        log.debug("#getPersistedAttachment(RequestContext requestContext, String attachmentId): {}, {}", requestContext, attachmentId);
        return getAttachmentResponse(requestContext, attachmentId, API_MSG_STORE_ENTRIES_VALUE);
    }

    public byte[] getAttachment(RequestContext requestContext, String attachmentId) {
        log.debug("#getAttachment(RequestContext requestContext, String attachmentId): {}, {}", requestContext, attachmentId);
        return getAttachmentResponse(requestContext, attachmentId, API_MSG_PROC_LOG_ATTACHMENT);
    }

    private byte[] getAttachmentResponse(RequestContext requestContext, String attachmentId, String apiMsgStoreEntriesValue) {
        try {
            String attachmentResource = format(apiMsgStoreEntriesValue, attachmentId);
            String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), attachmentResource);
            String responseText = executeGet(
                requestContext,
                resourcePath,
                response -> response
            );

            if (StringUtils.isNotBlank(responseText)) {
                return responseText.getBytes(StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new ClientIntegrationException(ex);
        }
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate): {}, {}, {}", requestContext, integrationFlowName, startDate);
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String queryParams = format(QUERY_PARAMS_FOR_TRACES_WITH_IFLOW_NAME,
            integrationFlowName,
            GMT_DATE_FORMAT.format(startDate),
            GMT_DATE_FORMAT.format(shiftDateTo55MinutesBackFromNow())
        );
        return getMessageProcessingLogs(requestContext, resourcePath, queryParams);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate): {}, {}, {}", requestContext, technicalNames, startDate);
        String resourcePathWithParams = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String additionalQueryParams = format(QUERY_PARAMS_FOR_TRACES,
            technicalNamesFilter,
            GMT_DATE_FORMAT.format(startDate),
            GMT_DATE_FORMAT.format(shiftDateTo55MinutesBackFromNow())
        );
        return getMessageProcessingLogs(requestContext, resourcePathWithParams, additionalQueryParams);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames) {
        log.debug("#getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames): {}, {}, {}", requestContext, correlationIds, technicalNames);
        String resourcePathWithParams = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String correlationIdsFilter = buildCorrelationIdsFilter(correlationIds);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String queryParams = format(FILTER_TEMPLATE, format("(%s) and (%s)", correlationIdsFilter, technicalNamesFilter));
        return getMessageProcessingLogs(requestContext, resourcePathWithParams, queryParams);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByMessageGuids(
        RequestContext requestContext,
        Set<String> messageGuids,
        boolean expandCustomHeaders
    ) {
        log.debug(
            "#getMessageProcessingLogsByMessageGuids(RequestContext requestContext, Set<String> messageGuids, boolean expandCustomHeaders): {}, {}, {}",
            requestContext, messageGuids, expandCustomHeaders
        );

        List<String> params = new ArrayList<>();
        for (String messageGuid : messageGuids) {
            params.add(format("MessageGuid eq '%s'", messageGuid));
        }

        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String filter = format(FILTER_TEMPLATE, format("(%s)", StringUtils.join(params, " or ")));
        String queryParams = expandCustomHeaders ? filter + "&$expand=CustomHeaderProperties" : filter;

        return getMessageProcessingLogs(
            requestContext,
            resourcePath,
            queryParams
        );
    }

    public MessageProcessingLogRunStep.TraceMessage getTraceMessage(
        RequestContext requestContext,
        MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria,
        MessageProcessingLogRunStep runStep
    ) {
        log.debug("#getTraceMessage(RequestContext requestContext, MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria, MessageProcessingLogRunStep runStep): {}, {}, {}",
            requestContext, runStepSearchCriteria, runStep
        );
        String runId = runStep.getRunId();
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES);
        String fullPath = format(resourcePath, runId, runStep.getChildCount());
        JSONArray runsJsonArray = executeGet(
            requestContext,
            fullPath,
            response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
            String.class
        );

        if (runsJsonArray.isEmpty()) {
            return null;
        }

        /*If traceMessagesJsonArray has multiple messages, we need to find the most appropriate one.
        Its SAP_TRACE_HEADER_<some digits>_MessageType property should be equal to "STEP". If it's not found, we just take the first one.
        Maybe later we will need to download and handle all the messages.*/
        TraceMessageHolder traceMessageHolder = new TraceMessageHolder();
        if (runsJsonArray.length() > 1) {
            for (int i = 0; i < runsJsonArray.length() && traceMessageHolder.getTraceMessageElement() == null; i++) {
                JSONObject currentTraceMessage = runsJsonArray.getJSONObject(i);
                String resourcePathProperties = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_TRACE_MESSAGE_PROPERTIES);
                String fullPathProperties = format(resourcePathProperties, optString(currentTraceMessage, "TraceId"));
                JSONArray traceMessagePropertiesJsonArray = executeGet(
                    requestContext,
                    fullPathProperties,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    String.class
                );

                setTraceMessageHolder(traceMessageHolder, traceMessagePropertiesJsonArray, currentTraceMessage);
            }
        }
        JSONObject traceMessageElement = Optional.ofNullable(traceMessageHolder.getTraceMessageElement())
            .orElse(getDefaultValueOfTraceMessageElement(runsJsonArray));
        MessageProcessingLogRunStep.TraceMessage traceMessage = createTraceMessage(
            traceMessageElement,
            runStep,
            runStepSearchCriteria,
            requestContext
        );

        if (runStepSearchCriteria.isInitTraceMessageProperties()) {
            JSONArray traceMessagePropertiesJsonArray;
            /*We could already have traceMessagePropertiesJsonArray if there are multiple trace messages.
             In this case we don't need to make the same request twice.*/
            if (traceMessageHolder.getFoundTraceMessagePropertiesJsonArray() != null) {
                traceMessagePropertiesJsonArray = traceMessageHolder.getFoundTraceMessagePropertiesJsonArray();
            } else {
                String resourcePathProperties = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_TRACE_MESSAGE_PROPERTIES);
                String fullPathProperties = format(resourcePathProperties, traceMessage.getTraceId());
                traceMessagePropertiesJsonArray = executeGet(
                    requestContext,
                    fullPathProperties,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    String.class
                );
            }

            setTraceMessageHeader(traceMessage, traceMessagePropertiesJsonArray);
            String resourcePathProperties = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_TRACE_MESSAGE_EXCHANGE_PROPERTIES);
            String fullPathProperties = format(resourcePathProperties, traceMessage.getTraceId());
            JSONArray traceMessageExchangePropertiesJsonArray = executeGet(
                requestContext,
                fullPathProperties,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );
            setTraceMessageExchange(traceMessage, traceMessageExchangePropertiesJsonArray);
        }
        return traceMessage;
    }

    public List<MessageProcessingLogRunStep> getRunSteps(RequestContext requestContext, String runId) {
        log.debug("#getRunSteps(RequestContext requestContext, String runId): {}, {}", requestContext, runId);
        try {
            List<JSONObject> jsonObjectRunSteps = getRunStepJsonObjects(requestContext, runId);
            return MessageProcessingLogRunStepParser.createMessageProcessingLogRunSteps(jsonObjectRunSteps);
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public byte[] getPayloadForMessage(RequestContext requestContext, String traceId) {
        log.debug("#getPayloadForMessage(RequestContext requestContext, String traceId): {}, {}", requestContext, traceId);
        try {
            String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_TRACE_MESSAGE_PAYLOAD);
            String fullPathWithParams = format(resourcePath, traceId);
            String payloadResponse = executeGet(
                requestContext,
                fullPathWithParams,
                response -> response,
                String.class
            );
            if (StringUtils.isNotBlank(payloadResponse)) {
                return payloadResponse.getBytes(StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds) {
        log.debug("#getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds): {}, {}", requestContext, correlationIds);

        List<String> params = new ArrayList<>();
        for (String correlationId : correlationIds) {
            params.add(format("CorrelationId eq '%s'", correlationId));
        }
        String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
        String queryParams = format(SORT_FILTER_TEMPLATE, StringUtils.join(params, " or "));
        return getMessageProcessingLogs(
            requestContext,
            resourcePath,
            queryParams
        );
    }

    private List<JSONObject> getRunStepJsonObjects(RequestContext requestContext, String runId) {
        int iterNumber = 0;
        Integer numberOfIterations = null;

        List<JSONObject> jsonObjectRunSteps = new ArrayList<>();
        do {
            int skip = MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION * iterNumber;
            String resourcePathProperties = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOG_RUN_STEPS);
            String fullPathProperties = format(resourcePathProperties, runId, MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION, skip);
            JSONObject dObject = executeGet(
                requestContext,
                fullPathProperties,
                response -> new JSONObject(response).getJSONObject("d"),
                String.class
            );
            if (numberOfIterations == null) {
                numberOfIterations = defineNumberOfIterations(dObject);
            }

            JSONArray runStepsJsonArray = dObject.getJSONArray("results");
            for (int i = 0; i < runStepsJsonArray.length(); i++) {
                jsonObjectRunSteps.add(runStepsJsonArray.getJSONObject(i));
            }
            iterNumber++;
        } while (iterNumber < numberOfIterations);
        return jsonObjectRunSteps;
    }

    private List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, int top, String filter) {
        try {
            String resourcePath = createBaseResourcePath(requestContext.getRuntimeLocationId(), API_MSG_PROC_LOGS);
            String queryParams = String.format(SORT_FILTER_LIMIT_TEMPLATE, top, filter);
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


    public List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String resourcePath, String queryParams) {
        try {
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
        String queryParams,
        String filterForCount
    ) throws URISyntaxException {
        URI uri = new URI(null, null, resourcePath, queryParams, null);
        JSONObject jsonObjectD = executeGet(
            requestContext,
            uri.toString(),
            response -> new JSONObject(response).getJSONObject("d"),
            String.class
        );
        int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, filterForCount);
        return extractMplsAndCountFromResponse(jsonObjectD, totalCount, requestContext);
    }

    private Pair<List<MessageProcessingLog>, Integer> extractMplsAndCountFromResponse(
        JSONObject jsonObjectD,
        int totalCount,
        RequestContext requestContext
    ) {
        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();
        int sizeOfMessageProcessingLogs = messageProcessingLogsJsonArray.length();
        for (int ind = 0; ind < sizeOfMessageProcessingLogs; ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);

            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(
                messageProcessingLogElement,
                requestContext.getConnectionProperties(),
                requestContext.getRuntimeLocationId()
            );
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, totalCount);
    }

    private String createBaseResourcePath(String runtimeLocationId, String path) {
        return String.format("%s%s%s", LOCATION, runtimeLocationId, path);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
