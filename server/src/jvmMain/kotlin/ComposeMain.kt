package eu.thomaskuenneth.souffleur

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import java.awt.Dimension
import java.net.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.prefs.Preferences

private const val KEY_SECRET = "secret"
private const val KEY_PORT = "port"

private val LOGGER = Logger.getLogger(SwingMain::class.java.name)

fun createComposeIndicator(
    indicator: String,
    viewModel: ViewModel
): ComposePanel {
    val panel = ComposePanel()
    panel.setContent {
        val state by remember {
            viewModel.observeAsState<String?>("lastCommand")
        }
        Icon(
            imageVector = when (indicator) {
                Server.HOME -> Icons.Default.Home
                Server.PREVIOUS -> Icons.Default.ArrowBack
                Server.NEXT -> Icons.Default.ArrowForward
                Server.END -> Icons.Default.ExitToApp
                else -> Icons.Default.Favorite
            },
            contentDescription = indicator,
            tint = if (state == indicator)
                MaterialTheme.colors.primary
            else
                MaterialTheme.colors.onBackground,
            modifier = Modifier.background(
                color = MaterialTheme.colors.background
            )
        )
    }
    panel.preferredSize = Dimension(24, 24)
    return panel
}

@Composable
@Preview
fun MainWindow() {
    val viewModel = ViewModel()
    val prefs = Preferences.userNodeForPackage(SwingMain::class.java)
    var secret: String? = prefs.get(KEY_SECRET, null)
    if (secret == null) {
        secret = UUID.randomUUID().toString()
        prefs.put(KEY_SECRET, secret)
    }
    setDeviceAndAddress(viewModel = viewModel)
    viewModel.isRunning = false
    viewModel.secret = secret
    viewModel.port = prefs.getInt(KEY_PORT, 8087)
    viewModel.isShowQRCode = false

    val device by viewModel.observeAsState<String>("device")
    val address by viewModel.observeAsState<String>("address")
    var port by remember { mutableStateOf(viewModel.port.toString()) }
    viewModel.observePort {
        port = it.toString()
    }
    var qrCodeVisible = viewModel.observeAsState<Boolean>("showQRCode")

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column {
                InfoText(
                    label = "Device", info = device
                )
                InfoText(
                    label = "Address", info = address,
                    modifier = Modifier.padding(top = 16.dp)
                )
                OutlinedTextField(value = port,
                    modifier = Modifier.padding(top = 16.dp),
                    onValueChange = { newValue ->
                        port = newValue.filter { it.isDigit() }
                    },
                    label = { Text(text = "Port") })
            }
        }
    }
}

@Composable
fun InfoText(label: String, info: String, modifier: Modifier = Modifier) {
    OutlinedTextField(modifier = modifier,
        value = info,
        onValueChange = {},
        readOnly = true,
        label = { Text(text = label) }
    )
}

// See https://stackoverflow.com/a/69160376
private fun setDeviceAndAddress(viewModel: ViewModel) {
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

fun main() {
    SwingMain.main(emptyArray())
    val icon = useResource("/eu/thomaskuenneth/souffleur/Icon-App-76x76@1x.png") {
        it.buffered().use { stream ->
            BitmapPainter(loadImageBitmap(stream))
        }
    }
    singleWindowApplication(
        title = "Souffleur",
        icon = icon
    ) {
        MainWindow()
    }
}
