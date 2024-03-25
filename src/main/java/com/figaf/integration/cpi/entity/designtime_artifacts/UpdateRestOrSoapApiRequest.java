package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.REST_API;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateRestOrSoapApiRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public UpdateRestOrSoapApiRequest(
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
        super(id, name, description, additionalAttrs, fileName, bundledModel, packageExternalId, packageTechnicalName, uploadDraftVersion, newArtifactVersion, comment);
    }

    @Override
    public String getType() {
        return REST_API.getQueryTitle();
    }

}
