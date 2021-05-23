package com.figaf.integration.cpi.entity.message_sender;

import lombok.*;

import static com.figaf.integration.cpi.entity.message_sender.AuthenticationType.BASIC;
import static com.figaf.integration.cpi.entity.message_sender.AuthenticationType.OAUTH;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.Assert.isTrue;

/**
 * @author Klochkov Sergey
 */
@Getter
@ToString
public class MessageSendingAdditionalProperties {

    private final AuthenticationType authenticationType;
    private final String oauthUrl;
    private final String restTemplateWrapperKey;
    private final boolean csrfProtected;

    private MessageSendingAdditionalProperties(
        AuthenticationType authenticationType,
        String oauthUrl,
        String restTemplateWrapperKey,
        boolean csrfProtected
    ) {
        this.authenticationType = authenticationType;
        this.oauthUrl = oauthUrl;
        this.restTemplateWrapperKey = restTemplateWrapperKey;
        this.csrfProtected = csrfProtected;
    }

    public static MessageSendingAdditionalProperties basicAuthorization(
        String restTemplateWrapperKey,
        boolean csrfProtected
    ) {
        isTrue(isNotBlank(restTemplateWrapperKey), "Rest template wrapper key must be not empty!");
        return new MessageSendingAdditionalProperties(BASIC, null, restTemplateWrapperKey, csrfProtected);
    }

    public static MessageSendingAdditionalProperties oauth(
        String oauthUrl,
        String restTemplateWrapperKey,
        boolean csrfProtected
    ) {
        isTrue(isNotBlank(oauthUrl), "Oauth url must be not empty!");
        isTrue(isNotBlank(restTemplateWrapperKey), "Rest template wrapper key must be not empty!");
        return new MessageSendingAdditionalProperties(OAUTH, oauthUrl, restTemplateWrapperKey, csrfProtected);
    }
}
