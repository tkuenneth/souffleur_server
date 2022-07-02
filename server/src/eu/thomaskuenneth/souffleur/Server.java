package eu.thomaskuenneth.souffleur;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server implements HttpHandler {

    public static final String END = "end";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String HOME = "home";
    public static final String HELLO = "hello";
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot robot;
    private final ServerCallback callback;

    private HttpsServer httpServer;
    private String address;
    private int port;
    private String secret;

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
                sendStatus(t, 200);
            }
            case PREVIOUS -> {
                Utils.sendCursorLeft(robot);
                callback.commandReceived(PREVIOUS);
                sendStatus(t, 200);
            }
            case NEXT -> {
                Utils.sendCursorRight(robot);
                callback.commandReceived(NEXT);
                sendStatus(t, 200);
            }
            case END -> {
                Utils.sendEnd(robot);
                callback.commandReceived(END);
                sendStatus(t, 200);
            }
            case HELLO -> {
                sendStringResult(t, String.format("Hello, world! It's %s.",
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(LocalTime.now())));
                callback.commandReceived(HELLO);
            }
            default -> sendStatus(t, 404);
        }
    }

    public boolean start(String address, int port, String secret) {
        boolean success = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
            this.address = address;
            this.port = port;
            this.secret = secret;
            this.httpServer = HttpsServer.create(socketAddress, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream is = getClass().getResourceAsStream("/souffleur.jks");
            ks.load(is, password);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            this.httpServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    SSLContext c = getSSLContext();
                    SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    SSLParameters sslParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslParameters);
                }
            });
            this.httpServer.createContext("/souffleur/" + secret, this);
            this.httpServer.setExecutor(null);
            this.httpServer.start();
            LOGGER.log(Level.INFO, getQRCodeAsString());
            success = true;
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException |
                 UnrecoverableKeyException | CertificateException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        return success;
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public String getQRCodeAsString() {
        return String.format("https://%s:%s/souffleur/%s/", address, port, secret);
    }

    private void sendStringResult(HttpExchange t, String text) {
        byte[] result = text.getBytes();
        try (OutputStream os = t.getResponseBody()) {
            t.sendResponseHeaders(200, result.length);
            os.write(result);
            t.getResponseHeaders().add("Content-Type", "text/plain");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendStringResult()", e);
        }
    }

    private void sendStatus(HttpExchange t, int status) {
        try (OutputStream ignored = t.getResponseBody()) {
            t.sendResponseHeaders(status, 0);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendStringResult()", e);
        }
    }
}
