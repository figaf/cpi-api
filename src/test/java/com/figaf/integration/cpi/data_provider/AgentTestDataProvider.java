package com.figaf.integration.cpi.data_provider;

import com.figaf.integration.common.data_provider.AbstractAgentTestDataProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Ilya Nesterov
 */
public class AgentTestDataProvider extends AbstractAgentTestDataProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
                Arguments.of(buildAgentTestData(
                        Paths.get("src/test/resources/agent-test-data/cpi-neo-1")
                )),
                Arguments.of(buildAgentTestData(
                        Paths.get("src/test/resources/agent-test-data/cpi-cf-1")
                ))
        );
    }
}
