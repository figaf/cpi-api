package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.SHARED_MESSAGE_MAPPING;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSharedMessageMappingRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateSharedMessageMappingRequest(
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
        return SHARED_MESSAGE_MAPPING.getQueryTitle();
    }

}
