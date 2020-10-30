package com.figaf.integration.cpi.entity.monitoring;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class ParticipantListCommandInformation {

    private String id;
    private String name;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ParticipantListCommandNodes> nodes;

}
