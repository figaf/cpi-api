package com.figaf.integration.cpi.entity.message_processing;

import com.figaf.integration.cpi.entity.AdditionalPayloadEntry;
import com.figaf.integration.cpi.entity.AdditionalPayloadType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
public class MessageProcessingLogAttachment implements AdditionalPayloadEntry {

    private String id;
    private String messageGuid;
    private Date date;
    private String name;
    private String contentType;
    private Integer payloadSize;

    //only for PERSISTED attachmentType
    private String messageStoreId;

    private AdditionalPayloadType attachmentType = AdditionalPayloadType.MPL_ATTACHMENT;

    @Override
    public String getUniqueId() {
        return id;
    }

    @Override
    public String getMessageId() {
        return messageGuid;
    }

    @Override
    public String getModelStepId() {
        return AdditionalPayloadType.PERSISTED_MESSAGE.equals(attachmentType) ? messageStoreId : name;
    }

    @Override
    public String getActivity() {
        return attachmentType.toString().toLowerCase();
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public AdditionalPayloadType getPayloadType() {
        return attachmentType;
    }

}
