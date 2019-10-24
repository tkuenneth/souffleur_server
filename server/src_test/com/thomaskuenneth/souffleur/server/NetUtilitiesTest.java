package com.thomaskuenneth.souffleur.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetUtilitiesTest {

    @Test
    void testServicesAreEqual() {
        String amazon = Utils.getIpAddressFromAmazon();
        String myExternalIp = Utils.getIpAddressFromMyExternalIp();
        Assertions.assertEquals(amazon, myExternalIp);
    }
}
