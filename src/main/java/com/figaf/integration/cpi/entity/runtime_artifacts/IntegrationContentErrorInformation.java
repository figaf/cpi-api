package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
        if (this.errorMessage == null) {
            this.errorMessage = buildErrorMessage(this.childInstances, 0);
        }
        return this.errorMessage;
    }

    private String buildErrorMessage(List<IntegrationContentErrorInformation> childInstances, int numberOfWhitespaces) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(childInstances)) {
            for (IntegrationContentErrorInformation child : childInstances) {

                for (int i = 1; i <= numberOfWhitespaces; i++) {
                    stringBuilder.append(" ");
                }
                numberOfWhitespaces++;

                stringBuilder.append(String.format("[%s][%s][%s] : ", child.getMessage().getSubsystemName(), child.getMessage().getSubsystemPartName(), child.getMessage().getMessageId()));
                stringBuilder.append(MessageFormat.format(child.getMessage().getMessageText(), child.getParameter().toArray()));
                stringBuilder.append("\n");
                return stringBuilder.append(buildErrorMessage(child.getChildInstances(), numberOfWhitespaces)).toString();
            }
        }
        return "";
    }

}
