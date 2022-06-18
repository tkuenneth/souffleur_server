package eu.thomaskuenneth.souffleur;

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

    public static final String END = "end";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String HOME = "home";
    public static final String QRCODE = "qrcode";
    public static final String HELLO = "hello";
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot robot;
    private final ServerCallback callback;

    private HttpServer httpServer;
    private String address;
    private int port;

    public Server(ServerCallback callback) throws AWTException {
        this.callback = callback;
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
    }

    @Override
    public void handle(HttpExchange t) {
        URI requestUri = t.getRequestURI();
        String path = requestUri.getPath();
        switch (path.substring(path.lastIndexOf('/') + 1).toLowerCase()) {
            case HOME -> {
                Utils.sendHome(robot);
                callback.commandReceived(HOME);
            }
            case PREVIOUS -> {
                Utils.sendCursorLeft(robot);
                callback.commandReceived(PREVIOUS);
            }
            case NEXT -> {
                Utils.sendCursorRight(robot);
                callback.commandReceived(NEXT);
            }
            case END -> {
                Utils.sendEnd(robot);
                callback.commandReceived(END);
            }
            case QRCODE -> {
                sendQRCode(t);
            }
            case HELLO -> {
                sendStringResult(t, "Hello, world!");
            }
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
