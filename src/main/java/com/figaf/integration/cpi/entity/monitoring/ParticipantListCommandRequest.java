package com.figaf.integration.cpi.entity.monitoring;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "root")
public class ParticipantListCommandRequest {

    @JacksonXmlProperty(isAttribute = true)
    private boolean withActiveTenants = false;
    private boolean onlyHeader = false;
    private boolean withAdminNodes = true;
    private boolean withNodes = true;
}
