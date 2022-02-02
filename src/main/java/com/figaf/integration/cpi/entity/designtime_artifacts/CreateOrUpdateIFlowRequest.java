package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;

/**
 * @author Nesterov Ilya
 */
@ToString
public class CreateOrUpdateIFlowRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateOrUpdateIFlowRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName
    ) {
        super(id, name, description, additionalAttrs, fileName);
    }

    @Override
    public String getType() {
        return IFLOW.getQueryTitle();
    }

}
