package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.MESSAGE_MAPPING;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateMessageMappingRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateMessageMappingRequest(
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
        return MESSAGE_MAPPING.getQueryTitle();
    }

}
