package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationDesigntimeArtifact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@AllArgsConstructor
public class IntegrationContentWithErrorInformationWrapper {

    private IntegrationContent integrationContent;
    private IntegrationContentErrorInformation errorInformation;
    private IntegrationDesigntimeArtifact integrationDesigntimeArtifact;
}
