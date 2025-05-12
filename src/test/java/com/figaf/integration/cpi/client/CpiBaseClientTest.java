package com.figaf.integration.cpi.client;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kostas Charalambous
 */
public class CpiBaseClientTest {

    @Test
    void testFigafTestIntegrationSuiteHost() {
        assertTrue(CpiBaseClient.isIntegrationSuiteHost(
            "figaftest-1.integrationsuite.cfapps.eu10-003.hana.ondemand.com"));
    }

    @Test
    void testEuDevIntegrationSuiteHost() {
        assertTrue(CpiBaseClient.isIntegrationSuiteHost(
            "test-eu-dev.integrationsuite-cpi026.cfapps.eu10-002.hana.ondemand.com"));
    }

    @Test
    void testDevIntegrationSuiteHost() {
        assertTrue(CpiBaseClient.isIntegrationSuiteHost(
            "dev-integrationsuite-region.integrationsuite.cfapps.eu10-002.hana.ondemand.com"));
    }

    @Test
    void testPublicUrlsNotIntegrationSuiteHost() {
        assertFalse(CpiBaseClient.isIntegrationSuiteHost(
            "figafTestHost-1.it-cpi018.cfapps.eu10-003.hana.ondemand.com"));
        assertFalse(CpiBaseClient.isIntegrationSuiteHost(
            "test-eu-dev.it-cpi026.cfapps.eu10-002.hana.ondemand.com"));
        assertFalse(CpiBaseClient.isIntegrationSuiteHost(
            "dev-integrationsuite-region.it-xxxxxxxx.cfapps.eu10-002.hana.ondemand.com"));
    }

    @Test
    void testFalseWhenIntegrationSuiteLaterThanSecondSegment() {
        assertFalse(CpiBaseClient.isIntegrationSuiteHost(
            "first.second.integrationsuite.third.com"));
    }

    @Test
    void testFalseWhenIntegrationSuiteIsFirst() {
        assertFalse(CpiBaseClient.isIntegrationSuiteHost(
            "dev-integrationsuite.second.third.third.com"));
    }

    @Test
    void testFalseWhenTooFewSegments() {
        assertFalse(CpiBaseClient.isIntegrationSuiteHost("justonetoken"));
        assertFalse(CpiBaseClient.isIntegrationSuiteHost("one.two"));
    }

    @Test
    void testMixedCaseIntegrationSuiteSegment() {
        assertTrue(CpiBaseClient.isIntegrationSuiteHost(
            "abc.integrationsuite-CPI.xyz.company.com"));
    }

    @Test
    void testMinimalThreeSegmentHost() {
        assertTrue(CpiBaseClient.isIntegrationSuiteHost(
            "a.integrationsuite.b"));
    }
}
