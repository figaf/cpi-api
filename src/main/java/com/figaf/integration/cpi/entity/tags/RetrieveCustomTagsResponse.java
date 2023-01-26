package com.figaf.integration.cpi.entity.tags;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class RetrieveCustomTagsResponse {

    private List<CustomTagsConfiguration> customTagsConfiguration;
}
