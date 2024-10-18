package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import com.figaf.integration.cpi.utils.CpiApiUtils;
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
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams, filter);

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
            int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, filter);
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
            return getMessageProcessingLogsToTotalCount(requestContext, resourcePath, queryParams, filter);
        } catch (Exception ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter) {
        log.debug("#getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter): {}, {}", requestContext, filter);
        return getMessageProcessingLogs(requestContext, top, filter);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate): {}, {}, {}", requestContext, integrationFlowName, startDate);
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String queryParams = format(QUERY_PARAMS_FOR_TRACES_WITH_IFLOW_NAME,
            integrationFlowName,
            GMT_DATE_FORMAT.format(startDate),
            GMT_DATE_FORMAT.format(shiftDateTo55MinutesBackFromNow())
        );
        return getMessageProcessingLogs(requestContext, resourcePath, queryParams);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate): {}, {}, {}", requestContext, technicalNames, startDate);
        String resourcePathWithParams = LOCATION + runtimeId + API_MSG_PROC_LOGS;
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
        String resourcePathWithParams = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String correlationIdsFilter = buildCorrelationIdsFilter(correlationIds);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String queryParams = format(QUERY_PARAMS_FILTER, format("(%s) and (%s)", correlationIdsFilter, technicalNamesFilter));
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

        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String filter = format(QUERY_PARAMS_FILTER, format("(%s)", StringUtils.join(params, " or ")));
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
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES;
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
        JSONObject traceMessageElement = null;
        JSONArray foundTraceMessagePropertiesJsonArray = null;
        if (runsJsonArray.length() > 1) {
            for (int i = 0; i < runsJsonArray.length() && traceMessageElement == null; i++) {
                JSONObject currentTraceMessage = runsJsonArray.getJSONObject(i);
                String resourcePathProperties = LOCATION + runtimeId + API_TRACE_MESSAGE_PROPERTIES;
                String fullPathProperties = format(resourcePathProperties, optString(currentTraceMessage, "TraceId"));
                JSONArray traceMessagePropertiesJsonArray = executeGet(
                    requestContext,
                    fullPathProperties,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    String.class
                );

                for (int traceMessagePropertyInd = 0; traceMessagePropertyInd < traceMessagePropertiesJsonArray.length(); traceMessagePropertyInd++) {
                    JSONObject traceMessagePropertyElement = traceMessagePropertiesJsonArray.getJSONObject(traceMessagePropertyInd);
                    String name = optString(traceMessagePropertyElement, "Name");
                    if (name.startsWith("SAP_TRACE_HEADER_") && name.endsWith("_MessageType")) {
                        String value = optString(traceMessagePropertyElement, "Value");
                        if (value.equals("STEP")) {
                            traceMessageElement = currentTraceMessage;
                            foundTraceMessagePropertiesJsonArray = traceMessagePropertiesJsonArray;
                        }
                        break;
                    }
                }
            }
        }
        if (traceMessageElement == null) {
            traceMessageElement = runsJsonArray.getJSONObject(0);
        }

        MessageProcessingLogRunStep.TraceMessage traceMessage = new MessageProcessingLogRunStep.TraceMessage();
        traceMessage.setTraceId(optString(traceMessageElement, "TraceId"));
        traceMessage.setMessageId(optString(traceMessageElement, "MplId"));
        traceMessage.setModelStepId(optString(traceMessageElement, "ModelStepId"));
        String payloadSize = optString(traceMessageElement, "PayloadSize");
        if (payloadSize != null) {
            traceMessage.setPayloadSize(Long.parseLong(payloadSize));
        }
        traceMessage.setMimeType(optString(traceMessageElement, "MimeType"));
        traceMessage.setProcessingDate(runStep.getStepStop());

        if (runStepSearchCriteria.isInitTraceMessagePayload()) {
            byte[] payloadForMessage = getPayloadForMessage(requestContext, traceMessage.getTraceId());
            traceMessage.setPayload(payloadForMessage);
        }

        if (runStepSearchCriteria.isInitTraceMessageProperties()) {

            JSONArray traceMessagePropertiesJsonArray;
            /*We could already have traceMessagePropertiesJsonArray if there are multiple trace messages.
             In this case we don't need to make the same request twice.*/
            if (foundTraceMessagePropertiesJsonArray != null) {
                traceMessagePropertiesJsonArray = foundTraceMessagePropertiesJsonArray;
            } else {
                String resourcePathProperties = LOCATION + runtimeId + API_TRACE_MESSAGE_PROPERTIES;
                String fullPathProperties = format(resourcePathProperties, traceMessage.getTraceId());
                traceMessagePropertiesJsonArray = executeGet(
                    requestContext,
                    fullPathProperties,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    String.class
                );
            }

            for (int traceMessagePropertyInd = 0; traceMessagePropertyInd < traceMessagePropertiesJsonArray.length(); traceMessagePropertyInd++) {
                JSONObject traceMessagePropertyElement = traceMessagePropertiesJsonArray.getJSONObject(traceMessagePropertyInd);

                traceMessage.getProperties().add(new MessageRunStepProperty(
                    PropertyType.TRACE_MESSAGE_HEADER,
                    optString(traceMessagePropertyElement, "Name"),
                    optString(traceMessagePropertyElement, "Value")
                ));
            }
            String resourcePathProperties = LOCATION + runtimeId + API_TRACE_MESSAGE_EXCHANGE_PROPERTIES;
            String fullPathProperties = format(resourcePathProperties, traceMessage.getTraceId());
            JSONArray traceMessageExchangePropertiesJsonArray = executeGet(
                requestContext,
                fullPathProperties,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                String.class
            );

            for (int traceMessageExchangePropertyInd = 0; traceMessageExchangePropertyInd < traceMessageExchangePropertiesJsonArray.length(); traceMessageExchangePropertyInd++) {
                JSONObject traceMessageExchangePropertyElement = traceMessageExchangePropertiesJsonArray.getJSONObject(traceMessageExchangePropertyInd);

                traceMessage.getProperties().add(new MessageRunStepProperty(
                    PropertyType.TRACE_MESSAGE_EXCHANGE,
                    optString(traceMessageExchangePropertyElement, "Name"),
                    optString(traceMessageExchangePropertyElement, "Value")
                ));
            }
        }
        return traceMessage;
    }

    public List<MessageProcessingLogRunStep> getRunSteps(RequestContext requestContext, String runId) {
        log.debug("#getRunSteps(RequestContext requestContext, String runId): {}, {}", requestContext, runId);
        try {

            List<JSONObject> jsonObjectRunSteps = getRunStepJsonObjects(requestContext, runId);

            List<MessageProcessingLogRunStep> runSteps = new ArrayList<>();
            for (int ind = jsonObjectRunSteps.size() - 1; ind >= 0; ind--) {
                JSONObject runStepElement = jsonObjectRunSteps.get(ind);

                MessageProcessingLogRunStep runStep = new MessageProcessingLogRunStep();
                runStep.setRunId(optString(runStepElement, "RunId"));
                runStep.setChildCount(runStepElement.getInt("ChildCount"));
                runStep.setStepStart(CpiApiUtils.parseDate(optString(runStepElement, "StepStart")));
                if (!runStepElement.isNull("StepStop")) {
                    runStep.setStepStop(CpiApiUtils.parseDate(optString(runStepElement, "StepStop")));
                }
                runStep.setStepId(optString(runStepElement, "StepId"));
                runStep.setModelStepId(optString(runStepElement, "ModelStepId"));
                runStep.setBranchId(optString(runStepElement, "BranchId"));
                runStep.setStatus(optString(runStepElement, "Status"));
                runStep.setError(optString(runStepElement, "Error"));
                runStep.setActivity(optString(runStepElement, "Activity"));

                JSONArray runStepPropertiesJsonArray = runStepElement.getJSONObject("RunStepProperties").getJSONArray("results");
                String traceId = null;
                for (int runStepPropertyInd = 0; runStepPropertyInd < runStepPropertiesJsonArray.length(); runStepPropertyInd++) {
                    JSONObject runStepPropertyElement = runStepPropertiesJsonArray.getJSONObject(runStepPropertyInd);

                    String name = optString(runStepPropertyElement, "Name");
                    String value = optString(runStepPropertyElement, "Value");
                    //getRunStepProperties is not used anywhere
                    runStep.getRunStepProperties().add(new MessageRunStepProperty(
                        PropertyType.RUN_STEP_PROPERTY,
                        name,
                        value
                    ));
                    if ("TraceIds".equals(name) && StringUtils.isNotBlank(value)) {
                        //This regex means that we want to find only the first value of the list
                        // since we rely on only one first trace message everywhere in the logic.
                        // In other words, we don't support multiple trace messages for a single run step
                        traceId = value.replaceAll("\\[(\\d*).*]", "$1");
                        if (StringUtils.isNotBlank(traceId)) {
                            runStep.setTraceId(traceId);
                        }
                    }
                }

                //If traceId == null it means that this run step doesn't have payload. We don't need such messages at all.
                if (traceId != null) {
                    runSteps.add(runStep);
                }
            }

            return runSteps;
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public byte[] getPayloadForMessage(RequestContext requestContext, String traceId) {
        log.debug("#getPayloadForMessage(RequestContext requestContext, String traceId): {}, {}", requestContext, traceId);
        try {
            String resourcePath = LOCATION + runtimeId + API_TRACE_MESSAGE_PAYLOAD;
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
        String resourcePath = LOCATION + runtimeId + API_MSG_PROC_LOGS;
        String queryParams = format(QUERY_PARAMS_WITH_ORDER_AND_FILTER, StringUtils.join(params, " or "));
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
            String resourcePathProperties = LOCATION + runtimeId + API_MSG_PROC_LOG_RUN_STEPS;
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


    private List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String resourcePath, String queryParams) {
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
        String filter
    ) throws URISyntaxException {
        URI uri = new URI(null, null, resourcePath, queryParams, null);
        JSONObject jsonObjectD = executeGet(
            requestContext,
            uri.toString(),
            response -> new JSONObject(response).getJSONObject("d"),
            String.class
        );
        int totalCount = getCountOfMessageProcessingLogsByFilter(requestContext, filter);
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
