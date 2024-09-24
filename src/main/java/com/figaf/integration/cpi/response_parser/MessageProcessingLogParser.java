package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.cpi.entity.AdditionalPayloadType;
import com.figaf.integration.cpi.entity.message_processing.CustomHeaderProperty;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLog;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogAttachment;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRun;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;

/**
 * @author Arsenii Istlentev
 */
public class MessageProcessingLogParser {

    public static Pair<List<MessageProcessingLog>, Integer> buildMessageProcessingLogsResult(String body) {
        JSONObject jsonObjectD = new JSONObject(body).getJSONObject("d");

        String totalCountString = optString(jsonObjectD, "__count");
        Integer totalMessagesCount = null;
        if (NumberUtils.isCreatable(totalCountString)) {
            totalMessagesCount = NumberUtils.toInt(totalCountString);
        }

        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        return getMessageProcessingLogsToCount(totalMessagesCount, messageProcessingLogsJsonArray);
    }

    private static Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsToCount(Integer totalMessagesCount, JSONArray messageProcessingLogsJsonArray) {
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();
        for (int ind = 0; ind < messageProcessingLogsJsonArray.length(); ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind).getJSONObject("Log");

            MessageProcessingLog messageProcessingLog = fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, totalMessagesCount);
    }

    public static Pair<List<MessageProcessingLog>, Integer> buildMessageProcessingLogsResult(String body, int totalCount) {
        JSONObject jsonObjectD = new JSONObject(body).getJSONObject("d");
        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        return getMessageProcessingLogsToCount(totalCount, messageProcessingLogsJsonArray);
    }

    public static MessageProcessingLog fillMessageProcessingLog(JSONObject messageProcessingLogElement) {
        MessageProcessingLog messageProcessingLog = new MessageProcessingLog();
        messageProcessingLog.setMessageGuid(optString(messageProcessingLogElement, "MessageGuid"));
        messageProcessingLog.setCorrelationId(optString(messageProcessingLogElement, "CorrelationId"));
        messageProcessingLog.setIntegrationFlowName(optString(messageProcessingLogElement, "IntegrationFlowName"));
        messageProcessingLog.setLogLevel(optString(messageProcessingLogElement, "LogLevel"));
        messageProcessingLog.setStatus(optString(messageProcessingLogElement, "Status"));
        messageProcessingLog.setCustomStatus(optString(messageProcessingLogElement, "CustomStatus"));
        messageProcessingLog.setApplicationMessageId(optString(messageProcessingLogElement, "ApplicationMessageId"));
        messageProcessingLog.setApplicationMessageType(optString(messageProcessingLogElement, "ApplicationMessageType"));
        messageProcessingLog.setSender(optString(messageProcessingLogElement, "Sender"));
        messageProcessingLog.setReceiver(optString(messageProcessingLogElement, "Receiver"));
        messageProcessingLog.setAlternateWebLink(optString(messageProcessingLogElement, "AlternateWebLink"));
        messageProcessingLog.setLogStart(CpiApiUtils.parseDate(optString(messageProcessingLogElement, "LogStart")));
        messageProcessingLog.setLogEnd(CpiApiUtils.parseDate(optString(messageProcessingLogElement, "LogEnd")));
        messageProcessingLog.setCustomHeaderProperties(CpiApiUtils.parseCustomerHeaderProperties(messageProcessingLogElement));
        return messageProcessingLog;
    }

    public static List<MessageProcessingLogAttachment> createMessageProcessingLogAttachmentsForAttachments(JSONArray attachmentsJsonArray) {
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
    }

    public static List<MessageProcessingLogAttachment> createMessageProcessingLogAttachmentsForPayloads(JSONArray attachmentsJsonArray) {
        List<MessageProcessingLogAttachment> attachments = new ArrayList<>();

        for (int ind = 0; ind < attachmentsJsonArray.length(); ++ind) {
            JSONObject attachmentElement = attachmentsJsonArray.getJSONObject(ind);
            MessageProcessingLogAttachment attachment = new MessageProcessingLogAttachment();
            attachment.setId(optString(attachmentElement, "Id"));
            attachment.setMessageGuid(optString(attachmentElement, "MessageGuid"));
            attachment.setDate(CpiApiUtils.parseDate(optString(attachmentElement, "TimeStamp")));
            attachment.setMessageStoreId(optString(attachmentElement, "MessageStoreId"));
            attachment.setName(String.format("%s-%s", attachment.getMessageStoreId(), attachment.getId().replace("sap-it-res:msg:", "")));
            attachment.setContentType("Persisted payload");
            attachment.setAttachmentType(AdditionalPayloadType.PERSISTED_MESSAGE);
            attachments.add(attachment);
        }
        return attachments;
    }

    public static List<MessageProcessingLogRun> createMessageProcessingLogAttachmentsForRuns(JSONArray runsJsonArray, String messageGuid) {
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
    }

    public static List<CustomHeaderProperty> createCustomHeaderProperties(JSONArray jsonArray) {
        List<CustomHeaderProperty> customHeaderProperties = new ArrayList<>();

        for (int ind = 0; ind < jsonArray.length(); ind++) {
            JSONObject customHeaderObject = jsonArray.getJSONObject(ind);
            CustomHeaderProperty customHeaderProperty = new CustomHeaderProperty();
            customHeaderProperty.setId(optString(customHeaderObject, "Id"));
            customHeaderProperty.setName(optString(customHeaderObject, "Name"));
            customHeaderProperty.setValue(optString(customHeaderObject, "Value"));
            customHeaderProperties.add(customHeaderProperty);
        }
        return customHeaderProperties;
    }

    public static List<MessageProcessingLog> createMessageProcessingLogsFromArray(JSONArray messageProcessingLogsJsonArray) {
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();
        for (int ind = 0; ind < messageProcessingLogsJsonArray.length(); ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);
            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }
        return messageProcessingLogs;
    }
}
