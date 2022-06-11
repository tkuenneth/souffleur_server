package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.imageio.ImageIO;
import java.awt.*;
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

    private HttpServer httpServer;
    private String address;
    private int port;

    public Server() throws AWTException {
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
    }

    @Override
    public void handle(HttpExchange t) {
        URI requestUri = t.getRequestURI();
        String path = requestUri.getPath().toLowerCase();
        if (path.endsWith(("next"))) {
            Utils.sendCursorRight(robot);
        } else if (path.endsWith(("previous"))) {
            Utils.sendCursorLeft(robot);
        } else if (path.endsWith(("home"))) {
            Utils.sendHome(robot);
        } else if (path.endsWith(("qrcode"))) {
            sendQRCode(t);
        } else if (path.endsWith(("hello"))) {
            sendStringResult(t, "Hello, world!");
        }
    }

    public void start(String address, int port) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(address);
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
        this.address = address;
        this.port = port;
        this.httpServer = HttpServer.create(socketAddress, 0);
        this.httpServer.createContext("/souffleur", this);
        this.httpServer.setExecutor(null);
        this.httpServer.start();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public String getQRCodeAsString() {
        return String.format("http://%s:%s/souffleur/", address, port);
    }

    private void sendQRCode(HttpExchange t) {
        BufferedImage image = Utils.generateQRCode(getQRCodeAsString());
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
}
