package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.AdditionalPayloadType;
import com.figaf.integration.cpi.entity.message_processing.CustomHeaderProperty;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLog;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogAttachment;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRun;
import com.figaf.integration.cpi.response_parser.MessageProcessingLogParser;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kostas Charalambous
 */
public abstract class AbstractMessageProcessingLogClient extends CpiBaseClient {

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

    protected List<MessageProcessingLogAttachment> createMessageProcessingLogAttachmentsForAttachments(JSONArray attachmentsJsonArray) {
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

    protected List<MessageProcessingLogAttachment> createMessageProcessingLogAttachmentsForPayloads(JSONArray attachmentsJsonArray) {
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

    protected List<MessageProcessingLogRun> createMessageProcessingLogAttachmentsForRuns(JSONArray runsJsonArray, String messageGuid) {
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

    protected List<CustomHeaderProperty> createCustomHeaderProperties(JSONArray jsonArray) {
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

    protected Pair<List<MessageProcessingLog>, Integer> extractMplsAndCountFromResponse(JSONObject jsonObjectD) {
        JSONArray messageProcessingLogsJsonArray = jsonObjectD.getJSONArray("results");
        List<MessageProcessingLog> messageProcessingLogs = new ArrayList<>();
        int sizeOfMessageProcessingLogs = messageProcessingLogsJsonArray.length();
        for (int ind = 0; ind < sizeOfMessageProcessingLogs; ind++) {
            JSONObject messageProcessingLogElement = messageProcessingLogsJsonArray.getJSONObject(ind);

            MessageProcessingLog messageProcessingLog = MessageProcessingLogParser.fillMessageProcessingLog(messageProcessingLogElement);
            messageProcessingLogs.add(messageProcessingLog);
        }

        return new MutablePair<>(messageProcessingLogs, sizeOfMessageProcessingLogs);
    }
}
