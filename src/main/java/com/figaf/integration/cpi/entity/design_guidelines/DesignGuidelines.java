package com.figaf.integration.cpi.entity.design_guidelines;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DesignGuidelines {

    private List<RuleResult> rulesResult;
}
