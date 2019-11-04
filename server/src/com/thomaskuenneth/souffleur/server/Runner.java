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
            Server server = new Server();
            server.start(args[0], addresses.get(0), 8087);
            StringBuilder sb = new StringBuilder(server.getQRCodeAsString());
            if (!sb.toString().endsWith("/")) {
                sb.append('/');
            }
            sb.append("qrcode");
            Utils.browse(sb.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create server", e);
        }
    }
}
