package com.figaf.integration.cpi.entity.monitoring;

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
@JacksonXmlRootElement(localName = "com.sap.it.op.srv.commands.dashboard.NodeProcessStatisticResponse")
public class NodeProcessStatisticCommandResponse {

    private NodeProcessStatisticCommandInfo info;
}
