package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.imageio.ImageIO;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot robot;
    private final HttpServer httpServer;

    public Server(String json, String address) throws Exception {
        robot = new Robot();
        httpServer = createServer(address);
    }

    @Override
    public void handle(HttpExchange t) {
        URI requestUri = t.getRequestURI();
        String path = requestUri.getPath().toLowerCase();
        if (path.endsWith(("next"))) {
            Utils.sendCursorRight(robot);
            sendStringResult(t, "OK");
        } else if (path.endsWith(("previous"))) {
            Utils.sendCursorLeft(robot);
            sendStringResult(t, "OK");
        } else if (path.endsWith(("qrcode"))) {
            sendQRCode(t);
        } else {
            sendStringResult(t, "???");
        }
    }

    public void start() {
        httpServer.start();
    }

    private void sendQRCode(HttpExchange t) {
        InetSocketAddress address = httpServer.getAddress();
        String url = String.format("http://%s:%s/souffleur/",
                address.getHostName(),
                address.getPort());
        BufferedImage image = Utils.generateQRCode(url);
        try (OutputStream os = t.getResponseBody()) {
            t.sendResponseHeaders(200, 0);
            ImageIO.write(image, "jpg", os);
            t.getResponseHeaders().add("Content-Type", "image/jpeg");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendQRCode()", e);
        }
    }

    private void sendStringResult(HttpExchange t, String text) {
        byte[] result = text.getBytes();
        try (OutputStream os = t.getResponseBody()) {
            t.sendResponseHeaders(200, result.length);
            os.write(result);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendStringResult()", e);
        }
    }

    private HttpServer createServer(String address) throws Exception {
        InetAddress inetAddress = InetAddress.getByName(address);
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 8087);
        HttpServer httpServer = HttpServer.create(socketAddress, 0);
        httpServer.createContext("/souffleur", this);
        httpServer.setExecutor(null);
        return httpServer;
    }
}
