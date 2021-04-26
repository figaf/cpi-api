package com.figaf.integration.cpi.response_parser;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Arsenii Istlentev
 */
class IntegrationContentPrivateApiParserTest {

    @Test
    void getAllIntegrationRuntimeArtifacts() throws IOException {
        //this payload has a German letter
        String sourceModelWithoutScript = IOUtils.toString(
                this.getClass().getClassLoader().getResource("client/integration-runtime-artifacts-response.xml"),
                StandardCharsets.UTF_8
        );
        IntegrationContentPrivateApiParser.getAllIntegrationRuntimeArtifacts(sourceModelWithoutScript);
    }
}