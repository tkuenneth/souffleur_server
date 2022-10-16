package eu.thomaskuenneth.souffleur;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class SwingMain extends JFrame {

    public static final String VERSION = "1.0.6";

    private static final Logger LOGGER = Logger.getLogger(SwingMain.class.getName());
    private static final String KEY_SECRET = "secret";
    private static final String KEY_PORT = "port";

    private final ViewModel viewModel;

    private JFrame qrCodeFrame;

    private final Preferences prefs;

    public SwingMain(ViewModel viewModel, Preferences prefs) throws AWTException, SocketException {
        super("Souffleur");
        this.viewModel = viewModel;
        this.prefs = prefs;
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
        setContentPane(createMainPanel());
        viewModel.observeShowQRCode(value -> {
            if (value)
                showQRCode();
            else
                hideQRCode();
        });
    }

    private JComponent createMainPanel() {
        Box mainPanel = new Box(BoxLayout.PAGE_AXIS);
        mainPanel.setAlignmentX(CENTER_ALIGNMENT);
        List<Component> updates = new ArrayList<>();
        updates.add(mainPanel.add(createConnectionInfo()));
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

    private JPanel createConnectionInfo() {
        JPanel panel = UIFactory.createFlowPanel(8);
        JTextField device = UIFactory.createNonEditableTextField();
        viewModel.observeDevice((device::setText));
        panel.add(device);
        JTextField address = UIFactory.createNonEditableTextField();
        viewModel.observeAddress(address::setText);
        panel.add(address);
        JTextComponent port = UIFactory.createIntegerField(0, 65535);
        viewModel.observePort(value -> {
            port.setText(Utils.nullSafeString(value));
            viewModel.setStartStopButtonEnabled(value != null);
            if (value != null)
                prefs.putInt(KEY_PORT, value);
        });
        port.addPropertyChangeListener(evt -> {
            try {
                viewModel.setPort(Integer.parseInt(port.getText()));
            } catch (NumberFormatException ex) {
                viewModel.setPort(null);
            }
        });
        panel.add(port);
        viewModel.observeRunning(value -> port.setEditable(!value));
        return panel;
    }

    private JPanel createStartStopButtonPanel() {
        JPanel panel = new JPanel();
        JButton startStop = new JButton();
        startStop.addActionListener(e -> viewModel.setRunning(!viewModel.isRunning()));
        viewModel.observeRunning(value -> {
            viewModel.setShowQRCode(value);
            if (value) {
                if (viewModel.startServer()) {
                    startStop.setText("Stop");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not start server",
                            "Souffleur",
                            JOptionPane.WARNING_MESSAGE);
                    viewModel.setRunning(false);
                    LOGGER.log(Level.SEVERE, "startServer() failed");
                }
            } else {
                viewModel.stopServer();
                startStop.setText("Start");
            }
        });
        viewModel.observeStartStopButtonEnabled(startStop::setEnabled);
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
        JPanel indicatorsPanel = UIFactory.createFlowPanel(24);
        indicatorsPanel.add(createIndicator(Server.HOME));
        indicatorsPanel.add(createIndicator(Server.PREVIOUS));
        indicatorsPanel.add(createIndicator(Server.NEXT));
        indicatorsPanel.add(createIndicator(Server.END));
        indicatorsPanel.add(createIndicator(Server.HELLO));
        JPanel panel = new JPanel();
        panel.add(indicatorsPanel);
        return panel;
    }

    private JLabel createIndicator(String indicator) {
        Map<String, String> symbols = Map.of(
                Server.HOME, "|<",
                Server.PREVIOUS, "<",
                Server.NEXT, ">",
                Server.END, ">|",
                Server.HELLO, ";-)");
        JLabel label = new JLabel(symbols.get(indicator));
        viewModel.observeLastCommand(value -> label.setForeground(indicator.equals(value) ? Color.red
                : UIManager.getColor("Label.foreground")));
        return label;
    }

    private void showQRCode() {
        hideQRCode();
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        BufferedImage image = Utils.generateQRCode(viewModel.getQRCodeAsString());
        ImageIcon imageIcon = new ImageIcon(image);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JLabel(imageIcon), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton close = new JButton("Stop");
        close.addActionListener(e -> viewModel.setRunning(false));
        buttonPanel.add(close);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        qrCodeFrame = frame;
    }

    private void hideQRCode() {
        if (qrCodeFrame != null) {
            if (qrCodeFrame.isVisible()) {
                qrCodeFrame.setVisible(false);
                qrCodeFrame.dispose();
            }
            qrCodeFrame = null;
        }
    }
}
