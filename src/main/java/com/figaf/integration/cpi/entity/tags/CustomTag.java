package com.figaf.integration.cpi.entity.tags;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomTag implements Serializable {

    private String name;
    private String value;
}
