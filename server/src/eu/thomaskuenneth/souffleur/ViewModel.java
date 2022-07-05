package eu.thomaskuenneth.souffleur;

import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

public class ViewModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Boolean running = null;
    private String device = null;
    private static final String ADDRESS = "address";
    private String address = null;
    private Integer port = null;
    private Boolean startStopButtonEnabled = null;

    private static final String SHOW_QR_CODE = "showQRCode";
    private Boolean showQRCode = null;
    private static final String LAST_COMMAND = "lastCommand";
    private String lastCommand = null;
    private String secret = null;

    private Thread indicatorThread = null;

    private final Server server;

    public ViewModel() throws AWTException {
        server = new Server(command -> {
            if (Server.HELLO.equals(command)) setShowQRCode(false);
            if (indicatorThread != null)
                indicatorThread.interrupt();
            indicatorThread = new Thread(() -> {
                SwingUtilities.invokeLater(() -> setLastCommand(command));
                try {
                    Thread.sleep(2000);
                    SwingUtilities.invokeLater(() -> setLastCommand(null));
                } catch (InterruptedException e) {
                    // no action required nor wanted
                }
            });
            indicatorThread.start();
        });
    }

    public void setSecret(String newSecret) {
        String oldSecret = this.secret;
        this.secret = newSecret;
        pcs.firePropertyChange("secret", oldSecret, newSecret);
    }

    public String getSecret() {
        return secret;
    }

    public Boolean isRunning() {
        return running;
    }

    public void setLastCommand(String newLastCommand) {
        String oldLastCommand = getLastCommand();
        this.lastCommand = newLastCommand;
        pcs.firePropertyChange(LAST_COMMAND, oldLastCommand, newLastCommand);
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void observeLastCommand(Consumer<String> callback) {
        observe(LAST_COMMAND, callback);
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
        String oldDevice = getDevice();
        this.device = newDevice;
        pcs.firePropertyChange("device", oldDevice, newDevice);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        String oldAddress = getAddress();
        this.address = newAddress;
        pcs.firePropertyChange(ADDRESS, oldAddress, newAddress);
        updateStartStopButtonBeEnabled();
    }

    public void observeAddress(Consumer<String> callback) {
        observe(ADDRESS, callback);
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
        Boolean oldStartStopButtonEnabled = isStartStopButtonEnabled();
        this.startStopButtonEnabled = newStartStopButtonEnabled;
        pcs.firePropertyChange("startStopButtonEnabled", oldStartStopButtonEnabled, newStartStopButtonEnabled);
    }

    public Boolean isShowQRCode() {
        return showQRCode;
    }

    public void setShowQRCode(Boolean newShowQRCode) {
        Boolean oldShowQRCode = isShowQRCode();
        this.showQRCode = newShowQRCode;
        pcs.firePropertyChange(SHOW_QR_CODE, oldShowQRCode, newShowQRCode);
    }

    public void observeShowQRCode(Consumer<Boolean> callback) {
        observe(SHOW_QR_CODE, callback);
    }

    private <T> void observe(String propertyName, Consumer<T> callback) {
        pcs.addPropertyChangeListener(propertyName, evt -> {
            if (propertyName.equals(evt.getPropertyName())) {
                callback.accept((T) evt.getNewValue());
            }
        });
    }

    public boolean startServer() {
        return server.start(getAddress(), getPort(), getSecret());
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
