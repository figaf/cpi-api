package com.figaf.integration.cpi.entity.design_guidelines;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RuleResult {

    private String status;
    private Boolean isRuleSkipped;
    private String ruleSkippedBy;
    private String ruleSkipComment;
    private String rulesetId;
    private String rulesetIdLabel;
    private String ruleId;
    private String ruleIdLabel;
    private String severity;
    private String guidelineMessage;
}
