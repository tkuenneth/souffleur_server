package eu.thomaskuenneth.souffleur;

import mdlaf.MaterialLookAndFeel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Main extends JFrame {

    public static final String VERSION = "1.0.5";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String KEY_SECRET = "secret";

    private final ViewModel viewModel;
    private final Map<String, List<String>> devices;

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
        viewModel.setDevice(Utils.getDefaultNetworkInterfaceDisplayName());
        viewModel.setPort(8087);
        viewModel.setRunning(false);
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String secret = prefs.get(KEY_SECRET, null);
        if (secret == null) {
            secret = UUID.randomUUID().toString();
            prefs.put(KEY_SECRET, secret);
        }
        viewModel.setSecret(secret);
        viewModel.observeShowQRCode(value -> {
            if (value)
                showQRCode();
            else
                hideQRCode();
        });
        pack();
    }

    private JComponent createMainPanel() {
        Box mainPanel = new Box(BoxLayout.PAGE_AXIS);
        List<Component> updates = new ArrayList<>();
        updates.add(mainPanel.add(createDeviceSelector()));
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(createStartStopButtonPanel());
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(createIndicators());
        mainPanel.add(Box.createVerticalGlue());
        updates.add(mainPanel.add((createVersionLabelPanel())));
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
        JPanel panel = UIFactory.createFlowPanel();
        String[] names = devices.keySet().toArray(new String[]{});
        JComboBox<String> comboBox = new JComboBox<>(names);
        comboBox.addItemListener(e -> viewModel.setDevice((String) e.getItem()));
        panel.add(comboBox);
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
                case "port" -> port.setText(Utils.nullSafeString(evt.getNewValue()));
                case "running" -> {
                    boolean running = (boolean) evt.getNewValue();
                    comboBox.setEnabled(!running);
                    port.setEditable(!running);
                }
            }
        });
        return panel;
    }

    private JPanel createStartStopButtonPanel() {
        JPanel panel = new JPanel();
        JButton startStop = new JButton();
        startStop.addActionListener(e -> viewModel.setRunning(!viewModel.isRunning()));
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "running" -> {
                    boolean running = (boolean) evt.getNewValue();
                    if (running) {
                        if (viewModel.startServer()) {
                            startStop.setText("Stop");
                            viewModel.setShowQRCode(true);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Could not start server",
                                    "Souffleur",
                                    JOptionPane.WARNING_MESSAGE);
                            viewModel.setRunning(false);
                            LOGGER.log(Level.SEVERE, "startServer() failed");
                        }
                    } else {
                        viewModel.setShowQRCode(false);
                        viewModel.stopServer();
                        startStop.setText("Start");
                    }
                }
                case "startStopButtonEnabled" -> startStop.setEnabled((boolean) evt.getNewValue());
            }
        });
        panel.add(startStop);
        return panel;
    }

    private JPanel createVersionLabelPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel versionLabel = new JLabel(String.format("Version %s", VERSION));
        versionLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        panel.add(versionLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createIndicators() {
        JPanel indicatorsPanel = UIFactory.createFlowPanel();
        indicatorsPanel.add(createIndicator(Server.HOME));
        indicatorsPanel.add(createIndicator(Server.PREVIOUS));
        indicatorsPanel.add(createIndicator(Server.NEXT));
        indicatorsPanel.add(createIndicator(Server.END));
        indicatorsPanel.add(Box.createHorizontalStrut(16));
        indicatorsPanel.add(createIndicator(Server.HELLO));
        JPanel panel = new JPanel();
        panel.add(indicatorsPanel);
        return panel;
    }

    private JLabel createIndicator(String indicator) {
        Map<String, String> symbols = Map.of(
                Server.HOME, "\u23ee",
                Server.PREVIOUS, "\u25c0",
                Server.NEXT, "\u25b6",
                Server.END, "\u23ed",
                Server.HELLO, new String(Character.toChars(0x0001F44B))
        );
        JLabel label = new JLabel(symbols.get(indicator));
        viewModel.observeLastCommand(value -> label.setForeground(indicator.equals(value) ? Color.red : UIManager.getColor("Label.foreground")));
        return label;
    }

    private void showQRCode() {
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
        qrCodeDialog = dialog;
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
                UIManager.setLookAndFeel(new MaterialLookAndFeel(new CustomizedMaterialTheme()));
                Main ui = new Main();
                ui.setLocationRelativeTo(null);
                ui.setVisible(true);
            } catch (UnsupportedLookAndFeelException
                     | AWTException | SocketException e) {
                LOGGER.log(Level.SEVERE, "setLookAndFeel()", e);
            }
        });
    }
}
