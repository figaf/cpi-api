    package com.figaf.integration.cpi.entity.criteria;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nesterov Ilya
 */

@Getter
@Setter
public class MessageProcessingLogRunStepSearchCriteria {

    private List<String> getModelStepsToIgnore;

    private boolean initTraceMessagePayload;
    private boolean initTraceMessageProperties;

    public List<String> getModelStepsToIgnore() {
        if (getModelStepsToIgnore == null) {
            getModelStepsToIgnore = new ArrayList<>();
        }
        return getModelStepsToIgnore;
    }
}
