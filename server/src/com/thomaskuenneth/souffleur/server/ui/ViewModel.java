package com.thomaskuenneth.souffleur.server.ui;

import com.thomaskuenneth.souffleur.server.Server;
import com.thomaskuenneth.souffleur.server.Utils;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

public class ViewModel {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Boolean running = null;
    private String jsonFile = null;
    private String device = null;
    private String address = null;
    private Integer port = null;
    private Boolean startStopButtonEnabled = null;

    private final Server server;

    public ViewModel() throws AWTException {
        server = new Server();
    }

    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean newRunning) {
        Boolean oldRunning = this.running;
        this.running = newRunning;
        pcs.firePropertyChange("running", oldRunning, newRunning);
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String newJsonFile) {
        String oldJsonFile = this.jsonFile;
        this.jsonFile = newJsonFile;
        pcs.firePropertyChange("jsonFile", oldJsonFile, newJsonFile);
        updateStartStopButtonBeEnabled();
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String newDevice) {
        String oldDevice = this.device;
        this.device = newDevice;
        pcs.firePropertyChange("device", oldDevice, newDevice);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        String oldAddress = this.address;
        this.address = newAddress;
        pcs.firePropertyChange("address", oldAddress, newAddress);
        updateStartStopButtonBeEnabled();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer newPort) {
        Integer oldPort = this.port;
        this.port = newPort;
        pcs.firePropertyChange("port", oldPort, newPort);
        updateStartStopButtonBeEnabled();
    }

    public Boolean isStartStopButtonEnabled() {
        return startStopButtonEnabled;
    }

    public void setStartStopButtonEnabled(Boolean newStartStopButtonEnabled) {
        Boolean oldStartStopButtonEnabled = this.startStopButtonEnabled;
        this.startStopButtonEnabled = newStartStopButtonEnabled;
        pcs.firePropertyChange("startStopButtonEnabled", oldStartStopButtonEnabled, newStartStopButtonEnabled);
    }

    public void startServer(Runnable callback) throws IOException {
        server.start(getJsonFile(), getAddress(), getPort(), callback);
    }

    public void stopServer() {
        server.stop();
    }

    public String getQRCodeAsString() {
        return server.getQRCodeAsString();
    }

    private void updateStartStopButtonBeEnabled() {
        boolean enabled = false;
        if (port != null) {
            File f = new File(Utils.nullSafeString(jsonFile));
            enabled = f.exists() && f.isFile();
        }
        setStartStopButtonEnabled(enabled);
    }

    // ---------------------------------------------------------------------------------------------------------------

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
