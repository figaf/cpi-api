package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

    //for cloud foundry only
    private String externalId;

    @JsonIgnore
    public IntegrationContent getSelf() {
        return this;
    }

    public static IntegrationContent searchByTechnicalName(List<IntegrationContent> artifacts, String technicalName) {
        for (IntegrationContent artifact : artifacts) {
            if (StringUtils.equals(artifact.getId(), technicalName)) {
                return artifact;
            }
        }

        return null;
    }
}
