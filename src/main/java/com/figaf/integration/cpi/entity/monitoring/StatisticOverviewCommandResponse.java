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
@JacksonXmlRootElement(localName = "com.sap.it.op.tmn.commands.dashboard.webui.StatisticOverviewCommand")
public class StatisticOverviewCommandResponse {

    private int certificateUserMappingsNumber;
    private int expiredLocksNumber;
    private int jdbcDataSourceNumber;
    private int keystoreEntriesNumber;
    private int variablesNumber;
}
