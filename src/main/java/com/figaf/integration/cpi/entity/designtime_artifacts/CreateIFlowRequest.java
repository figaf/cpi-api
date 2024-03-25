package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;

/**
 * @author Nesterov Ilya
 */
@ToString(callSuper = true)
public class CreateIFlowRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateIFlowRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName,
        byte[] bundledModel,
        String packageExternalId,
        String packageTechnicalName
    ) {
        super(id, name, description, additionalAttrs, fileName, bundledModel, packageExternalId, packageTechnicalName);
    }

    @Override
    public String getType() {
        return IFLOW.getQueryTitle();
    }

}
