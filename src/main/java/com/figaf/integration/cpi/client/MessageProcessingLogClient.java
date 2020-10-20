package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class MessageProcessingLogClient extends CpiBaseClient {

    public MessageProcessingLogClient(String ssoUrl, HttpClientsFactory httpClientsFactory) {
        super(ssoUrl, httpClientsFactory);
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(CommonClientWrapperEntity commonClientWrapperEntity, int top, int skip, String filter) {
        log.debug("getMessageProcessingLogsByCustomHeader(CommonClientWrapperEntity commonClientWrapperEntity, int top, int skip, String filter): {}, {}, {}, {}", commonClientWrapperEntity, top, skip, filter);
        String path = String.format("/itspaces/odata/api/v1/MessageProcessingLogCustomHeaderProperties?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$expand=Log&$filter=%s", top, skip, filter.replace(" ", "%20"));
        return executeGet(commonClientWrapperEntity, path, MessageProcessingLogParser::buildMessageProcessingLogsResult);
    }


    public List<MessageProcessingLog> getMessageProcessingLogs(ConnectionProperties connectionProperties, String integrationFlowName, Date startDate) {
        log.debug("#getMessageProcessingLogs(ConnectionProperties connectionProperties, String integrationFlowName, Date startDate): {}, {}", connectionProperties, startDate);
        FastDateFormat dateFormat = FastDateFormat.getInstance(
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                TimeZone.getTimeZone("GMT")
        );
        String resourcePath = String.format(API_MSG_PROC_LOGS,
                String.format("LogLevel eq 'TRACE' and Status eq 'COMPLETED' and IntegrationFlowName eq '%s' and LogStart gt datetime'%s' and LogStart gt datetime'%s'",
                        integrationFlowName,
                        dateFormat.format(startDate),
                        dateFormat.format(DateUtils.addMinutes(new Date(), -55))
                )
        );
        return getMessageProcessingLogs(connectionProperties, resourcePath);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, String filter, Date startDate) {
        log.debug("#getMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, String filter, Date startDate): {}, {}, {}", connectionProperties, filter, startDate);
        FastDateFormat dateFormat = FastDateFormat.getInstance(
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                TimeZone.getTimeZone("GMT")
        );
        String resourcePath = String.format(API_MSG_PROC_LOGS,
                String.format("%s and LogStart gt datetime'%s'", filter, dateFormat.format(startDate))
        );

        return getMessageProcessingLogs(connectionProperties, resourcePath);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationId(ConnectionProperties connectionProperties, String correlationId) {
        log.debug("#getMessageProcessingLogsByCorrelationId(ConnectionProperties connectionProperties, String correlationId): {}, {}", connectionProperties, correlationId);
        String resourcePath = String.format(API_MSG_PROC_LOGS,
                String.format("CorrelationId eq '%s'", correlationId)
        );
        return getMessageProcessingLogs(connectionProperties, resourcePath);
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, int top, int skip, String filter) {
        log.debug("getMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, int top, int skip, String filter): {}, {}, {}, {}", connectionProperties, top, skip, filter);
        String resourcePath = String.format("/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s", top, skip, filter);

        try {

            JSONObject jsonObjectD = callRestWs(
                    connectionProperties,
                    resourcePath,
                    response -> new JSONObject(response).getJSONObject("d"),
                    null
            );

            String totalCountString = optString(jsonObjectD, "__count");
            Integer totalMessagesCount = null;
            if (NumberUtils.isNumber(totalCountString)) {
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

        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<CustomHeaderProperty> getCustomHeaderProperties(ConnectionProperties connectionProperties, String messageGuid) {
        log.debug("#getCustomHeaderProperties(ConnectionProperties connectionProperties, String messageGuid): {}, {}", connectionProperties, messageGuid);
        String resourcePath = String.format("/api/v1/MessageProcessingLogs('%s')/CustomHeaderProperties?$format=json", messageGuid);

        try {

            JSONObject jsonObjectD = callRestWs(
                    connectionProperties,
                    resourcePath,
                    response -> new JSONObject(response).getJSONObject("d"),
                    null
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

    public int getCountOfMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, String filter) {
        log.debug("#getCountOfMessageProcessingLogsByFilter(ConnectionProperties connectionProperties, String filter): {}, {}", connectionProperties, filter);
        String resourcePath = String.format("/api/v1/MessageProcessingLogs/$count?$filter=%s", filter);
        try {
            int count = callRestWs(
                    connectionProperties,
                    resourcePath,
                    response -> NumberUtils.isNumber(response) ? NumberUtils.toInt(response) : 0,
                    null
            );
            return count;
        } catch (Exception ex) {
            log.error("Error occurred while getting count of Message Processing Logs: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting count of Message Processing Logs: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLog> getMessageProcessingLogs(ConnectionProperties connectionProperties, String resourcePath) {

        try {

            JSONArray messageProcessingLogsJsonArray = callRestWs(
                    connectionProperties,
                    resourcePath,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    null
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

    public MessageProcessingLog getMessageProcessingLogByGuid(ConnectionProperties connectionProperties, String messageGuid) {
        try {
            JSONObject messageProcessingLogsObject = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_ID, messageGuid),
                    response -> new JSONObject(response).getJSONObject("d"),
                    null
            );
            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogsObject);
            return messageProcessingLog;
        } catch (Exception ex) {
            log.error("Error occurred while collecting message processing log: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting message processing log : " + ex.getMessage(), ex);
        }
    }


    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(ConnectionProperties connectionProperties, String messageGuid) {
        log.debug("#getAttachmentsMetadata(ConnectionProperties connectionProperties, String messageGuid): {}, {}", connectionProperties, messageGuid);

        try {

            JSONArray attachmentsJsonArray = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_ATTACHMENTS, messageGuid),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    null
            );

            List<MessageProcessingLogAttachment> attachments = new ArrayList<>();

            for (int ind = 0; ind < attachmentsJsonArray.length(); ind++) {
                JSONObject attachmentElement = attachmentsJsonArray.getJSONObject(ind);

                MessageProcessingLogAttachment attachment = new MessageProcessingLogAttachment();
                attachment.setId(optString(attachmentElement, "Id"));
                attachment.setMessageGuid(optString(attachmentElement, "MessageGuid"));
                String timeStamp = optString(attachmentElement, "TimeStamp");
                if (timeStamp != null) {
                    attachment.setDate(
                            new Timestamp(Long.parseLong(timeStamp.replaceAll("[^0-9]", "")))
                    );
                }
                attachment.setName(optString(attachmentElement, "Name"));
                attachment.setContentType(optString(attachmentElement, "ContentType"));
                String payloadSizeString = optString(attachmentElement, "PayloadSize");
                if (NumberUtils.isNumber(payloadSizeString)) {
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

    public List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(ConnectionProperties connectionProperties, String messageGuid) {
        log.debug("#getMessageStoreEntriesPayloads(ConnectionProperties connectionProperties, String messageGuid): {}, {}", connectionProperties, messageGuid);

        try {

            JSONArray attachmentsJsonArray = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES, messageGuid),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    null
            );

            List<MessageProcessingLogAttachment> attachments = new ArrayList<>();

            for (int ind = 0; ind < attachmentsJsonArray.length(); ind++) {
                JSONObject attachmentElement = attachmentsJsonArray.getJSONObject(ind);

                MessageProcessingLogAttachment attachment = new MessageProcessingLogAttachment();
                attachment.setId(optString(attachmentElement, "Id"));
                attachment.setMessageGuid(optString(attachmentElement, "MessageGuid"));
                String timeStamp = optString(attachmentElement, "TimeStamp");
                if (timeStamp != null) {
                    attachment.setDate(
                            new Timestamp(Long.parseLong(timeStamp.replaceAll("[^0-9]", "")))
                    );
                }
                attachment.setName(String.format("%s-%s", optString(attachmentElement, "MessageStoreId"), attachment.getId().replace("sap-it-res:msg:", "")));
                attachment.setContentType("Persisted payload");
                attachment.setAttachmentType(MessageProcessingLogAttachmentType.PERSISTED);

                attachments.add(attachment);
            }

            return attachments;
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public MessageProcessingLogErrorInformation getErrorInformation(ConnectionProperties connectionProperties, String messageId) {
        log.debug("#getErrorInformation(ConnectionProperties connectionProperties, String messageId): {}, {}", connectionProperties, messageId);

        try {

            JSONObject jsonObject = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_ERROR_INFORMATION, messageId),
                    response -> new JSONObject(response).getJSONObject("d"),
                    null
            );

            MessageProcessingLogErrorInformation mplErrorInformation = new MessageProcessingLogErrorInformation();

            mplErrorInformation.setLastErrorModelStepId(optString(jsonObject, "LastErrorModelStepId"));

            String errorMessage = getErrorInformationValue(connectionProperties, messageId);
            mplErrorInformation.setErrorMessage(errorMessage);

            return mplErrorInformation;

        } catch (Exception ex) {
            log.error("Error occurred while collecting error information: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting error information:" + ex.getMessage(), ex);
        }
    }

    public String getErrorInformationValue(ConnectionProperties connectionProperties, String messageGuid) {
        log.debug("#getErrorInformationValue(ConnectionProperties connectionProperties, String messageGuid): {}, {}", connectionProperties, messageGuid);
        try {
            String errorInformationValue = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE, messageGuid),
                    response -> response,
                    null
            );
            return errorInformationValue;
        } catch (Exception ex) {
            log.error("Error occurred while collecting error information value: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while collecting error information value:" + ex.getMessage(), ex);
        }
    }

    public byte[] getAttachment(ConnectionProperties connectionProperties, String attachmentId) {
        log.debug("#getAttachment(ConnectionProperties connectionProperties, String attachmentId): {}, {}", connectionProperties, attachmentId);

        try {

            String responseText = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOG_ATTACHMENT, attachmentId),
                    response -> response,
                    null
            );

            return responseText.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ClientIntegrationException(ex);
        }

    }

    public byte[] getPersistedAttachment(ConnectionProperties connectionProperties, String attachmentId) {
        log.debug("#getPersistedAttachment(ConnectionProperties connectionProperties, String attachmentId): {}, {}", connectionProperties, attachmentId);
        try {
            String responseText = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_STORE_ENTRIES_VALUE, attachmentId),
                    response -> response,
                    null
            );
            return responseText.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ClientIntegrationException(ex);
        }
    }

    public List<MessageProcessingLogRun> getRunsMetadata(ConnectionProperties connectionProperties, String messageGuid) {
        log.debug("#getRunsMetadata(ConnectionProperties connectionProperties, String messageGuid): {}, {}", connectionProperties, messageGuid);

        try {

            JSONArray runsJsonArray = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOGS_RUNS, messageGuid),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    null
            );

            List<MessageProcessingLogRun> runs = new ArrayList<>();

            for (int ind = 0; ind < runsJsonArray.length(); ind++) {
                JSONObject runElement = runsJsonArray.getJSONObject(ind);

                MessageProcessingLogRun run = new MessageProcessingLogRun();
                run.setId(optString(runElement, "Id"));
                String runStart = optString(runElement, "RunStart");
                if (runStart != null) {
                    run.setRunStart(
                            new Timestamp(Long.parseLong(runStart.replaceAll("[^0-9]", "")))
                    );
                }
                String runStop = optString(runElement, "RunStop");
                if (runStop != null) {
                    run.setRunStop(
                            new Timestamp(Long.parseLong(runStop.replaceAll("[^0-9]", "")))
                    );
                }
                run.setLogLevel(optString(runElement, "LogLevel"));
                run.setOverallState(optString(runElement, "OverallState"));
                run.setProcessId(optString(runElement, "ProcessId"));

                runs.add(run);
            }

            return runs;
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public List<MessageProcessingLogRunStep> getRunSteps(ConnectionProperties connectionProperties, String runId, MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria) {
        log.debug("#getRunWithSteps(ConnectionProperties connectionProperties, String runId, MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria): {}, {}, {}",
                connectionProperties, runId, runStepSearchCriteria
        );

        try {

            RestTemplate restTemplate = httpClientsFactory.createRestTemplate(new BasicAuthenticationInterceptor(connectionProperties.getUsername(), connectionProperties.getPassword()));

            JSONArray runStepsJsonArray = callRestWs(
                    connectionProperties,
                    String.format(API_MSG_PROC_LOG_RUN_STEPS, runId),
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                    restTemplate
            );

            List<MessageProcessingLogRunStep> runSteps = new ArrayList<>();

            boolean firstStepWithPayloadChecked = false;
            for (int ind = runStepsJsonArray.length() - 1; ind >= 0; ind--) {
                JSONObject runStepElement = runStepsJsonArray.getJSONObject(ind);

                MessageProcessingLogRunStep runStep = new MessageProcessingLogRunStep();
                runStep.setRunId(optString(runStepElement, "RunId"));
                runStep.setChildCount(runStepElement.getInt("ChildCount"));
                String stepStart = optString(runStepElement, "StepStart");
                if (stepStart != null) {
                    runStep.setStepStart(
                            new Timestamp(Long.parseLong(stepStart.replaceAll("[^0-9]", "")))
                    );
                }
                String stepStop = optString(runStepElement, "StepStop");
                if (stepStop != null) {
                    runStep.setStepStop(
                            !runStepElement.isNull("StepStop")
                                    ? new Timestamp(Long.parseLong(stepStop.replaceAll("[^0-9]", "")))
                                    : null
                    );
                }
                runStep.setStepId(optString(runStepElement, "StepId"));
                runStep.setModelStepId(optString(runStepElement, "ModelStepId"));
                runStep.setBranchId(optString(runStepElement, "BranchId"));
                runStep.setStatus(optString(runStepElement, "Status"));
                runStep.setError(optString(runStepElement, "Error"));
                runStep.setActivity(optString(runStepElement, "Activity"));

                JSONArray runStepPropertiesJsonArray = runStepElement.getJSONObject("RunStepProperties").getJSONArray("results");
                for (int runStepPropertyInd = 0; runStepPropertyInd < runStepPropertiesJsonArray.length(); runStepPropertyInd++) {
                    JSONObject runStepPropertyElement = runStepPropertiesJsonArray.getJSONObject(runStepPropertyInd);

                    runStep.getRunStepProperties().add(new MessageRunStepProperty(
                            PropertyType.RUN_STEP_PROPERTY,
                            optString(runStepPropertyElement, "Name"),
                            optString(runStepPropertyElement, "Value")
                    ));
                }


                if (runStep.matches(runStepSearchCriteria) || runStep.getModelStepId().startsWith("EndEvent") || !firstStepWithPayloadChecked) {

                    JSONArray traceMessagesJsonArray = callRestWs(
                            connectionProperties,
                            String.format(API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES, runId, runStep.getChildCount()),
                            response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                            restTemplate
                    );

                    if (traceMessagesJsonArray.length() == 0) {
                        continue;
                    }

                    firstStepWithPayloadChecked = true;

                    // for now we take only first message
                    JSONObject traceMessageElement = traceMessagesJsonArray.getJSONObject(0);

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
                        getPayloadForMessage(connectionProperties, traceMessage.getTraceId());
                    }

                    if (runStepSearchCriteria.isInitTraceMessageProperties()) {

                        JSONArray traceMessagePropertiesJsonArray = callRestWs(
                                connectionProperties,
                                String.format(API_TRACE_MESSAGE_PROPERTIES, traceMessage.getTraceId()),
                                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                                restTemplate
                        );

                        for (int traceMessagePropertyInd = 0; traceMessagePropertyInd < traceMessagePropertiesJsonArray.length(); traceMessagePropertyInd++) {
                            JSONObject traceMessagePropertyElement = traceMessagePropertiesJsonArray.getJSONObject(traceMessagePropertyInd);

                            traceMessage.getProperties().add(new MessageRunStepProperty(
                                    PropertyType.TRACE_MESSAGE_HEADER,
                                    optString(traceMessagePropertyElement, "Name"),
                                    optString(traceMessagePropertyElement, "Value")
                            ));
                        }

                        JSONArray traceMessageExchangePropertiesJsonArray = callRestWs(
                                connectionProperties,
                                String.format(API_TRACE_MESSAGE_EXCHANGE_PROPERTIES, traceMessage.getTraceId()),
                                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results"),
                                restTemplate
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

                    runStep.getTraceMessages().add(traceMessage);
                    runSteps.add(runStep);

                }

            }

            return runSteps;
        } catch (Exception ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public byte[] getPayloadForMessage(ConnectionProperties connectionProperties, String traceId) {
        log.debug("#getPayloadForMessage(ConnectionProperties connectionProperties, String traceId): {}, {}", connectionProperties, traceId);
        try {
            RestTemplate restTemplate = httpClientsFactory.createRestTemplate(new BasicAuthenticationInterceptor(connectionProperties.getUsername(), connectionProperties.getPassword()));

            String payloadResponse = callRestWs(
                    connectionProperties,
                    String.format(API_TRACE_MESSAGE_PAYLOAD, traceId),
                    response -> response,
                    restTemplate
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
