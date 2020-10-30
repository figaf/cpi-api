package com.figaf.integration.cpi.entity.message_processing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class MessageProcessingLogErrorInformation {

    private String lastErrorModelStepId;
    private String errorMessage;
}
