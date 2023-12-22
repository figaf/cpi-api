package com.figaf.integration.cpi.entity.message_processing;

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
public class MessageProcessingLogRun {

    private String id;
    private Date runStart;
    private Date runStop;
    private String logLevel;
    private String overallState;
    private String processId;
    private String messageProcessingLogId;

    private List<MessageProcessingLogRunStep> runSteps;

    public List<MessageProcessingLogRunStep> getRunSteps() {
        if (runSteps == null) {
            runSteps = new ArrayList<>();
        }
        return runSteps;
    }
}
