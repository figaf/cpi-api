package com.figaf.integration.cpi.entity.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class KeystoreEntry extends SecurityContent {

    private String hexalias;
    private String alias;
    private String type;
}
