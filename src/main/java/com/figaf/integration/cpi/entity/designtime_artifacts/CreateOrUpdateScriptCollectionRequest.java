package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.SCRIPT_COLLECTION;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString
public class CreateOrUpdateScriptCollectionRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateOrUpdateScriptCollectionRequest(
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
        return SCRIPT_COLLECTION.getQueryTitle();
    }

}
