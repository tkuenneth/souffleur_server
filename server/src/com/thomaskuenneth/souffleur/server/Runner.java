package com.thomaskuenneth.souffleur.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Runner {

    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Runner <json file> <adapter name>");
                System.exit(1);
            }
            List<String> addresses = Utils.findIpAddress(args[1]);
            Server server = new Server(args[0], addresses.get(0));
            server.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create server", e);
        }
    }
}
