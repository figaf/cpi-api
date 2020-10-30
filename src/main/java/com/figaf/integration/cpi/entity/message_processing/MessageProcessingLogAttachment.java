package com.figaf.integration.cpi.entity.message_processing;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
public class MessageProcessingLogAttachment {

    private String id;
    private String messageGuid;
    private Date date;
    private String name;
    private String contentType;
    private Integer payloadSize;

    private MessageProcessingLogAttachmentType attachmentType = MessageProcessingLogAttachmentType.LOGGED;

}
