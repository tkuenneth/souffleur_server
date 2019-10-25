package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
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

    public static HttpServer createServer() throws Exception {
        InetAddress inetAddress = InetAddress.getByName("192.168.43.126");
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 8087);
        LOGGER.log(Level.INFO, inetAddress.getHostAddress());
        HttpServer server = HttpServer.create(socketAddress, 0);
        server.createContext("/souffleur", new SouffleurHandler());
        server.setExecutor(null);
        return server;
    }

    static class SouffleurHandler implements HttpHandler {

        Robot r;

        SouffleurHandler() {
            try {
                r = new Robot();
            } catch (AWTException e) {
                LOGGER.log(Level.SEVERE, "Could not create robot", e);
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestUri = t.getRequestURI();
            String path = requestUri.getPath().toLowerCase();
            String response = "???";
            if (path.endsWith(("next"))) {
                response = "next";
                sendCursorRight();
            } else if (path.endsWith(("previous"))) {
                response = "previous";
                sendCursorLeft();
            }
            byte[] result = response.getBytes();
            t.sendResponseHeaders(200, result.length);
            OutputStream os = t.getResponseBody();
            os.write(result);
            os.close();
        }

        void sendCursorLeft() {
            r.keyPress(KeyEvent.VK_LEFT);
            r.keyRelease(KeyEvent.VK_LEFT);
        }

        void sendCursorRight() {
            r.keyPress(KeyEvent.VK_RIGHT);
            r.keyRelease(KeyEvent.VK_RIGHT);
        }
    }
}
