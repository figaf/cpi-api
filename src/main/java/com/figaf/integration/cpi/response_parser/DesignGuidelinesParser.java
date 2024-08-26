package com.figaf.integration.cpi.response_parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.cpi.entity.design_guidelines.DesignGuidelines;
import com.figaf.integration.cpi.entity.design_guidelines.constants.DesignGuidelinesConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class DesignGuidelinesParser {

    protected final ObjectMapper jsonMapper;

    public DesignGuidelinesParser() {
        this.jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public DesignGuidelines parseDesignGuidelinesFromJsonString(String rawDesignGuidelines) throws IOException {
        DesignGuidelines designGuidelines = jsonMapper.readValue(rawDesignGuidelines, DesignGuidelines.class);
        designGuidelines.getRulesResult()
            .forEach(designGuideline -> {
                designGuideline.setRulesetIdLabel(DesignGuidelinesConstants.RULE_LABELS.get(designGuideline.getRulesetId()));
                designGuideline.setRuleIdLabel(DesignGuidelinesConstants.RULE_LABELS.get(designGuideline.getRuleId()));
            });
        return designGuidelines;
    }
}