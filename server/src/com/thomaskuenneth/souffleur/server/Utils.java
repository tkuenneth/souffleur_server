package com.thomaskuenneth.souffleur.server;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

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

    public static List<String> findIpAddress(String displayName) throws Exception {
        List<String> result = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isUp()) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String name = networkInterface.getDisplayName();
                    String hostAddress = addr.getHostAddress();
                    if (displayName.equals(name)) {
                        result.add(hostAddress);
                    }
                    LOGGER.info(String.format("%s: %s", name, hostAddress));
                }
            }
        }
        return result;
    }

    public static void sendCursorLeft(Robot r) {
        r.keyPress(KeyEvent.VK_LEFT);
        r.keyRelease(KeyEvent.VK_LEFT);
    }

    public static void sendCursorRight(Robot r) {
        r.keyPress(KeyEvent.VK_RIGHT);
        r.keyRelease(KeyEvent.VK_RIGHT);
    }
}
