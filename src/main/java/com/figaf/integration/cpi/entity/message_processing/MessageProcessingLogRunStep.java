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

    private List<MessageRunStepProperty> runStepProperties;
    private List<TraceMessage> traceMessages;

    private boolean relatesToFirstMpl;

    public boolean matches(MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria) {
        return !runStepSearchCriteria.getModelStepsToIgnore().contains(modelStepId);
    }

    public List<MessageRunStepProperty> getRunStepProperties() {
        if (runStepProperties == null) {
            runStepProperties = new ArrayList<>();
        }
        return runStepProperties;
    }

    public List<TraceMessage> getTraceMessages() {
        if (traceMessages == null) {
            traceMessages = new ArrayList<>();
        }
        return traceMessages;
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
