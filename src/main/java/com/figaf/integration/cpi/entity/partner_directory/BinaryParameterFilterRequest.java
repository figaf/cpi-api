package com.figaf.integration.cpi.entity.partner_directory;

import lombok.*;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BinaryParameterFilterRequest {

    private String pid;

    public String createFilter() {
        StringBuilder filterBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(this.pid)) {
            filterBuilder.append(String.format("&$filter=Pid eq '%s'", this.pid));
        }
        return filterBuilder.toString();
    }
}
