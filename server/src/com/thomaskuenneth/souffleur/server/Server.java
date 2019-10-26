package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Robot;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot r;
    private final HttpServer server;

    public Server(String json, String address) throws Exception {
        r = new Robot();
        server = createServer(address);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        URI requestUri = t.getRequestURI();
        String path = requestUri.getPath().toLowerCase();
        String response = "???";
        if (path.endsWith(("next"))) {
            response = "next";
            Utils.sendCursorRight(r);
        } else if (path.endsWith(("previous"))) {
            response = "previous";
            Utils.sendCursorLeft(r);
        }
        byte[] result = response.getBytes();
        t.sendResponseHeaders(200, result.length);
        OutputStream os = t.getResponseBody();
        os.write(result);
        os.close();
    }

    public void start() {
        server.start();
    }

    private HttpServer createServer(String address) throws Exception {
        InetAddress inetAddress = InetAddress.getByName(address);
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 8087);
        LOGGER.log(Level.INFO, inetAddress.getHostAddress());
        HttpServer server = HttpServer.create(socketAddress, 0);
        server.createContext("/souffleur", this);
        server.setExecutor(null);
        return server;
    }
}
