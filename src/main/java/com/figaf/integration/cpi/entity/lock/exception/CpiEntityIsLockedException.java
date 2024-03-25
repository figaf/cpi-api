package com.figaf.integration.cpi.entity.lock.exception;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.lock.LockInfo;
import lombok.Getter;

@Getter
public class CpiEntityIsLockedException extends ClientIntegrationException {

    private final LockInfo lockInfo;
    private final String artifactExternalId;
    private final boolean isPackageLocked;

    public CpiEntityIsLockedException(LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

    public CpiEntityIsLockedException(String message, LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        super(message);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

    public CpiEntityIsLockedException(String message, String possibleSolution, LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        super(message, possibleSolution);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

    public CpiEntityIsLockedException(String message, Object additionalData, LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        super(message, additionalData);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

    public CpiEntityIsLockedException(Throwable cause, LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        super(cause);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

    public CpiEntityIsLockedException(String message, Throwable cause, LockInfo lockInfo, String artifactExternalId, boolean isPackageLocked) {
        super(message, cause);
        this.lockInfo = lockInfo;
        this.artifactExternalId = artifactExternalId;
        this.isPackageLocked = isPackageLocked;
    }

}
