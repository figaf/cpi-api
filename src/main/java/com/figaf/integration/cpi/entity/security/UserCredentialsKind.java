package com.figaf.integration.cpi.entity.security;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserCredentialsKind {

    USER_CREDENTIALS("default"),
    OPEN_CONNECTORS("openconnectors"),
    SUCCESS_FACTORS("successfactors");

    private String apiValue;

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    UserCredentialsKind(String apiValue) {
        this.apiValue = apiValue;
    }

    public static UserCredentialsKind getByApiValue(String apiValue) {
        for (UserCredentialsKind userCredentialsKind : UserCredentialsKind.values()) {
            if (userCredentialsKind.getApiValue().equals(apiValue)) {
                return userCredentialsKind;
            }
        }
        return null;
    }
}
