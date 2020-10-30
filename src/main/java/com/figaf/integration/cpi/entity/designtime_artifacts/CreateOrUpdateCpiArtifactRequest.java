package com.figaf.integration.cpi.entity.designtime_artifacts;

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
public abstract class CreateOrUpdateCpiArtifactRequest {

    private String id;
    private String name;
    private String description;
    private String type;
    private AdditionalAttributes additionalAttrs = new AdditionalAttributes();
    private String fileName;

    @Getter
    @ToString
    public static class AdditionalAttributes {

        private List<String> source = new ArrayList<>();
        private List<String> target = new ArrayList<>();
    }
}
