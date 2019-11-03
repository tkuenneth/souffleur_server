package com.thomaskuenneth.souffleur.server.ui;

import com.thomaskuenneth.souffleur.server.Server;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SouffleurServerUI extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(SouffleurServerUI.class.getName());

    private final ViewModel viewModel;
    private final Server server;

    public SouffleurServerUI() throws AWTException {
        super("Souffleur");
        viewModel = new ViewModel();
        server = new Server();
        setContentPane(createContentPane());
        viewModel.setRunning(false);
        pack();
    }

    private JPanel createContentPane() {
        JPanel cp = new JPanel(new BorderLayout());
        cp.add(createMainPanel(), BorderLayout.CENTER);
        cp.add(createButtonPanel(), BorderLayout.SOUTH);
        return cp;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 8, 9));
        mainPanel.add(createJsonFileSelector());
        mainPanel.add(createDeviceSelector());
        mainPanel.add(createPortSelector());
        return mainPanel;
    }

    private JPanel createJsonFileSelector() {
        JPanel p = new JPanel(new BorderLayout());
        JTextField tf = new JTextField(40);
        p.add(tf, BorderLayout.CENTER);
        JButton b = new JButton("Choose");
        b.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            fc.addChoosableFileFilter(new JsonFileFilter());
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                viewModel.setJsonFile(fc.getSelectedFile().getAbsolutePath());
            }
        });
        p.add(b, BorderLayout.EAST);
        viewModel.addPropertyChangeListener(evt -> {
            if ("jsonFile".equals(evt.getPropertyName())) {
                tf.setText(evt.getNewValue().toString());
            }
        });
        return p;
    }

    private JPanel createDeviceSelector() {
        JPanel p = new JPanel();
        JComboBox<String> devices = new JComboBox<>();
        p.add(devices);
        return p;
    }

    private JPanel createPortSelector() {
        JPanel p = new JPanel();
        return p;
    }

    private JPanel createButtonPanel() {
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 8));
        bp.add(createStartStopButton());
        return bp;
    }

    private JButton createStartStopButton() {
        JButton b = new JButton();
        b.addActionListener(e -> {
            viewModel.setRunning(!viewModel.isRunning());
        });
        viewModel.addPropertyChangeListener(evt -> {
            if ("running".equals(evt.getPropertyName())) {
                if (Boolean.TRUE.equals(evt.getNewValue())) {
                    b.setText("Stop");
                } else {
                    b.setText("Start");
                }
            }
        });
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SouffleurServerUI ui = new SouffleurServerUI();
                ui.setLocationRelativeTo(null);
                ui.setVisible(true);
            } catch (ClassNotFoundException | InstantiationException
                    | IllegalAccessException | UnsupportedLookAndFeelException
                    | AWTException e) {
                LOGGER.log(Level.SEVERE, "setLookAndFeel()", e);
            }
        });
    }
}
