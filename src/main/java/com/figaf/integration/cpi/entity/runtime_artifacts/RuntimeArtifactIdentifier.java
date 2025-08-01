package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.figaf.integration.common.entity.RequestContext;
import lombok.Builder;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Builder
@ToString
public class RuntimeArtifactIdentifier {

    private final String technicalName;

    private final String runtimeArtifactId;

    //ChatGPT-generated javadoc

    /**
     * Retrieves the identification parameter based on the provided {@link RequestContext}.
     * <p>
     * The method determines whether to use the {@code technicalName} or {@code runtimeArtifactId}
     * as the identifier, depending on whether the runtime is default (evaluated by
     * {@link RequestContext#isDefaultRuntime()}). It ensures the selected parameter is not blank,
     * throwing an exception otherwise.
     * </p>
     *
     * @param requestContext the {@link RequestContext} providing context for the request
     * @return the identification parameter, which is either {@code technicalName} or
     * {@code runtimeArtifactId}, based on the runtime type
     * @throws RuntimeException if no appropriate identifier is found, or the chosen parameter is blank
     */
    public String getIdentificationParameter(RequestContext requestContext) {
        return Optional.ofNullable(requestContext.isDefaultRuntime() ? technicalName : runtimeArtifactId)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new RuntimeException(String.format("couldn't find appropriate identifier for %s", this)));
    }
}
