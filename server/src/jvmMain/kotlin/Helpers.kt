package eu.thomaskuenneth.souffleur

import java.net.*
import java.util.logging.Level
import java.util.logging.Logger

val LOGGER: Logger = Logger.getLogger(SwingMain::class.java.name)

// See https://stackoverflow.com/a/69160376
fun setDeviceAndAddress(viewModel: ViewModel) {
    try {
        DatagramSocket().use { s ->
            val remoteAddress = InetAddress.getByName("a.root-servers.net")
            if (remoteAddress != null) {
                s.connect(remoteAddress, 80)
                viewModel.address = s.localAddress.hostAddress
                viewModel.device = NetworkInterface.getByInetAddress(s.localAddress).displayName
            }
        }
    } catch (e: UnknownHostException) {
        LOGGER.log(Level.SEVERE, null, e)
    } catch (e: SocketException) {
        LOGGER.log(Level.SEVERE, null, e)
    }
}
