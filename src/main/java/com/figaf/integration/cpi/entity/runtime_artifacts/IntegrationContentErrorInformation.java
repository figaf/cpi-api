package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class IntegrationContentErrorInformation {

    @Setter
    @ToString
    public static class IntegrationContentErrorInformationMessage {

        private String subsystemName;
        private String subsytemPartName;
        private String subsystemPartName;
        private String messageId;
        private String messageText;

        public String getSubsystemName() {
            return subsystemName;
        }

        public String getSubsystemPartName() {
            return subsystemPartName != null ? subsystemPartName : subsytemPartName;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getMessageText() {
            return messageText;
        }
    }

    private IntegrationContentErrorInformationMessage message;
    private List<String> parameter = new ArrayList<>();
    private List<IntegrationContentErrorInformation> childInstances;

    private String errorMessage;

    public String getErrorMessage() {
        if (StringUtils.isBlank(this.errorMessage)) {
            this.errorMessage = buildErrorMessageFromChildInstances(this.childInstances, 0);
        }
        if (StringUtils.isBlank(this.errorMessage)) {
            this.errorMessage = buildErrorMessageFromParameter();
        }
        return this.errorMessage;
    }

    private String buildErrorMessageFromChildInstances(List<IntegrationContentErrorInformation> childInstances, int numberOfWhitespaces) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(childInstances)) {
            for (IntegrationContentErrorInformation child : childInstances) {
                for (int i = 1; i <= numberOfWhitespaces; i++) {
                    stringBuilder.append(" ");
                }
                numberOfWhitespaces++;

                stringBuilder.append(buildErrorMessageLabel(child.getMessage()));
                stringBuilder.append(MessageFormat.format(child.getMessage().getMessageText(), child.getParameter().toArray()));
                stringBuilder.append("\n");
                stringBuilder.append(buildErrorMessageFromChildInstances(child.getChildInstances(), numberOfWhitespaces));
            }
            return stringBuilder.toString();
        }
        return "";
    }

    private String buildErrorMessageFromParameter() {
        if (getParameter().isEmpty()) {
            return "";
        }
        String messageTemplate = this.message == null || StringUtils.isBlank(this.message.getMessageText())
            ? "{0}"
            : this.message.getMessageText();
        return buildErrorMessageLabel(this.message) + MessageFormat.format(messageTemplate, getParameter().toArray());
    }

    private String buildErrorMessageLabel(IntegrationContentErrorInformationMessage message) {
        if (message == null) {
            return "";
        }
        return format("[%s][%s][%s] : ",
            message.getSubsystemName(),
            message.getSubsystemPartName(),
            message.getMessageId()
        );
    }

}
