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
@ToString
public class CreateOrUpdateRestApiRequest extends CreateOrUpdateCpiArtifactRequest {

    @Builder
    public CreateOrUpdateRestApiRequest(
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
        return REST_API.getQueryTitle();
    }

}
