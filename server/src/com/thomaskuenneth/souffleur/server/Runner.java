package com.thomaskuenneth.souffleur.server;

import com.sun.net.httpserver.HttpServer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Runner {

    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

    public static void main(String[] args) {
        try {
            HttpServer server = NetUtilities.createServer();
            server.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create http server", e);
        }
    }
}
