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
@ToString(exclude = "bundledModel")
public abstract class CreateOrUpdateCpiArtifactRequest {

    private String id;
    private String name;
    private String description;
    private AdditionalAttributes additionalAttrs;
    private String fileName;

    //---------------- Additional parameters -----------------
    private byte[] bundledModel;
    private String packageExternalId;

    //only for update
    private boolean uploadDraftVersion;
    private String newArtifactVersion;
    private String comment;

    private CreateOrUpdateCpiArtifactRequest() {
    }

    protected CreateOrUpdateCpiArtifactRequest(
        String id,
        String name,
        String description,
        AdditionalAttributes additionalAttrs,
        String fileName,
        byte[] bundledModel,
        String packageExternalId
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.additionalAttrs = additionalAttrs;
        this.fileName = fileName;

        this.bundledModel = bundledModel;
        this.packageExternalId = packageExternalId;
    }

    protected CreateOrUpdateCpiArtifactRequest(
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
        this.id = id;
        this.name = name;
        this.description = description;
        this.additionalAttrs = additionalAttrs;
        this.fileName = fileName;

        this.bundledModel = bundledModel;
        this.packageExternalId = packageExternalId;

        this.uploadDraftVersion = uploadDraftVersion;
        this.newArtifactVersion = newArtifactVersion;
        this.comment = comment;
    }

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
