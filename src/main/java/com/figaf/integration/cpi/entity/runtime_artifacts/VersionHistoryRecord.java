package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VersionHistoryRecord {

    private String comment;
    private String semanticVersion;
    private int technicalVersion;
    private long createdDate;
    private String createdBy;
    private String state;
}
