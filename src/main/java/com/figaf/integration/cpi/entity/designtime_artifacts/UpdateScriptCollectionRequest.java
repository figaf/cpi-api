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
public class UpdateScriptCollectionRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateScriptCollectionRequest(
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
        return SCRIPT_COLLECTION.getQueryTitle();
    }

}
