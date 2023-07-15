package eu.thomaskuenneth.souffleur;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Souffleur server utilizes {@code com.sun.net.httpserver.HttpsServer} to listen to
 * client requests. It uses a keystore that holds a self-signed certificate. To create a
 * keystore you can do something like this:
 * <pre>
 * keytool -genkeypair -keyalg RSA -alias selfsigned
 *         -keystore souffleur.jks -storepass password
 *         -validity 9999 -keysize 2048
 * </pre>
 * In this case, the password would be <strong>password</strong>.
 */
public class Server implements HttpHandler {

    static {
        System.setProperty("sun.net.httpserver.nodelay", "true");
        System.setProperty("sun.net.httpserver.debug", "false");
    }

    public static final String END = "end";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String HOME = "home";
    public static final String HELLO = "hello";
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Robot robot;
    private final ServerCallback callback;

    private HttpsServer httpServer;

    public Server(ServerCallback callback) throws AWTException {
        this.callback = callback;
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
    }

    @Override
    public void handle(HttpExchange t) {
        try {
            String path = t.getRequestURI().getPath();
            switch (path.substring(path.lastIndexOf('/') + 1).toLowerCase()) {
                case HOME -> {
                    Utils.sendHome(robot);
                    callback.commandReceived(HOME);
                    sendResult(t, "", HttpURLConnection.HTTP_OK);
                }
                case PREVIOUS -> {
                    Utils.sendCursorLeft(robot);
                    callback.commandReceived(PREVIOUS);
                    sendResult(t, "", HttpURLConnection.HTTP_OK);
                }
                case NEXT -> {
                    Utils.sendCursorRight(robot);
                    callback.commandReceived(NEXT);
                    sendResult(t, "", HttpURLConnection.HTTP_OK);
                }
                case END -> {
                    Utils.sendEnd(robot);
                    callback.commandReceived(END);
                    sendResult(t, "", HttpURLConnection.HTTP_OK);
                }
                case HELLO -> {
                    sendResult(t,
                            String.format("Hello, world! It's %s.",
                                    DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(LocalTime.now())),
                            HttpURLConnection.HTTP_OK);
                    callback.commandReceived(HELLO);
                }
                default -> sendResult(t, "", HttpURLConnection.HTTP_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "handle()", e);
            sendResult(t, "", HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public boolean start(String address, int port, String secret) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
            this.httpServer = HttpsServer.create(socketAddress, 0);
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream is = getClass().getResourceAsStream("/souffleur.jks");
            ks.load(is, password);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            this.httpServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
            this.httpServer.createContext("/souffleur/" + secret, this);
            this.httpServer.setExecutor(null);
            this.httpServer.start();
            return true;
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException |
                 UnrecoverableKeyException | CertificateException e) {
            LOGGER.log(Level.SEVERE, "start()", e);
            stop();
        }
        return false;
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public boolean isStarted() {
        return httpServer != null;
    }

    private void sendResult(HttpExchange t, String text, int status) {
        try {
            t.getRequestBody().close();
            byte[] result = text.getBytes();
            t.getResponseHeaders().add("Content-Type", "text/plain");
            t.sendResponseHeaders(status, result.length);
            OutputStream out = t.getResponseBody();
            out.write(result);
            out.flush();
            out.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendStringResult()", e);
        }
    }
}
