package com.thomaskuenneth.souffleur.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetUtilities {

    private static final Logger LOGGER = Logger.getLogger(NetUtilities.class.getName());

    public static String getIpAddressFromAmazon() {
        return getIpAddress("https://checkip.amazonaws.com");
    }

    public static String getIpAddressFromMyExternalIp() {
        return getIpAddress("https://myexternalip.com/raw");
    }

    public static String getIpAddress(String service) {
        try {
            URL url = new URL(service);
            try (InputStream is = url.openStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
                return in.readLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "getIpAddress()", e);
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "getIpAddress()", e);
        }
        return null;
    }
}
