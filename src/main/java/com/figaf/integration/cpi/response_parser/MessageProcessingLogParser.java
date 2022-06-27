package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLog;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
public class MessageProcessingLogParser {

    public static Pair<List<MessageProcessingLog>, Integer> buildMessageProcessingLogsResult(String body) {
        JSONObject jsonObjectD = new JSONObject(body).getJSONObject("d");

        String totalCountString = Utils.optString(jsonObjectD, "__count");
        Integer totalMessagesCount = null;
        if (NumberUtils.isCreatable(totalCountString)) {
            totalMessagesCount = NumberUtils.toInt(totalCountString);
        }

        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();

        for (int ind = 0; ind < messageProcessingLogsJsonArray.length(); ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind).getJSONObject("Log");

            MessageProcessingLog messageProcessingLog = fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, totalMessagesCount);
    }

    public static MessageProcessingLog fillMessageProcessingLog(JSONObject messageProcessingLogElement) {
        MessageProcessingLog messageProcessingLog = new MessageProcessingLog();
        messageProcessingLog.setMessageGuid(Utils.optString(messageProcessingLogElement, "MessageGuid"));
        messageProcessingLog.setCorrelationId(Utils.optString(messageProcessingLogElement, "CorrelationId"));
        messageProcessingLog.setIntegrationFlowName(Utils.optString(messageProcessingLogElement, "IntegrationFlowName"));
        messageProcessingLog.setLogLevel(Utils.optString(messageProcessingLogElement, "LogLevel"));
        messageProcessingLog.setStatus(Utils.optString(messageProcessingLogElement, "Status"));
        messageProcessingLog.setApplicationMessageId(Utils.optString(messageProcessingLogElement, "ApplicationMessageId"));
        messageProcessingLog.setApplicationMessageType(Utils.optString(messageProcessingLogElement, "ApplicationMessageType"));
        messageProcessingLog.setSender(Utils.optString(messageProcessingLogElement, "Sender"));
        messageProcessingLog.setReceiver(Utils.optString(messageProcessingLogElement, "Receiver"));
        messageProcessingLog.setAlternateWebLink(Utils.optString(messageProcessingLogElement, "AlternateWebLink"));
        messageProcessingLog.setLogStart(CpiApiUtils.parseDate(Utils.optString(messageProcessingLogElement, "LogStart")));
        messageProcessingLog.setLogEnd(CpiApiUtils.parseDate(Utils.optString(messageProcessingLogElement, "LogEnd")));
        messageProcessingLog.setCustomHeaderProperties(CpiApiUtils.parseCustomerHeaderProperties(messageProcessingLogElement));
        return messageProcessingLog;
    }
}
