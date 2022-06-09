package com.figaf.integration.cpi.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class DeleteAndUndeployIFlowResult {

    private final String iflowName;
    private boolean deleted;
    private boolean undeployed;

    public DeleteAndUndeployIFlowResult(String iflowName) {
        this.iflowName = iflowName;
    }
}
