package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.FUNCTION_LIBRARIES;

@Getter
@Setter
@ToString(callSuper = true)
public class CreateFunctionLibrariesRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateFunctionLibrariesRequest(
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
        return FUNCTION_LIBRARIES.getQueryTitle();
    }

}
