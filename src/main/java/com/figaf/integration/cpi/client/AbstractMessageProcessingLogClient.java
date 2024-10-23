package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Kostas Charalambous
 */
public abstract class AbstractMessageProcessingLogClient extends CpiBaseClient {

    protected final static int MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION = 500;

    protected final static String LOCATION = "/location/";

    protected static final String API_MSG_PROC_LOGS = "/api/v1/MessageProcessingLogs";

    protected static final String API_MSG_PROC_LOGS_WITH_PARAMS = "/api/v1/MessageProcessingLogs?$format=json&$orderby=LogEnd&$filter=%s";

    protected static final String API_MSG_PROC_LOG_CUSTOM_HEADER = "/api/v1/MessageProcessingLogCustomHeaderProperties";

    protected static final String PAGINATED_SORTED_FILTERED_QUERY_PARAMS_TEMPLATE = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s";

    protected static final String PAGINATED_SORTED_FILTERED_SELECT_QUERY_PARAMS_TEMPLATE = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s&$select=%s";

    protected static final String SORTED_FILTERED_ALL_PAGES_QUERY_PARAMS_TEMPLATE = "$inlinecount=allpages&$format=json&$top=%d&$orderby=LogEnd&$filter=%s";

    protected static final String PAGINATED_EXPANDABLE_FILTERED_QUERY_PARAMS_TEMPLATE = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$expand=Log&$filter=%s";

    protected static final String QUERY_PARAMS_FOR_TRACES_WITH_IFLOW_NAME = "$format=json&$filter=LogLevel eq 'TRACE' and IntegrationFlowName eq '%s' and LogStart gt datetime'%s' and LogStart gt datetime'%s' and (Status eq 'COMPLETED' or Status eq 'FAILED')";

    protected static final String SORTED_FILTERED_QUERY_PARAMS_TEMPLATE = "$format=json&$orderby=LogEnd&$filter=%s";

    protected static final String QUERY_PARAMS_FOR_TRACES = "$format=json&$filter=LogLevel eq 'TRACE' and (%s) and LogStart gt datetime'%s' and LogStart gt datetime'%s' and (Status eq 'COMPLETED' or Status eq 'FAILED')";

    protected static final String API_MSG_PROC_LOGS_CUSTOM_HEADER = "/api/v1/MessageProcessingLogs('%s')/CustomHeaderProperties?$format=json";

    protected static final String FILTERED_QUERY_PARAMS_TEMPLATE = "$format=json&$filter=%s";

    protected final static String FILTER = "$filter=%s";

    protected static final String API_MSG_PROC_LOGS_COUNT = "/api/v1/MessageProcessingLogs/$count";

    protected static final String API_MSG_PROC_LOGS_ID = "/api/v1/MessageProcessingLogs('%s')?$format=json";

    protected static final String API_MSG_PROC_LOGS_ORDERED = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$orderby=LogEnd&$filter=%s";

    protected static final String API_MSG_PROC_LOGS_PAGINATED_WITH_SELECTED_RESPONSE_FIELDS = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s&$select=%s";

    protected static final String API_MSG_PROC_LOGS_PAGINATED = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s";

    protected static final String API_MSG_PROC_LOGS_ATTACHMENTS = "/api/v1/MessageProcessingLogs('%s')/Attachments?$format=json";

    protected static final String API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES = "/api/v1/MessageProcessingLogs('%s')/MessageStoreEntries?$format=json";

    protected static final String API_MSG_PROC_LOGS_ERROR_INFORMATION = "/api/v1/MessageProcessingLogs('%s')/ErrorInformation?$format=json";

    protected static final String API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE = "/api/v1/MessageProcessingLogs('%s')/ErrorInformation/$value";

    protected static final String API_MSG_PROC_LOG_ATTACHMENT = "/api/v1/MessageProcessingLogAttachments('%s')/$value";

    protected static final String API_MSG_PROC_LOGS_RUNS = "/api/v1/MessageProcessingLogs('%s')/Runs?$format=json";

    protected static final String API_MSG_PROC_LOG_RUN_STEPS = "/api/v1/MessageProcessingLogRuns('%s')/RunSteps?$format=json&$expand=RunStepProperties&$inlinecount=allpages&$top=%d&$skip=%d";

    protected static final String API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES = "/api/v1/MessageProcessingLogRunSteps(RunId='%s',ChildCount=%d)/TraceMessages?$format=json";

    protected static final String API_TRACE_MESSAGE_PAYLOAD = "/api/v1/TraceMessages(%sL)/$value";

    protected static final String API_TRACE_MESSAGE_EXCHANGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/ExchangeProperties?$format=json";

    protected static final String API_TRACE_MESSAGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/Properties?$format=json";

    protected static final String API_MSG_STORE_ENTRIES_VALUE = "/api/v1/MessageStoreEntries('%s')/$value";

