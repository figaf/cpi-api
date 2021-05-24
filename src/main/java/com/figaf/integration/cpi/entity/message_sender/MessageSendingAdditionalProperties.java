package com.figaf.integration.cpi.entity.message_sender;

import lombok.*;

import static com.figaf.integration.cpi.entity.message_sender.AuthenticationType.BASIC;
import static com.figaf.integration.cpi.entity.message_sender.AuthenticationType.OAUTH;

/**
 * @author Klochkov Sergey
 */
@Getter
@ToString
public class MessageSendingAdditionalProperties {

    private final AuthenticationType authenticationType;
    private final String oauthUrl;
    private final String restTemplateWrapperKey;

    private MessageSendingAdditionalProperties(AuthenticationType authenticationType, String oauthUrl, String restTemplateWrapperKey) {
        this.authenticationType = authenticationType;
        this.oauthUrl = oauthUrl;
        this.restTemplateWrapperKey = restTemplateWrapperKey;
    }

    public static MessageSendingAdditionalProperties basicAuthorization() {
        return new MessageSendingAdditionalProperties(BASIC, null, null);
    }

    public static MessageSendingAdditionalProperties oauth(String oauthUrl, String restTemplateWrapperKey) {
        return new MessageSendingAdditionalProperties(OAUTH, oauthUrl, restTemplateWrapperKey);
    }
}
