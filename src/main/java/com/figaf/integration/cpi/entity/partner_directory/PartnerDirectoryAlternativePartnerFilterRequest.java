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
public class PartnerDirectoryAlternativePartnerFilterRequest {

    private List<PartnerDirectoryAlternativePartnerKey> partnerDirectoryAlternativePartnerKeys;

    public PartnerDirectoryAlternativePartnerFilterRequest(List<PartnerDirectoryAlternativePartnerKey> partnerDirectoryAlternativePartnerKeys) {
        this.partnerDirectoryAlternativePartnerKeys = partnerDirectoryAlternativePartnerKeys;
    }

    public String createAlternativePartnerKeyFilter() {
        if (CollectionUtils.isEmpty(this.partnerDirectoryAlternativePartnerKeys)) {
            return StringUtils.EMPTY;
        }
        boolean containsInvalidParams = this.partnerDirectoryAlternativePartnerKeys
            .stream()
            .anyMatch(alternativePartnerKey -> StringUtils.isBlank(alternativePartnerKey.getAgency())
                                               || StringUtils.isBlank(alternativePartnerKey.getScheme())
                                               || StringUtils.isBlank(alternativePartnerKey.getId()));
        if (containsInvalidParams) {
            throw new IllegalArgumentException(String.format("alternativePartnerKeys %s contain empty agency, scheme or id.", this.partnerDirectoryAlternativePartnerKeys));
        }

        return this.partnerDirectoryAlternativePartnerKeys
            .stream()
            .map(key -> String.format("(Agency eq '%s' and Scheme eq '%s' and Id eq '%s')", key.getAgency(), key.getScheme(), key.getId()))
            .collect(Collectors.joining(" or ", "&$filter=", ""));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PartnerDirectoryAlternativePartnerKey {

        private String agency;
        private String scheme;
        private String id;
    }
}
