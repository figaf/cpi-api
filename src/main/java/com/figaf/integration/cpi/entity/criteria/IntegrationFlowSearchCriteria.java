package com.figaf.integration.cpi.entity.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
public class IntegrationFlowSearchCriteria {

    private String agentId;
    private Boolean deployed;
}
