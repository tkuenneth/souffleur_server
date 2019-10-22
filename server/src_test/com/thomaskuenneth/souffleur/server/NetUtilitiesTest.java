package com.thomaskuenneth.souffleur.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetUtilitiesTest {

    @Test
    void testServicesAreEqual() {
        String amazon = NetUtilities.getIpAddressFromAmazon();
        String myExternalIp = NetUtilities.getIpAddressFromMyExternalIp();
        Assertions.assertEquals(amazon, myExternalIp);
    }
}
