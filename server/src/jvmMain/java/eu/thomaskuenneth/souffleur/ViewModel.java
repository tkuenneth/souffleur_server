package eu.thomaskuenneth.souffleur;

import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

public class ViewModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static final String RUNNING = "running";
    private Boolean running = null;

    public static final String DEVICE = "device";
    private String device = null;

    public static final String ADDRESS = "address";
    private String address = null;

    public static final String PORT = "port";
    private Integer port = null;

    public static final String START_STOP_BUTTON_ENABLED = "startStopButtonEnabled";
    private Boolean startStopButtonEnabled = null;

    public static final String SHOW_QR_CODE = "showQRCode";
    private Boolean showQRCode = null;

    public static final String LAST_COMMAND = "lastCommand";
    private String lastCommand = null;

    public static final String SECRET = "secret";
    private String secret = null;

    private Thread indicatorThread = null;

    private final Server server;

    public ViewModel() throws AWTException {
        server = new Server(command -> {
            setShowQRCode(false);
            if (indicatorThread != null) {
                indicatorThread.interrupt();
            }
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

    public String getSecret() {
        return secret;
    }

    public void setSecret(String newSecret) {
        String oldSecret = this.secret;
        this.secret = newSecret;
        pcs.firePropertyChange(SECRET, oldSecret, newSecret);
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String newLastCommand) {
        String oldLastCommand = getLastCommand();
        this.lastCommand = newLastCommand;
        pcs.firePropertyChange(LAST_COMMAND, oldLastCommand, newLastCommand);
    }

    public void observeLastCommand(Consumer<String> callback) {
        observe(LAST_COMMAND, callback);
    }

    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean newRunning) {
        Boolean oldRunning = this.running;
        this.running = newRunning;
        pcs.firePropertyChange(RUNNING, oldRunning, newRunning);
    }

    public void observeRunning(Consumer<Boolean> callback) {
        observe(RUNNING, callback);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String newDevice) {
        String oldDevice = getDevice();
        this.device = newDevice;
        pcs.firePropertyChange(DEVICE, oldDevice, newDevice);
    }

    public void observeDevice(Consumer<String> callback) {
        observe(DEVICE, callback);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        String oldAddress = getAddress();
        this.address = newAddress;
        pcs.firePropertyChange(ADDRESS, oldAddress, newAddress);
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
        pcs.firePropertyChange(PORT, oldPort, newPort);
    }

    public void observePort(Consumer<Integer> callback) {
        observe(PORT, callback);
    }

    public Boolean isStartStopButtonEnabled() {
        return startStopButtonEnabled;
    }

    public void setStartStopButtonEnabled(Boolean newStartStopButtonEnabled) {
        Boolean oldStartStopButtonEnabled = isStartStopButtonEnabled();
        this.startStopButtonEnabled = newStartStopButtonEnabled;
        pcs.firePropertyChange(START_STOP_BUTTON_ENABLED, oldStartStopButtonEnabled, newStartStopButtonEnabled);
    }

    public void observeStartStopButtonEnabled(Consumer<Boolean> callback) {
        observe(START_STOP_BUTTON_ENABLED, callback);
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

    public String getQRCodeAsString() {
        return String.format("https://%s:%s/souffleur/%s/", getAddress(), getPort(), getSecret());
    }

    public boolean startServer() {
        return server.isStarted() || server.start(getAddress(), getPort(), getSecret());
    }

    public void stopServer() {
        server.stop();
    }

    @SuppressWarnings("unchecked")
    private <T> void observe(String propertyName, Consumer<T> callback) {
        pcs.addPropertyChangeListener(propertyName, evt -> {
            if (propertyName.equals(evt.getPropertyName())) {
                callback.accept((T) evt.getNewValue());
            }
        });
    }
}
