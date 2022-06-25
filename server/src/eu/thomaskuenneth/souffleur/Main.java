package eu.thomaskuenneth.souffleur;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Main extends JFrame {

    public static final String VERSION = "1.0.3";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String KEY_SECRET = "secret";
    private static final String KEY_SHOW_QRCODE = "showQrcode";

    private final ViewModel viewModel;
    private final Map<String, List<String>> devices;
    private final Preferences prefs;

    private JDialog qrCodeDialog;

    public Main() throws AWTException, SocketException {
        super("Souffleur");
        var filenames = new String[]{
                "/eu/thomaskuenneth/souffleur/Icon-App-1024x1024@1x.png",
                "/eu/thomaskuenneth/souffleur/Icon-App-76x76@1x.png",
                "/eu/thomaskuenneth/souffleur/Icon-App-40x40@1x.png",
                "/eu/thomaskuenneth/souffleur/Icon-App-29x29@1x.png",
                "/eu/thomaskuenneth/souffleur/Icon-App-20x20@1x.png"};
        List<Image> images = Utils.loadIconImages(filenames);
        if (images.size() > 0) {
            setIconImages(images);
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE))
                    taskbar.setIconImage(images.get(0));
            }
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                viewModel.stopServer();
                System.exit(0);
            }
        });
        setResizable(false);
        viewModel = new ViewModel();
        devices = Utils.getIpAddress();
        setContentPane(createMainPanel());
        viewModel.setPort(8087);
        viewModel.setRunning(false);
        prefs = Preferences.userNodeForPackage(this.getClass());
        String secret = prefs.get(KEY_SECRET, null);
        if (secret == null) {
            secret = UUID.randomUUID().toString();
            prefs.put(KEY_SECRET, secret);
        }
        viewModel.setSecret(secret);
        viewModel.setShowQRCode(prefs.getBoolean(KEY_SHOW_QRCODE, true));
        viewModel.observeShowQRCode(value -> prefs.putBoolean(KEY_SHOW_QRCODE, value));
        pack();
    }

    private JComponent createMainPanel() {
        Box mainPanel = new Box(BoxLayout.PAGE_AXIS);
        List<Component> updates = new ArrayList<>();
        updates.add(mainPanel.add(createDeviceSelector()));
        updates.add(mainPanel.add(createConfigSwitches()));
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(createIndicators());
        updates.add(mainPanel.add((createButtonPanel())));
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getContentPane().getWidth();
                for (Component c : updates) {
                    int height = c.getPreferredSize().height;
                    Dimension size = new Dimension(width, height);
                    c.setMaximumSize(size);
                }
            }
        });
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return mainPanel;
    }

    private JPanel createDeviceSelector() {
        String defaultDevice = Utils.getDefaultNetworkInterfaceDisplayName();
        JPanel panel = UIFactory.createFlowPanel();
        String[] names = devices.keySet().toArray(new String[]{});
        JComboBox<String> comboBox = new JComboBox<>(names);
        comboBox.addItemListener(e -> viewModel.setDevice((String) e.getItem()));
        panel.add(comboBox);
        JLabel label = new JLabel();
        panel.add(label);
        JTextComponent port = UIFactory.createIntegerField(0, 65535);
        port.addPropertyChangeListener(evt -> {
            try {
                viewModel.setPort(Integer.parseInt(port.getText()));
            } catch (NumberFormatException ex) {
                viewModel.setPort(null);
            }
        });
        panel.add(port);
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "device" -> {
                    String device = evt.getNewValue().toString();
                    viewModel.setAddress(devices.get(device).get(0));
                }
                case "address" -> label.setText(viewModel.getAddress());
                case "port" -> port.setText(Utils.nullSafeString(evt.getNewValue()));
                case "running" -> {
                    boolean running = (boolean) evt.getNewValue();
                    comboBox.setEnabled(!running);
                    port.setEditable(!running);
                }
            }
        });
        comboBox.setSelectedItem(defaultDevice);
        return panel;
    }

    private JPanel createConfigSwitches() {
        JPanel panel = UIFactory.createFlowPanel();
        JCheckBox cbShowQRCode = new JCheckBox("Show qrcode upon start");
        cbShowQRCode.addActionListener(e -> viewModel.setShowQRCode(cbShowQRCode.isSelected()));
        panel.add(cbShowQRCode);
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "showQRCode" -> cbShowQRCode.setSelected((boolean) evt.getNewValue());
                case "running" -> {
                    boolean running = (boolean) evt.getNewValue();
                    cbShowQRCode.setEnabled(!running);
                }
            }
        });
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton startStop = new JButton();
        startStop.addActionListener(e -> viewModel.setRunning(!viewModel.isRunning()));
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "running" -> {
                    boolean running = (boolean) evt.getNewValue();
                    if (running) {
                        try {
                            viewModel.startServer();
                            startStop.setText("Stop");
                            if (viewModel.isShowQRCode()) {
                                qrCodeDialog = showQRCode();
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "startServer()", e);
                            viewModel.setRunning(false);
                        }
                    } else {
                        hideQRCode();
                        viewModel.stopServer();
                        startStop.setText("Start");
                    }
                }
                case "startStopButtonEnabled" -> startStop.setEnabled((boolean) evt.getNewValue());
            }
        });
        JLabel versionLabel = new JLabel(String.format("Version %s", VERSION));
        versionLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        panel.add(versionLabel, BorderLayout.CENTER);
        panel.add(startStop, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        return panel;
    }

    private JPanel createIndicators() {
        JPanel panel = UIFactory.createFlowPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Command indicators"));
        panel.add(createIndicator(Server.HOME));
        panel.add(createIndicator(Server.PREVIOUS));
        panel.add(createIndicator(Server.NEXT));
        panel.add(createIndicator(Server.END));
        return panel;
    }

    private JLabel createIndicator(String indicator) {
        Map<String, String> symbols = Map.of(
                Server.HOME, "\u23ee",
                Server.PREVIOUS, "\u25c0",
                Server.NEXT, "\u25b6",
                Server.END, "\u23ed"
        );
        JLabel label = new JLabel(symbols.get(indicator));
        viewModel.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("lastCommand")) {
                label.setForeground(indicator.equals(evt.getNewValue()) ? Color.red : UIManager.getColor("Label.foreground"));
            }
        });
        return label;
    }

    private JDialog showQRCode() {
        hideQRCode();
        String url = viewModel.getQRCodeAsString();
        JDialog dialog = new JDialog(this, false);
        BufferedImage image = Utils.generateQRCode(url);
        ImageIcon ii = new ImageIcon(image);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JLabel(ii), BorderLayout.CENTER);
        dialog.setContentPane(contentPane);
        dialog.setTitle("Souffleur - QR code");
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
        return dialog;
    }

    private void hideQRCode() {
        if (qrCodeDialog != null) {
            if (qrCodeDialog.isVisible()) {
                qrCodeDialog.setVisible(false);
                qrCodeDialog.dispose();
            }
            qrCodeDialog = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Main ui = new Main();
                ui.setLocationRelativeTo(null);
                ui.setVisible(true);
            } catch (ClassNotFoundException | InstantiationException
                     | IllegalAccessException | UnsupportedLookAndFeelException
                     | AWTException | SocketException e) {
                LOGGER.log(Level.SEVERE, "setLookAndFeel()", e);
            }
        });
    }
}
