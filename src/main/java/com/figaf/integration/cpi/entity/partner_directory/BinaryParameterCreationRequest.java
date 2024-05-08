package com.figaf.integration.cpi.entity.partner_directory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BinaryParameterCreationRequest {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Pid")
    private String pid;

    @JsonIgnore
    private byte[] value;

    @JsonIgnore
    private String base64FormatValue;

    @JsonProperty("ContentType")
    private String contentType;

    public BinaryParameterCreationRequest(String id, String pid, byte[] value, String contentType) {
        this.id = id;
        this.pid = pid;
        this.value = value;
        this.contentType = contentType;
    }

    public BinaryParameterCreationRequest(String id, String pid, String base64FormatValue, String contentType) {
        this.id = id;
        this.pid = pid;
        this.base64FormatValue = base64FormatValue;
        this.contentType = contentType;
    }

    @JsonProperty("Value")
    public String getValueAsBase64() {
        if (Optional.ofNullable(this.base64FormatValue).isPresent()) {
            return this.base64FormatValue;
        }
        return this.value == null ? null : Base64.getEncoder().encodeToString(this.value);
    }
}
