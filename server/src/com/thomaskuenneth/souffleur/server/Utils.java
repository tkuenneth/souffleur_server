package com.thomaskuenneth.souffleur.server;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.thomaskuenneth.souffleur.server.ui.UIFactory;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    public static String getIpAddressFromAmazon() {
        return getIpAddress("https://checkip.amazonaws.com");
    }

    public static String getIpAddressFromMyExternalIp() {
        return getIpAddress("https://myexternalip.com/raw");
    }

    public static String getIpAddress(String service) {
        try {
            URL url = new URL(service);
            try (InputStream is = url.openStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
                return in.readLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "getIpAddress()", e);
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "getIpAddress()", e);
        }
        return null;
    }

    public static List<String> findIpAddress(String displayName) throws Exception {
        return getIpAddress().get(displayName);
    }

    public static Map<String, List<String>> getIpAddress() throws SocketException {
        Map<String, List<String>> result = new HashMap<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if ((networkInterface.isUp()) && !networkInterface.isLoopback()) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                List<String> list = new ArrayList<>();
                String name = networkInterface.getDisplayName();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String hostAddress = addr.getHostAddress();
                    boolean isInet4Address = addr instanceof Inet4Address;
                    if (isInet4Address) {
                        list.add(hostAddress);
                    }
                    LOGGER.info(String.format("%s: %s (%b)", name, hostAddress, isInet4Address));
                }
                if (list.size() > 0) {
                    result.put(name, list);
                }
            }
        }
        return result;
    }

    public static void sendCursorLeft(Robot r) {
        r.keyPress(KeyEvent.VK_LEFT);
        r.keyRelease(KeyEvent.VK_LEFT);
    }

    public static void sendCursorRight(Robot r) {
        r.keyPress(KeyEvent.VK_RIGHT);
        r.keyRelease(KeyEvent.VK_RIGHT);
    }

    public static BufferedImage generateQRCode(String text) {
        BufferedImage result = null;
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 1);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 400,
                    400, hintMap);
            int byteMatrixWidth = byteMatrix.getWidth();
            int byteMatrixHeight = byteMatrix.getHeight();
            BufferedImage image = new BufferedImage(byteMatrixWidth, byteMatrixHeight,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, byteMatrixWidth, byteMatrixHeight);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < byteMatrixWidth; i++) {
                for (int j = 0; j < byteMatrixWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            result = image;
        } catch (WriterException e) {
            LOGGER.log(Level.SEVERE, "generateQRCode()", e);
        }
        return result;
    }

    public static String readTextFile(String filename) throws IOException {
        if (filename == null) {
            throw new IOException("filename is null");
        }
        try (FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    public static void browse(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "browse()", e);
            }
        }
    }

    public static String nullSafeString(Object s) {
        return (s == null) ? "" : s.toString();
    }

    public static void setIconImages(JFrame f, String[] paths) {
        List<Image> iconImages = new ArrayList<>();
        for (String path : paths) {
            var image = loadImage(path);
            if (image != null) {
                iconImages.add(image);
            }
        }
        if (iconImages.size() > 0) {
            f.setIconImages(iconImages);
        }
    }

    public static Image loadImage(String path) {
        try (InputStream in = UIFactory.class.getResourceAsStream(path)) {
            return ImageIO.read(in);
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "loadImage()", e);
        }
        return null;
    }

    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    public static void setDockIconImage(Image image) {
        try {
            Class c = Class.forName("com.apple.eawt.Application", false, null);
            Method getApplication = c.getDeclaredMethod("getApplication");
            Object _app = getApplication.invoke(null);
            Method setDockIconImage = c.getDeclaredMethod("setDockIconImage", Image.class);
            setDockIconImage.invoke(_app, image);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "setDockIconImage()", e);
        }
    }
}
