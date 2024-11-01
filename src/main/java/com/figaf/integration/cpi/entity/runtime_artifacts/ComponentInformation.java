package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Nesterov
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ComponentInformation {

    private String hostname;
    private String nodeId;
    private String state;

    @JacksonXmlElementWrapper(localName = "message")
    @JacksonXmlProperty(localName = "message")
    private List<MessageDetail> messageDetails;

    public List<MessageDetail> getMessageDetails() {
        if (messageDetails == null) {
            messageDetails = new ArrayList<>();
        }
        return messageDetails;
    }

    public MessageDetail getFirstMessageDetail() {
        if (getMessageDetails().isEmpty()) {
            return null;
        }
        return getMessageDetails().get(0);
    }
}
