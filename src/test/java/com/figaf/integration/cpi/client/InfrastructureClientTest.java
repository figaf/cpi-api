package com.figaf.integration.cpi.client;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.CloudPlatformType;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.data_provider.AgentTestDataProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static com.figaf.integration.cpi.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arsenii Istlentev
 */
public class InfrastructureClientTest {

    private static InfrastructureClient infrastructureClient = null;

    @BeforeAll
    static void setUp() {
        infrastructureClient = new InfrastructureClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_fetchIflMapHost(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        String fetchIflMapHost = infrastructureClient.fetchIflMapHost(requestContext);
        if (CloudPlatformType.NEO.equals(agentTestData.getCloudPlatformType())) {
            assertThat(fetchIflMapHost).isEqualTo("p0201-iflmap.hcisbp.eu1.hana.ondemand.com");
        } else {
            assertThat(fetchIflMapHost).isEqualTo("figafawscf.it-cpi001-rt.cfapps.eu10.hana.ondemand.com");
        }
    }

}