    protected final static FastDateFormat GMT_DATE_FORMAT = FastDateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        TimeZone.getTimeZone("GMT")
    );

    public AbstractMessageProcessingLogClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        boolean expandCustomHeaders
    );

    public abstract List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid);

    public abstract List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid);

    public abstract MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid);

    public abstract String getErrorInformationValue(RequestContext requestContext, String messageGuid);

    public abstract List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid);

    public abstract List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid);

    public abstract int getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter);

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter);

    public abstract byte[] getPersistedAttachment(RequestContext requestContext, String attachmentId);

    public abstract byte[] getAttachment(RequestContext requestContext, String attachmentId);

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilterWithSelectedResponseFields(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        String responseFields
    );

    public abstract List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter);

    public abstract List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate);

    public abstract List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(RequestContext requestContext, List<String> technicalNames, Date startDate);

    public abstract List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames);

    public abstract List<MessageProcessingLog> getMessageProcessingLogsByMessageGuids(RequestContext requestContext, Set<String> messageGuids, boolean expandCustomHeaders);

    public abstract byte[] getPayloadForMessage(RequestContext requestContext, String traceId);

    public abstract List<MessageProcessingLogRunStep> getRunSteps(RequestContext requestContext, String runId);

    public abstract MessageProcessingLogRunStep.TraceMessage getTraceMessage(
        RequestContext requestContext,
        MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria,
        MessageProcessingLogRunStep runStep
    );

    public abstract List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds);

    protected Date shiftDateTo55MinutesBackFromNow() {
        return DateUtils.addMinutes(new Date(), -55);
    }

    protected String buildTechnicalNamesFilter(List<String> technicalNames) {
        return technicalNames.stream()
            .map(technicalName -> format("IntegrationFlowName eq '%s'", technicalName))
            .collect(Collectors.joining(" or "));
    }

    protected String buildCorrelationIdsFilter(List<String> correlationIds) {
        return correlationIds.stream()
            .map(correlationId -> format("CorrelationId eq '%s'", correlationId))
            .collect(Collectors.joining(" or "));
    }

    protected Integer defineNumberOfIterations(JSONObject dObject) {
        Integer numberOfIterations;
        String totalCountStr = dObject.getString("__count");
        int totalCount = Integer.parseInt(totalCountStr);
        numberOfIterations = totalCount % MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION == 0
            ? totalCount / MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION
            : totalCount / MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION + 1;
        return numberOfIterations;
    }

    protected void setTraceMessageHolder(
        TraceMessageHolder traceMessageHolder,
        JSONArray traceMessagePropertiesJsonArray,
        JSONObject currentTraceMessage
    ) {
        for (int traceMessagePropertyInd = 0; traceMessagePropertyInd < traceMessagePropertiesJsonArray.length(); traceMessagePropertyInd++) {
            JSONObject traceMessagePropertyElement = traceMessagePropertiesJsonArray.getJSONObject(traceMessagePropertyInd);
            String name = optString(traceMessagePropertyElement, "Name");
            if (name.startsWith("SAP_TRACE_HEADER_") && name.endsWith("_MessageType")) {
                String value = optString(traceMessagePropertyElement, "Value");
                if (value.equals("STEP")) {
                    traceMessageHolder.setTraceMessageElement(currentTraceMessage);
                    traceMessageHolder.setFoundTraceMessagePropertiesJsonArray(traceMessagePropertiesJsonArray);
                }
                break;
            }
        }
    }

    protected void setTraceMessageHeader(MessageProcessingLogRunStep.TraceMessage traceMessage, JSONArray traceMessagePropertiesJsonArray) {
        for (int traceMessagePropertyInd = 0; traceMessagePropertyInd < traceMessagePropertiesJsonArray.length(); traceMessagePropertyInd++) {
            JSONObject traceMessagePropertyElement = traceMessagePropertiesJsonArray.getJSONObject(traceMessagePropertyInd);

            traceMessage.getProperties().add(new MessageRunStepProperty(
                PropertyType.TRACE_MESSAGE_HEADER,
                optString(traceMessagePropertyElement, "Name"),
                optString(traceMessagePropertyElement, "Value")
            ));
        }
    }

    protected void setTraceMessageExchange(MessageProcessingLogRunStep.TraceMessage traceMessage, JSONArray traceMessageExchangePropertiesJsonArray) {
        for (int traceMessageExchangePropertyInd = 0; traceMessageExchangePropertyInd < traceMessageExchangePropertiesJsonArray.length(); traceMessageExchangePropertyInd++) {
            JSONObject traceMessageExchangePropertyElement = traceMessageExchangePropertiesJsonArray.getJSONObject(traceMessageExchangePropertyInd);

            traceMessage.getProperties().add(new MessageRunStepProperty(
                PropertyType.TRACE_MESSAGE_EXCHANGE,
                optString(traceMessageExchangePropertyElement, "Name"),
                optString(traceMessageExchangePropertyElement, "Value")
            ));
        }
    }

    protected JSONObject getDefaultValueOfTraceMessageElement(JSONArray runsJsonArray) {
        return runsJsonArray.getJSONObject(0);
    }

    protected MessageProcessingLogRunStep.TraceMessage createTraceMessage(
        JSONObject traceMessageElement,
        MessageProcessingLogRunStep runStep,
        MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria,
        RequestContext requestContext
    ) {
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
        return traceMessage;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    protected static class TraceMessageHolder {

        private JSONObject traceMessageElement;
        private JSONArray foundTraceMessagePropertiesJsonArray;
    }
}
