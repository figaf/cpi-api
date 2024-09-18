package com.figaf.integration.cpi.entity.monitoring;

import lombok.*;

import java.util.List;

@Getter

@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RuntimeLocationsResponse {

    private List<RuntimeLocation> runtimeLocations;
}
