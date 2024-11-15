package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
@ToString(of = {"name", "type", "version", "status"})
public class IntegrationContent implements Serializable {

    private String id;
    private String version;
    private String name;
    private String type;
    private String deployedBy;
    private Date deployedOn;
    private String status;
    private String runtimeLocationId;

    //for cloud foundry only
    private String externalId;

    // initialized only when fetched by Web API
    private String tenantId;

    private IntegrationContentErrorInformation errorInformation;

    private LogConfiguration logConfiguration;

    @JsonIgnore
    public IntegrationContent getSelf() {
        return this;
    }

    public static IntegrationContent searchByTechnicalName(
        List<IntegrationContent> artifacts,
        String technicalName,
        String runtimeLocationId
    ) {
        return artifacts
            .stream()
            .filter(artifact -> artifact.getId().equals(technicalName) &&
                Objects.equals(artifact.getRuntimeLocationId(), runtimeLocationId))
            .findFirst()
            .orElse(null);
    }
}
