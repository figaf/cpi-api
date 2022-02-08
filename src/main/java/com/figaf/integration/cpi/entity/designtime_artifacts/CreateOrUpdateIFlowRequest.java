package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
@ToString
public class CreateOrUpdateIFlowRequest extends CreateOrUpdateCpiArtifactRequest {

    @Override
    public String getType() {
        return "IFlow";
    }

}
