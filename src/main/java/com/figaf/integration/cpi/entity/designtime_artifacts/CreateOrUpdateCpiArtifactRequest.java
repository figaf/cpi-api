package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public abstract class CreateOrUpdateCpiArtifactRequest {

    private String id;
    private String name;
    private String description;
    private AdditionalAttributes additionalAttrs;
    private String fileName;

    public abstract String getType();

    public AdditionalAttributes getAdditionalAttrs() {
        if (additionalAttrs == null) {
            additionalAttrs = new AdditionalAttributes();
        }
        return additionalAttrs;
    }

    @Getter
    @ToString
    public static class AdditionalAttributes {

        private final List<String> source = new ArrayList<>();
        private final List<String> target = new ArrayList<>();
    }
}
