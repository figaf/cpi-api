package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Builder;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Builder
@ToString
public class RuntimeArtifactIdentifier {

    private final String technicalName;

    private final String runtimeArtifactId;

    /**
     * @return identificationParameter - if runtimeLocationId is defined it should be runtime artifact id,
     * otherwise - technical name
     */
    public Optional<String> getIdentificationParameter(String runtimeLocationId) {
        return StringUtils.isNoneBlank(runtimeLocationId, this.runtimeArtifactId)
                ? Optional.of(this.runtimeArtifactId)
                : Optional.ofNullable(this.technicalName).filter(StringUtils::isNotBlank);
    }
}
