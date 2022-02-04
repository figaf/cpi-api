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
@ToString
public class CreateOrUpdateSharedMessageMappingRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateOrUpdateSharedMessageMappingRequest(
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
        return SHARED_MESSAGE_MAPPING.getQueryTitle();
    }

}
