package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;

/**
 * @author Nesterov Ilya
 */
@ToString
public class CreateIFlowRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateIFlowRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName,
        byte[] bundledModel,
        String packageExternalId
    ) {
        super(id, name, description, additionalAttrs, fileName, bundledModel, packageExternalId);
    }

    @Override
    public String getType() {
        return IFLOW.getQueryTitle();
    }

}
