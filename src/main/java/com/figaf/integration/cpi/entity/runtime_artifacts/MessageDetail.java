package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Ilya Nesterov
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MessageDetail {

    @JacksonXmlProperty(localName = "subsystemName", isAttribute = true)
    private String subsystemName;
    @JacksonXmlProperty(localName = "subsystemPartName", isAttribute = true)
    private String subsystemPartName;
    @JacksonXmlProperty(localName = "messageId", isAttribute = true)
    private String messageId;
    @JacksonXmlProperty(localName = "messageText", isAttribute = true)
    private String messageText;
}
