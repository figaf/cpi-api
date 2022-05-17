package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;

/**
 * @author Nesterov Ilya
 */
@ToString(callSuper = true)
public class UpdateIFlowRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateIFlowRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName,
        byte[] bundledModel,
        String packageExternalId,
        boolean uploadDraftVersion,
        String newArtifactVersion,
        String comment
    ) {
        super(id, name, description, additionalAttrs, fileName, bundledModel, packageExternalId, uploadDraftVersion, newArtifactVersion, comment);
    }

    @Override
    public String getType() {
        return IFLOW.getQueryTitle();
    }

}
