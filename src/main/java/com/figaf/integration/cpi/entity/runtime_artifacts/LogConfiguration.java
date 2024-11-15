package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LogConfiguration {

    private boolean debugActive;
    private String logLevel;
    private String replicationLogLevel;
    private boolean traceActive;
    private boolean traceEnabled;
}
