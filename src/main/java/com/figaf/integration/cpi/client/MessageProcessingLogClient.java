package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.AdditionalPayloadType;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class MessageProcessingLogClient extends CpiBaseClient {

    private final static int MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION = 500;
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
        String resourcePath = format(API_MSG_PROC_LOGS,
            format("IntegrationFlowName eq '%s' and LogStart gt datetime'%s'",
                integrationFlowName,
                GMT_DATE_FORMAT.format(startDate)
            )
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate) {
        log.debug("#getFinishedMessageProcessingLogsWithTraceLevel(RequestContext requestContext, String integrationFlowName, Date startDate): {}, {}, {}", requestContext, integrationFlowName, startDate);
        String resourcePath = format(API_MSG_PROC_LOGS,
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
        String resourcePath = format(API_MSG_PROC_LOGS,
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

        String resourcePath = expandCustomHeaders ? API_MSG_PROC_LOGS + "&$expand=CustomHeaderProperties" : API_MSG_PROC_LOGS;

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
            format(API_MSG_PROC_LOGS, StringUtils.join(params, " or "))
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
        String resourcePath = String.format(API_MSG_PROC_LOGS,
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
        String resourcePath = format(API_MSG_PROC_LOGS,
            format("CorrelationId eq '%s'", correlationId)
        );
        return getMessageProcessingLogs(requestContext, resourcePath);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames) {
        log.debug("#getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames): {}, {}, {}", requestContext, correlationIds, technicalNames);
        String correlationIdsFilter = buildCorrelationIdsFilter(correlationIds);
        String technicalNamesFilter = buildTechnicalNamesFilter(technicalNames);
        String resourcePath = format(API_MSG_PROC_LOGS,
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

    private Pair<List<MessageProcessingLog>, Integer> extractMplsAndCountFromResponse(JSONObject jsonObjectD) {
        String totalCountString = optString(jsonObjectD, "__count");
        Integer totalMessagesCount = null;
        if (NumberUtils.isCreatable(totalCountString)) {
            totalMessagesCount = NumberUtils.toInt(totalCountString);
        }

        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();

        for (int ind = 0; ind < messageProcessingLogsJsonArray.length(); ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);

            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, totalMessagesCount);
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
            List<CustomHeaderProperty> customHeaderPropertyList = new ArrayList<>();

            for (int ind = 0; ind < jsonArray.length(); ind++) {
                JSONObject customHeaderObject = jsonArray.getJSONObject(ind);
                CustomHeaderProperty customHeaderProperty = new CustomHeaderProperty();
                customHeaderProperty.setId(optString(customHeaderObject, "Id"));
                customHeaderProperty.setName(optString(customHeaderObject, "Name"));
                customHeaderProperty.setValue(optString(customHeaderObject, "Value"));
                customHeaderPropertyList.add(customHeaderProperty);
            }

            return customHeaderPropertyList;

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

            List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();

            for (int ind = 0; ind < messageProcessingLogsJsonArray.length(); ind++) {
                JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);

                MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogElement);
                messageProcessingLogs.add(messageProcessingLog);
            }

            return messageProcessingLogs;

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

            List<MessageProcessingLogAttachment> attachments = new ArrayList<>();

            for (int ind = 0; ind < attachmentsJsonArray.length(); ind++) {
                JSONObject attachmentElement = attachmentsJsonArray.getJSONObject(ind);

                MessageProcessingLogAttachment attachment = new MessageProcessingLogAttachment();
                attachment.setId(optString(attachmentElement, "Id"));
                attachment.setMessageGuid(optString(attachmentElement, "MessageGuid"));
                attachment.setDate(CpiApiUtils.parseDate(optString(attachmentElement, "TimeStamp")));
                attachment.setName(optString(attachmentElement, "Name"));
                attachment.setContentType(optString(attachmentElement, "ContentType"));
                String payloadSizeString = optString(attachmentElement, "PayloadSize");
                if (NumberUtils.isCreatable(payloadSizeString)) {
                    attachment.setPayloadSize(NumberUtils.toInt(payloadSizeString));
                }

                attachments.add(attachment);
            }

            return attachments;
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

            List<MessageProcessingLogAttachment> attachments = new ArrayList<>();

            for (int ind = 0; ind < attachmentsJsonArray.length(); ind++) {
                JSONObject attachmentElement = attachmentsJsonArray.getJSONObject(ind);

                MessageProcessingLogAttachment attachment = new MessageProcessingLogAttachment();
                attachment.setId(optString(attachmentElement, "Id"));
                attachment.setMessageGuid(optString(attachmentElement, "MessageGuid"));
                attachment.setDate(CpiApiUtils.parseDate(optString(attachmentElement, "TimeStamp")));
                attachment.setMessageStoreId(optString(attachmentElement, "MessageStoreId"));
                attachment.setName(format("%s-%s", attachment.getMessageStoreId(), attachment.getId().replace("sap-it-res:msg:", "")));
                attachment.setContentType("Persisted payload");
                attachment.setAttachmentType(AdditionalPayloadType.PERSISTED_MESSAGE);

                attachments.add(attachment);
            }

            return attachments;
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

            List<MessageProcessingLogRun> runs = new ArrayList<>();

            for (int ind = 0; ind < runsJsonArray.length(); ind++) {
                JSONObject runElement = runsJsonArray.getJSONObject(ind);

                MessageProcessingLogRun run = new MessageProcessingLogRun();
                run.setId(optString(runElement, "Id"));
                run.setRunStart(CpiApiUtils.parseDate(optString(runElement, "RunStart")));
                run.setRunStop(CpiApiUtils.parseDate(optString(runElement, "RunStop")));
                run.setLogLevel(optString(runElement, "LogLevel"));
                run.setOverallState(optString(runElement, "OverallState"));
                run.setProcessId(optString(runElement, "ProcessId"));
                run.setMessageProcessingLogId(messageGuid);

                runs.add(run);
            }

            return runs;
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
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

        if (traceMessagesJsonArray.length() == 0) {
            return null;
        }

        /*If traceMessagesJsonArray has multiple messages, we need to find the most appropriate one.
        Its SAP_TRACE_HEADER_<some digits>_MessageType property should be equal to "STEP". If it's not found, we just take the first one.
        Maybe later we will need to download and handle all the messages.*/
        JSONObject traceMessageElement = null;
        JSONArray foundTraceMessagePropertiesJsonArray = null;
        if (traceMessagesJsonArray.length() > 1) {
            for (int i = 0; i < traceMessagesJsonArray.length() && traceMessageElement == null; i++) {
                JSONObject currentTraceMessage = traceMessagesJsonArray.getJSONObject(i);
                JSONArray traceMessagePropertiesJsonArray = callRestWs(
                    requestContext,
                    format(API_TRACE_MESSAGE_PROPERTIES, optString(currentTraceMessage, "TraceId")),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
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
            traceMessageElement = traceMessagesJsonArray.getJSONObject(0);
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
                traceMessagePropertiesJsonArray = callRestWs(
                    requestContext,
                    format(API_TRACE_MESSAGE_PROPERTIES, traceMessage.getTraceId()),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
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

            JSONArray traceMessageExchangePropertiesJsonArray = callRestWs(
                requestContext,
                format(API_TRACE_MESSAGE_EXCHANGE_PROPERTIES, traceMessage.getTraceId()),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
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

    private Date shiftDateTo55MinutesBackFromNow() {
        return DateUtils.addMinutes(new Date(), -55);
    }

    private String buildTechnicalNamesFilter(List<String> technicalNames) {
        return technicalNames.stream()
            .map(technicalName -> format("IntegrationFlowName eq '%s'", technicalName))
            .collect(Collectors.joining(" or "));
    }

    private String buildCorrelationIdsFilter(List<String> correlationIds) {
        return correlationIds.stream()
            .map(correlationId -> format("CorrelationId eq '%s'", correlationId))
            .collect(Collectors.joining(" or "));
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

    private Integer defineNumberOfIterations(JSONObject dObject) {
        Integer numberOfIterations;
        String totalCountStr = dObject.getString("__count");
        int totalCount = Integer.parseInt(totalCountStr);
        numberOfIterations = totalCount % MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION == 0
            ? totalCount / MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION
            : totalCount / MAX_NUMBER_OF_RUN_STEPS_IN_ONE_ITERATION + 1;
        return numberOfIterations;
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

    @Override
    protected Logger getLogger() {
        return log;
    }

}
