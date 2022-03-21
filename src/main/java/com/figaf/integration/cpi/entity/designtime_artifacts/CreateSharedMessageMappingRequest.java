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
public class CreateSharedMessageMappingRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateSharedMessageMappingRequest(
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
        return SHARED_MESSAGE_MAPPING.getQueryTitle();
    }

}
