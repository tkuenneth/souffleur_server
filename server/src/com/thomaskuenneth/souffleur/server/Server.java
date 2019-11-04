package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot robot;

    private HttpServer httpServer;
    private SlideNotes[] slideNotes;
    private String address;
    private int port;

    private int currentSlide;

    public Server() throws AWTException {
        robot = new Robot();
    }

    @Override
    public void handle(HttpExchange t) {
        URI requestUri = t.getRequestURI();
        String path = requestUri.getPath().toLowerCase();
        if (path.endsWith(("start"))) {
            currentSlide = 0;
            sendNotes(t, currentSlide);
        } else if (path.endsWith(("current"))) {
            sendNotes(t, currentSlide);
        } else if (path.endsWith(("next"))) {
            if (updateCurrentSlide(1)) {
                Utils.sendCursorRight(robot);
            }
            sendNotes(t, currentSlide);
        } else if (path.endsWith(("previous"))) {
            if (updateCurrentSlide(-1)) {
                Utils.sendCursorLeft(robot);
            }
            sendNotes(t, currentSlide);
        } else if (path.endsWith(("qrcode"))) {
            sendQRCode(t);
        } else {
            sendNotes(t, currentSlide);
        }
    }

    public void start(String jsonFile, String address, int port) throws UnknownHostException, IOException {
        InetAddress inetAddress = InetAddress.getByName(address);
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
        this.slideNotes = readSlideNotes(jsonFile);
        this.address = address;
        this.port = port;
        this.httpServer = HttpServer.create(socketAddress, 0);
        this.httpServer.createContext("/souffleur", this);
        this.httpServer.setExecutor(null);
        this.httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        httpServer = null;
    }

    public String getQRCodeAsString() {
        return String.format("http://%s:%s/souffleur/", address, port);
    }

    private boolean updateCurrentSlide(int offset) {
        int last = currentSlide;
        currentSlide += offset;
        if (currentSlide < 0) {
            currentSlide = 0;
        } else if (currentSlide >= slideNotes.length) {
            currentSlide = slideNotes.length - 1;
        }
        return last != currentSlide;
    }

    private void sendNotes(HttpExchange t, int slide) {
        JSONObject object = new JSONObject(slideNotes[slide]);
        sendStringResult(t, object.toString());
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

    private SlideNotes[] readSlideNotes(String filename) {
        List<SlideNotes> slideNotes = new ArrayList<>();
        String json = Utils.readTextFile(filename);
        if (json.length() == 0) {
            throw new RuntimeException(String.format("Could not read json file %s", filename));
        }
        JSONArray array = new JSONArray(json);
        int total = array.length();
        for (int i = 0; i < total; i++) {
            JSONObject object = array.getJSONObject(i);
            String name = object.getString("Name");
            JSONArray jsonNotes = object.getJSONArray("Notes");
            String[] notes = new String[jsonNotes.length()];
            for (int j = 0; j < jsonNotes.length(); j++) {
                String note = jsonNotes.getString(j);
                notes[j] = note;
            }
            SlideNotes current = new SlideNotes(name, notes, i + 1, total);
            slideNotes.add(current);
        }
        SlideNotes[] result = new SlideNotes[slideNotes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = slideNotes.get(i);
        }
        currentSlide = 0;
        return result;
    }
}
