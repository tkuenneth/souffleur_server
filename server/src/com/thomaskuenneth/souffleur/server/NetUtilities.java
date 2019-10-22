package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
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

    public static HttpServer createServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/souffleur", new SouffleurHandler());
        server.setExecutor(null);
        return server;
    }

    static class SouffleurHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestUri = t.getRequestURI();
            String path = requestUri.getPath().toLowerCase();
            String response = "???";
            if (path.endsWith(("next"))) {
                response = "ok";
            } else if (path.endsWith(("previous"))) {
                response = "ok";
            }
            byte[] result = response.getBytes();
            t.sendResponseHeaders(200, result.length);
            OutputStream os = t.getResponseBody();
            os.write(result);
            os.close();
        }
    }
}
