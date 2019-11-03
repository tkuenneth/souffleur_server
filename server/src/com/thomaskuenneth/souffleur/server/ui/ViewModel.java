package com.thomaskuenneth.souffleur.server.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewModel {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Boolean running = null;
    private String jsonFile = null;

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

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
