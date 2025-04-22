package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.cpi.entity.designtime_artifacts.AdditionalAttributes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ilya Nesterov
 */
class CpiRuntimeArtifactParserTest {

    @Test
    void test_retrieveAdditionalAttributes() {
        String body = """
        {
            "id" : "b1eb73c8cead480382630fa5ec6d9f54",
            "name" : "SimpleFlowWithSenderReceiver",
            "tooltip" : "SimpleFlowWithSenderReceiver",
            "description" : "test",
            "type" : "IFlow",
            "entityID" : "b1eb73c8cead480382630fa5ec6d9f54",
            "additionalAttrs" : {
                "OriginBundleSymbolicName" : [ "SimpleFlowWithSenderReceiver" ],
                "source" : [ "Amazon", "test" ],
                "nodeType" : [ "IFLMAP" ],
                "OriginBundleName" : [ "SimpleFlowWithSenderReceiver" ],
                "productProfile" : [ "iflmap" ],
                "target" : [ "Cisco", "ChatGPT", "test" ]
            },
            "semanticVersion" : "1.0.1",
            "modifiedAt" : 1744966595767,
            "privilegeState" : "EDIT_ALLOWED",
            "modifiedBy" : "iln@figaf.com",
            "createdAt" : 1744964442527,
            "createdBy" : "iln@figaf.com",
            "references" : [ ]
        }
        """;

        AdditionalAttributes additionalAttributes = CpiRuntimeArtifactParser.retrieveAdditionalAttributes(body);

        assertThat(additionalAttributes).isNotNull();
        assertThat(additionalAttributes.getSource())
            .isNotEmpty()
            .containsExactly("Amazon", "test");
        assertThat(additionalAttributes.getTarget())
            .isNotEmpty()
            .containsExactly("Cisco", "ChatGPT", "test");
    }
}
