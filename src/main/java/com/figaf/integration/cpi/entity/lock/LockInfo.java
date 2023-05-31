package com.figaf.integration.cpi.entity.lock;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LockInfo {

    private boolean isCurrentSessionHasLock;
    private boolean isCurrentUserHasLock;
    private boolean isLocked;
    private boolean isPublishToCatalogAllowed;
    private boolean isResourceEditOpted;
    private String lockedBy;
    private String lockedTime;
}
