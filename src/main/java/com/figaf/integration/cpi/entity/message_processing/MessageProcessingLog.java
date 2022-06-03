package com.figaf.integration.cpi.entity.message_processing;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Nesterov Ilya
 */

@EqualsAndHashCode(of = "messageGuid")
@Getter
@Setter
@ToString(exclude = "runs")
public class MessageProcessingLog implements Serializable {

    private String messageGuid;
    private String correlationId;
    private String integrationFlowName;
    private String status;
    private String logLevel;
    private Date logStart;
    private Date logEnd;
    private String applicationMessageId;
    private String applicationMessageType;
    private String sender;
    private String receiver;
    private String alternateWebLink;

    private List<MessageProcessingLogRun> runs;
}
