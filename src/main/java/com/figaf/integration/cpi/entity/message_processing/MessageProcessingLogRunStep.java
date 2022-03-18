package com.figaf.integration.cpi.entity.message_processing;

import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
public class MessageProcessingLogRunStep {

    private String runId;
    private int childCount;
    private Date stepStart;
    private Date stepStop;
    private String stepId;
    private String modelStepId;
    private String branchId;
    private String status;
    private String error;
    private String activity;

    //for now, we support only a single traceId per runStep
    private String traceId;
    private List<MessageRunStepProperty> runStepProperties;

    private boolean relatesToFirstMpl;

    public List<MessageRunStepProperty> getRunStepProperties() {
        if (runStepProperties == null) {
            runStepProperties = new ArrayList<>();
        }
        return runStepProperties;
    }

    @Getter
    @Setter
    public static class TraceMessage {
        private String traceId;
        private String messageId;
        private String modelStepId;
        private long payloadSize;
        private String mimeType;
        private Date processingDate;
        private byte[] payload;
        private List<MessageRunStepProperty> properties;

        public List<MessageRunStepProperty> getProperties() {
            if (properties == null) {
                properties = new ArrayList<>();
            }
            return properties;
        }
    }
}
