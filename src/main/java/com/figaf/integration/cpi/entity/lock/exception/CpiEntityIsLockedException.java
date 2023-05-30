package com.figaf.integration.cpi.entity.lock.exception;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.lock.LockInfo;
import lombok.Getter;

@Getter
public class CpiEntityIsLockedException extends ClientIntegrationException {

    private final LockInfo lockInfo;
    private final String artifactExternalId;

    public CpiEntityIsLockedException(LockInfo lockInfo, String artifactExternalId) {
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

    public CpiEntityIsLockedException(String message, LockInfo lockInfo, String artifactExternalId) {
        super(message);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

    public CpiEntityIsLockedException(String message, String possibleSolution, LockInfo lockInfo, String artifactExternalId) {
        super(message, possibleSolution);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

    public CpiEntityIsLockedException(String message, Object additionalData, LockInfo lockInfo, String artifactExternalId) {
        super(message, additionalData);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

    public CpiEntityIsLockedException(Throwable cause, LockInfo lockInfo, String artifactExternalId) {
        super(cause);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

    public CpiEntityIsLockedException(String message, Throwable cause, LockInfo lockInfo, String artifactExternalId) {
        super(message, cause);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
    }

}
