package eu.thomaskuenneth.souffleur;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

public class ViewModel {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Boolean running = null;
    private String device = null;
    private String address = null;
    private Integer port = null;
    private Boolean startStopButtonEnabled = null;
    private Boolean showQRCode = null;

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

    public Boolean isShowQRCode() {
        return showQRCode;
    }

    public void setShowQRCode(Boolean newShowQRCode) {
        Boolean oldShowQRCode = this.showQRCode;
        this.showQRCode = newShowQRCode;
        pcs.firePropertyChange("showQRCode", oldShowQRCode, newShowQRCode);
    }

    public void startServer() throws IOException {
        server.start(getAddress(), getPort());
    }

    public void stopServer() {
        server.stop();
    }

    public String getQRCodeAsString() {
        return server.getQRCodeAsString();
    }

    private void updateStartStopButtonBeEnabled() {
        setStartStopButtonEnabled(port != null);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
