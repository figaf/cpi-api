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
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.figaf.integration.cpi.response_parser.MessageProcessingLogParser.*;
import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class MessageProcessingLogClient extends AbstractMessageProcessingLogClient {


    private final static FastDateFormat GMT_DATE_FORMAT = FastDateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        TimeZone.getTimeZone("GMT")
    );

    public MessageProcessingLogClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter) {
        log.debug("getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter): {}, {}, {}, {}", requestContext, top, skip, filter);
        String path = format("/itspaces/odata/api/v1/MessageProcessingLogCustomHeaderProperties?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$expand=Log&$filter=%s", top, skip, filter.replace(" ", "%20"));
        return executeGet(requestContext, path, MessageProcessingLogParser::buildMessageProcessingLogsResult);
    }

    public List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String integrationFlowName, Date startDate) {
        log.debug("#getMessageProcessingLogs(RequestContext requestContext, String integrationFlowName, Date startDate): {}, {}, {}", requestContext, integrationFlowName, startDate);
        String resourcePath = format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("IntegrationFlowName eq '%s' and LogStart gt datetime'%s'",
                integrationFlowName,
                GMT_DATE_FORMAT.format(startDate)
            )
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate): {}, {}, {}", requestContext, integrationFlowName, startDate);
        String resourcePath = format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("LogLevel eq 'TRACE' and IntegrationFlowName eq '%s' and LogStart gt datetime'%s' and LogStart gt datetime'%s' and (Status eq 'COMPLETED' or Status eq 'FAILED')",
                integrationFlowName,
                GMT_DATE_FORMAT.format(startDate),
                GMT_DATE_FORMAT.format(shiftDateTo55MinutesBackFromNow())
            )
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate): {}, {}, {}", requestContext, technicalNames, startDate);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String resourcePath = format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("LogLevel eq 'TRACE' and (%s) and LogStart gt datetime'%s' and LogStart gt datetime'%s' and (Status eq 'COMPLETED' or Status eq 'FAILED')",
                technicalNamesFilter,
                GMT_DATE_FORMAT.format(startDate),
                GMT_DATE_FORMAT.format(shiftDateTo55MinutesBackFromNow())
            )
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
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

        String resourcePath = expandCustomHeaders ? API_MSG_PROC_LOGS_WITH_PARAMS + "&$expand=CustomHeaderProperties" : API_MSG_PROC_LOGS_WITH_PARAMS;

        return getMessageProcessingLogs(
            requestContext,
            format(resourcePath, StringUtils.join(params, " or "))
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds) {
        log.debug("#getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds): {}, {}", requestContext, correlationIds);

        List<String> params = new ArrayList<>();
        for (String correlationId : correlationIds) {
            params.add(format("CorrelationId eq '%s'", correlationId));
        }

        return getMessageProcessingLogs(
            requestContext,
            format(API_MSG_PROC_LOGS_WITH_PARAMS, StringUtils.join(params, " or "))
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, String filter, Date leftBoundDate) {
        return getMessageProcessingLogsByFilter(requestContext, 1000, 0, filter, leftBoundDate, false);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, String filter, Date leftBoundDate, boolean expandCustomHeaders) {
        return getMessageProcessingLogsByFilter(requestContext, 1000, 0, filter, leftBoundDate, expandCustomHeaders);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        Date leftBoundDate,
        boolean expandCustomHeaders
    ) {
        log.debug("#getMessageProcessingLogsByFilter: top={}, skip={}, filter={}, leftBoundDate={}, expandCustomHeaders={}, requestContext={}",
            top,
            skip,
            filter,
            leftBoundDate,
            expandCustomHeaders,
            requestContext
        );
        String resourcePath = String.format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("%s and LogEnd ge datetime'%s'",
                filter.contains("or") ? format("(%s)", filter) : filter,
                GMT_DATE_FORMAT.format(leftBoundDate))
        );
        if (expandCustomHeaders) {
            resourcePath += format("&$expand=CustomHeaderProperties&$top=%d&$skip=%d", top, skip);
        }

        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter) {
        log.debug("#getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter): {}, {}", requestContext, filter);
        return getMessageProcessingLogs(requestContext, format(API_MSG_PROC_LOGS_ORDERED, top, filter));
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationId(RequestContext requestContext, String correlationId) {
        log.debug("#getMessageProcessingLogsByCorrelationId(RequestContext requestContext, String correlationId): {}, {}", requestContext, correlationId);
        String resourcePath = format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("CorrelationId eq '%s'", correlationId)
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames) {
        log.debug("#getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames): {}, {}, {}", requestContext, correlationIds, technicalNames);
        String correlationIdsFilter = buildCorrelationIdsFilter(correlationIds);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String resourcePath = format(API_MSG_PROC_LOGS_WITH_PARAMS,
            format("(%s) and (%s)", correlationIdsFilter, technicalNamesFilter)
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
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
        String resourcePath = expandCustomHeaders ? API_MSG_PROC_LOGS_PAGINATED + "&$expand=CustomHeaderProperties" : API_MSG_PROC_LOGS_PAGINATED;
        try {
            JSONObject jsonObjectD = callRestWs(
                requestContext,
                format(resourcePath, top, skip, filter),
                response -> new JSONObject(response).getJSONObject("d")
            );

            return extractMplsAndCountFromResponse(jsonObjectD);

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
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

        try {
            JSONObject jsonObjectD = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_PAGINATED_WITH_SELECTED_RESPONSE_FIELDS, top, skip, filter, responseFields),
                response -> new JSONObject(response).getJSONObject("d")
            );

            return extractMplsAndCountFromResponse(jsonObjectD);

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid) {
        log.debug("#getCustomHeaderProperties(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);
        String resourcePath = format("/api/v1/MessageProcessingLogs('%s')/CustomHeaderProperties?$format=json", messageGuid);

        try {

            JSONObject jsonObjectD = callRestWs(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d")
            );

            JSONArray jsonArray = jsonObjectD.getJSONArray("results");
            return createCustomHeaderProperties(jsonArray);

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public int getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter) {
        log.debug("#getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter): {}, {}", requestContext, filter);
        String resourcePath = format("/api/v1/MessageProcessingLogs/$count?$filter=%s", filter);
        try {
            int count = callRestWs(
                requestContext,
                resourcePath,
                response -> NumberUtils.isCreatable(response) ? NumberUtils.toInt(response) : 0
            );
            return count;
        } catch (Exception ex) {
            log.error("Error occurred while getting count of Message Processing Logs: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting count of Message Processing Logs: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String resourcePath) {

        try {

            JSONArray messageProcessingLogsJsonArray = callRestWs(
                requestContext,
                resourcePath,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );

            return createMessageProcessingLogsFromArray(messageProcessingLogsJsonArray);

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid) {
        try {
            JSONObject messageProcessingLogsObject = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_ID, messageGuid),
                response -> new JSONObject(response).getJSONObject("d")
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


    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getAttachmentsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {

            JSONArray attachmentsJsonArray = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_ATTACHMENTS, messageGuid),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
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

            JSONArray attachmentsJsonArray = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES, messageGuid),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );

            return createMessageProcessingLogAttachmentsForPayloads(attachmentsJsonArray);
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public MessageProcessingLogErrorInformation getErrorInformation(RequestContext requestContext, String messageId) {
        log.debug("#getErrorInformation(RequestContext requestContext, String messageId): {}, {}", requestContext, messageId);

        try {

            JSONObject jsonObject = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_ERROR_INFORMATION, messageId),
                response -> new JSONObject(response).getJSONObject("d")
            );

            MessageProcessingLogErrorInformation mplErrorInformation = new MessageProcessingLogErrorInformation();

            mplErrorInformation.setLastErrorModelStepId(optString(jsonObject, "LastErrorModelStepId"));

            String errorMessage = getErrorInformationValue(requestContext, messageId);
            mplErrorInformation.setErrorMessage(errorMessage);

            return mplErrorInformation;

        } catch (Exception ex) {
            log.error("Error occurred while collecting error information: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting error information:" + ex.getMessage(), ex);
        }
    }

    public String getErrorInformationValue(RequestContext requestContext, String messageGuid) {
        log.debug("#getErrorInformationValue(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);
        try {
            String errorInformationValue = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE, messageGuid),
                response -> response
            );
            return errorInformationValue;
        } catch (Exception ex) {
            log.error("Error occurred while collecting error information value: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting error information value:" + ex.getMessage(), ex);
        }
    }

    public byte[] getAttachment(RequestContext requestContext, String attachmentId) {
        log.debug("#getAttachment(RequestContext requestContext, String attachmentId): {}, {}", requestContext, attachmentId);

        try {

            String responseText = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOG_ATTACHMENT, attachmentId),
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

    public byte[] getPersistedAttachment(RequestContext requestContext, String attachmentId) {
        log.debug("#getPersistedAttachment(RequestContext requestContext, String attachmentId): {}, {}", requestContext, attachmentId);
        try {
            String responseText = callRestWs(
                requestContext,
                format(API_MSG_STORE_ENTRIES_VALUE, attachmentId),
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

    public List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid) {
        log.debug("#getRunsMetadata(RequestContext requestContext, String messageGuid): {}, {}", requestContext, messageGuid);

        try {

            JSONArray runsJsonArray = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOGS_RUNS, messageGuid),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );

            return createMessageProcessingLogAttachmentsForRuns(runsJsonArray, messageGuid);
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogRunStep> getRunSteps(RequestContext requestContext, String runId) {
        log.debug("#getRunSteps(RequestContext requestContext, String runId): {}, {}", requestContext, runId);
        try {
            List<JSONObject> jsonObjectRunSteps = getRunStepJsonObjects(requestContext, runId);
            return MessageProcessingLogRunStepParser.createMessageProcessingLogRunSteps(jsonObjectRunSteps);
        } catch (Exception ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
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

        JSONArray traceMessagesJsonArray = callRestWs(
            requestContext,
            format(API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES, runId, runStep.getChildCount()),
            response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
        );

        if (traceMessagesJsonArray.isEmpty()) {
            return null;
        }

        /*If traceMessagesJsonArray has multiple messages, we need to find the most appropriate one.
        Its SAP_TRACE_HEADER_<some digits>_MessageType property should be equal to "STEP". If it's not found, we just take the first one.
        Maybe later we will need to download and handle all the messages.*/
        TraceMessageHolder traceMessageHolder = new TraceMessageHolder();
        if (traceMessagesJsonArray.length() > 1) {
            for (int i = 0; i < traceMessagesJsonArray.length() && traceMessageHolder.getTraceMessageElement() == null; i++) {
                JSONObject currentTraceMessage = traceMessagesJsonArray.getJSONObject(i);
                JSONArray traceMessagePropertiesJsonArray = callRestWs(
                    requestContext,
                    format(API_TRACE_MESSAGE_PROPERTIES, optString(currentTraceMessage, "TraceId")),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
                );
                setTraceMessageHolder(traceMessageHolder, traceMessagePropertiesJsonArray, currentTraceMessage);
            }
        }
        JSONObject traceMessageElement = Optional.ofNullable(traceMessageHolder.getTraceMessageElement())
            .orElse(getDefaultValueOfTraceMessageElement(traceMessagesJsonArray));
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
                traceMessagePropertiesJsonArray = callRestWs(
                    requestContext,
                    format(API_TRACE_MESSAGE_PROPERTIES, traceMessage.getTraceId()),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
                );
            }
            setTraceMessageHeader(traceMessage, traceMessagePropertiesJsonArray);
            JSONArray traceMessageExchangePropertiesJsonArray = callRestWs(
                requestContext,
                format(API_TRACE_MESSAGE_EXCHANGE_PROPERTIES, traceMessage.getTraceId()),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );
            setTraceMessageExchange(traceMessage, traceMessageExchangePropertiesJsonArray);
        }
        return traceMessage;
    }

    private List<JSONObject> getRunStepJsonObjects(RequestContext requestContext, String runId) {
        int iterNumber = 0;
        Integer numberOfIterations = null;

        List<JSONObject> jsonObjectRunSteps = new ArrayList<>();
        do {
            int skip = MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION * iterNumber;
            JSONObject dObject = callRestWs(
                requestContext,
                format(API_MSG_PROC_LOG_RUN_STEPS, runId, MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION, skip),
                response -> new JSONObject(response).getJSONObject("d")
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

    public byte[] getPayloadForMessage(RequestContext requestContext, String traceId) {
        log.debug("#getPayloadForMessage(RequestContext requestContext, String traceId): {}, {}", requestContext, traceId);
        try {

            String payloadResponse = callRestWs(
                requestContext,
                format(API_TRACE_MESSAGE_PAYLOAD, traceId),
                response -> response
            );

            if (StringUtils.isNotBlank(payloadResponse)) {
                return payloadResponse.getBytes(StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    private Pair<List<MessageProcessingLog>, Integer> extractMplsAndCountFromResponse(JSONObject jsonObjectD) {
        String totalCountString = optString(jsonObjectD, "__count");
        Integer totalMessagesCount = null;
        if (NumberUtils.isCreatable(totalCountString)) {
            totalMessagesCount = NumberUtils.toInt(totalCountString);
        }
        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        return new MutablePair<>(createMessageProcessingLogsFromArray(messageProcessingLogsJsonArray), totalMessagesCount);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
