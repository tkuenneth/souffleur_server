package com.thomaskuenneth.souffleur.server.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewModel {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Boolean running = null;
    private String jsonFile = null;
    private String device = null;
    private String address = null;
    private String port = null;

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
    }

    public String getPort() {
        return port;
    }

    public void setPort(String newPort) {
        String oldPort = this.port;
        this.port = newPort;
        pcs.firePropertyChange("port", oldPort, newPort);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
