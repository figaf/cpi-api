package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IMPORTED_ARCHIVES;

@Getter
@Setter
@ToString(callSuper = true)
public class UpdateImportedArchivesRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateImportedArchivesRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName,
        byte[] bundledModel,
        String packageExternalId,
        String packageTechnicalName,
        boolean uploadDraftVersion,
        String newArtifactVersion,
        String comment
    ) {
        super(
            id,
            name,
            description,
            additionalAttrs,
            fileName,
            bundledModel,
            packageExternalId,
            packageTechnicalName,
            uploadDraftVersion,
            newArtifactVersion,
            comment
        );
    }

    @Override
    public String getType() {
        return IMPORTED_ARCHIVES.getQueryTitle();
    }

}
