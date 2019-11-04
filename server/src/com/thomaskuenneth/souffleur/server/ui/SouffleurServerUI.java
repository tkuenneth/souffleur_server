package com.thomaskuenneth.souffleur.server.ui;

import com.thomaskuenneth.souffleur.server.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SouffleurServerUI extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(SouffleurServerUI.class.getName());

    private final ViewModel viewModel;
    private final Map<String, List<String>> devices;

    public SouffleurServerUI() throws AWTException, SocketException {
        super("Souffleur");
        setResizable(false);
        viewModel = new ViewModel();
        devices = Utils.getIpAddress();
        setContentPane(createMainPanel());
        viewModel.setDevice(devices.keySet().iterator().next());
        viewModel.setPort(8087);
        viewModel.setRunning(false);
        pack();
    }

    private JComponent createMainPanel() {
        Box mainPanel = new Box(BoxLayout.PAGE_AXIS);
        List<Component> updates = new ArrayList<>();
        updates.add(mainPanel.add(createJsonFileSelector()));
        updates.add(mainPanel.add(createDeviceSelector()));
        mainPanel.add(Box.createVerticalGlue());
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
        return mainPanel;
    }

    private JPanel createJsonFileSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JTextField filename = new JTextField(30);
        panel.add(filename);
        JButton choose = new JButton("Choose");
        choose.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            fc.addChoosableFileFilter(new JsonFileFilter());
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                viewModel.setJsonFile(fc.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(choose);
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "jsonFile":
                    filename.setText(evt.getNewValue().toString());
                    break;
                case "running":
                    boolean running = (boolean) evt.getNewValue();
                    choose.setEnabled(!running);
                    filename.setEditable(!running);
                    break;
            }
        });
        return panel;
    }

    private JPanel createDeviceSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        String[] names = devices.keySet().toArray(new String[]{});
        JComboBox<String> comboBox = new JComboBox<>(names);
        comboBox.addItemListener(e -> viewModel.setDevice((String) e.getItem()));
        panel.add(comboBox);
        JLabel label = new JLabel();
        panel.add(label);
        JTextField port = new JTextField(4);
        panel.add(port);
        viewModel.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "device":
                    String device = evt.getNewValue().toString();
                    viewModel.setAddress(devices.get(device).get(0));
                    break;
                case "address":
                    label.setText(viewModel.getAddress());
                    break;
                case "port":
                    port.setText(evt.getNewValue().toString());
                    break;
                case "running":
                    boolean running = (boolean) evt.getNewValue();
                    comboBox.setEnabled(!running);
                    port.setEditable(!running);
                    break;
            }
        });
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        JButton startStop = new JButton();
        startStop.addActionListener(e -> viewModel.setRunning(!viewModel.isRunning()));
        viewModel.addPropertyChangeListener(evt -> {
            if ("running".equals(evt.getPropertyName())) {
                boolean running = (boolean) evt.getNewValue();
                if (running) {
                    try {
                        viewModel.startServer();
                        startStop.setText("Stop");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "startServer()", e);
                        viewModel.setRunning(false);
                    }
                } else {
                    viewModel.stopServer();
                    startStop.setText("Start");
                }
            }
        });
        panel.add(startStop);
        return panel;
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
                    | AWTException | SocketException e) {
                LOGGER.log(Level.SEVERE, "setLookAndFeel()", e);
            }
        });
    }
}
