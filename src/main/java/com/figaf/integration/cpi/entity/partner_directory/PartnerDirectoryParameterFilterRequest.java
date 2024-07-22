package com.figaf.integration.cpi.entity.partner_directory;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class PartnerDirectoryParameterFilterRequest {

    public PartnerDirectoryParameterFilterRequest(String pid) {
        this.pid = pid;
    }

    public PartnerDirectoryParameterFilterRequest(List<PartnerDirectoryParameterKey> partnerDirectoryParameterKeys) {
        this.partnerDirectoryParameterKeys = partnerDirectoryParameterKeys;
    }

    private String pid;

    private List<PartnerDirectoryParameterKey> partnerDirectoryParameterKeys;

    public String createFilter() {
        String filter = createPidFilter();
        if (StringUtils.isNotBlank(filter)) {
            return filter;
        }
        return createBinaryParameterKeyFilter();
    }

    private String createPidFilter() {
        StringBuilder filterBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(this.pid)) {
            filterBuilder.append(String.format("&$filter=Pid eq '%s'", this.pid));
        }
        return filterBuilder.toString();
    }

    private String createBinaryParameterKeyFilter() {
        if (CollectionUtils.isEmpty(this.partnerDirectoryParameterKeys)) {
            return StringUtils.EMPTY;
        }
        boolean containsInvalidParams = this.partnerDirectoryParameterKeys
            .stream()
            .anyMatch(partnerDirectoryParameterKey -> StringUtils.isBlank(partnerDirectoryParameterKey.getParamId()) || StringUtils.isBlank(partnerDirectoryParameterKey.getPid()));
        if (containsInvalidParams) {
            throw new IllegalArgumentException(String.format("binaryParameterKeys %s contains empty paramId or pid.", this.partnerDirectoryParameterKeys));
        }

        return this.partnerDirectoryParameterKeys
            .stream()
            .map(key -> String.format("(Pid eq '%s' and Id eq '%s')", key.getPid(), key.getParamId()))
            .collect(Collectors.joining(" or ", "&$filter=", ""));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PartnerDirectoryParameterKey {

        private String pid;
        private String paramId;
    }
}
