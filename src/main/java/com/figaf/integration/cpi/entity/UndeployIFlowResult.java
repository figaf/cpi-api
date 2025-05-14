package com.figaf.integration.cpi.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Ilya Nesterov
 */
@Getter
@Setter
@ToString
public class UndeployIFlowResult {

    private final String technicalName;
    private boolean skippedBecauseNotDeployed;
    private boolean undeploymentTriggeredSuccessfully;

    public UndeployIFlowResult(String technicalName) {
        this.technicalName = technicalName;
    }
}
