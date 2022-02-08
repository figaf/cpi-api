package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString
public class CreateOrUpdateValueMappingRequest extends CreateOrUpdateCpiArtifactRequest {

    @Override
    public String getType() {
        return "ValueMapping";
    }

}
