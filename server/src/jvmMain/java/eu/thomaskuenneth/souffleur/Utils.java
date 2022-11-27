package eu.thomaskuenneth.souffleur;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    public static void sendCursorLeft(Robot r) {
        sendKeycode(r, KeyEvent.VK_LEFT);
    }

    public static void sendCursorRight(Robot r) {
        sendKeycode(r, KeyEvent.VK_RIGHT);
    }

    public static void sendHome(Robot r) {
        sendKeycode(r, KeyEvent.VK_HOME);
    }

    public static void sendEnd(Robot r) {
        sendKeycode(r, KeyEvent.VK_END);
    }

    public static BufferedImage generateQRCode(String text) {
        BufferedImage result = null;
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 1);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200,
                    200, hintMap);
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

    public static String nullSafeString(Object s) {
        return (s == null) ? "" : s.toString();
    }

    public static List<Image> loadIconImages(String[] paths) {
        List<Image> iconImages = new ArrayList<>();
        for (String path : paths) {
            Image image = loadImage(path);
            if (image != null) {
                iconImages.add(image);
            }
        }
        return iconImages;
    }

    public static Image loadImage(String path) {
        try (InputStream in = UIFactory.class.getResourceAsStream(path)) {
            if (in != null)
                return ImageIO.read(in);
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "loadImage()", e);
        }
        return null;
    }

    private static void sendKeycode(Robot r, int keycode) {
        SwingUtilities.invokeLater(() -> {
            r.keyPress(keycode);
            r.keyRelease(keycode);
        });
    }
}
