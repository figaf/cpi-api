package com.figaf.integration.cpi.entity.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true, exclude = "secureParam")
public class SecureParameter extends SecurityContent {

    private String secureParam;
}
