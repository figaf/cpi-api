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
public class CreateScriptCollectionRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateScriptCollectionRequest(
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
        return SCRIPT_COLLECTION.getQueryTitle();
    }

}
