package com.figaf.integration.cpi.entity.partner_directory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Optional;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BinaryParameterUpdateRequest {

    @JsonIgnore
    private byte[] value;

    @JsonProperty("ContentType")
    private String contentType;

    @JsonIgnore
    private String base64FormatValue;

    @JsonProperty("Value")
    public String getValueAsBase64() {
        if (Optional.ofNullable(this.base64FormatValue).isPresent()) {
            return this.base64FormatValue;
        }
        return this.value == null ? null : Base64.getEncoder().encodeToString(this.value);
    }

    public BinaryParameterUpdateRequest(byte[] value, String contentType) {
        this.value = value;
        this.contentType = contentType;
    }

    public BinaryParameterUpdateRequest(String base64FormatValue, String contentType) {
        this.base64FormatValue = base64FormatValue;
        this.contentType = contentType;
    }
}
