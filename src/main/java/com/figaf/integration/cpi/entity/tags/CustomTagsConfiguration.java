package com.figaf.integration.cpi.entity.tags;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class CustomTagsConfiguration {

    private String tagName;
    private boolean isMandatory;
}
