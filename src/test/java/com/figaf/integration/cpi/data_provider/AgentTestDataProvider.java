package com.figaf.integration.cpi.data_provider;

import com.figaf.integration.common.data_provider.AbstractAgentTestDataProvider;
import com.figaf.integration.common.data_provider.AgentTestData;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Ilya Nesterov
 */
public class AgentTestDataProvider extends AbstractAgentTestDataProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
//            Arguments.of(buildAgentTestDataForNeo()),
//            Arguments.of(buildAgentTestDataForCf1())
//            Arguments.of(buildAgentTestDataForCf1UseCustomIdp())
            Arguments.of(buildAgentTestDataForCfIntegrationSuite())
//                Arguments.of(buildAgentTestDataForCf3()),
//                Arguments.of(buildAgentTestDataForCf4())
        );
    }

    public static AgentTestData buildAgentTestDataForCf1() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-1"));
    }

    public static AgentTestData buildAgentTestDataForCf1UseCustomIdp() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-1-use-custom-idp"));
    }

    public static AgentTestData buildAgentTestDataForCfIntegrationSuite() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-integration-suite"));
    }

    public static AgentTestData buildAgentTestDataForCf2() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-2"));
    }

    public static AgentTestData buildAgentTestDataForCf3() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-oauth-3"));
    }

    public static AgentTestData buildAgentTestDataForCf4() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-oauth-4"));
    }

    public static AgentTestData buildAgentTestDataForNeo() {
        return buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-neo-1"));
    }

}
