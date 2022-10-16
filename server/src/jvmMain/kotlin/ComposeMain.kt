package eu.thomaskuenneth.souffleur

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import java.awt.AWTException
import java.net.SocketException
import java.util.*
import java.util.logging.Level
import java.util.prefs.Preferences
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

private const val KEY_SECRET = "secret"
private const val KEY_PORT = "port"

@Composable
fun IndicatorIcon(indicator: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        imageVector = when (indicator) {
            Server.HOME -> Icons.Default.Home
            Server.PREVIOUS -> Icons.Default.ArrowBack
            Server.NEXT -> Icons.Default.ArrowForward
            Server.END -> Icons.Default.ExitToApp
            else -> Icons.Default.Favorite
        },
        contentDescription = indicator,
        tint = if (isActive)
            MaterialTheme.colors.primary
        else
            MaterialTheme.colors.onBackground,
    )
}

@Composable
fun MainWindow(viewModel: ViewModel) {
    val device by viewModel.observeAsState<String>("device")
    val address by viewModel.observeAsState<String>("address")
    var port by remember { mutableStateOf(viewModel.port.toString()) }
    viewModel.observePort {
        port = it.toString()
    }
    //   val qrCodeVisible by viewModel.observeAsState<Boolean>("showQRCode")
    val lastCommand by viewModel.observeAsState<String?>("lastCommand")
    val isHomeActive by remember(lastCommand) { mutableStateOf(Server.HOME == lastCommand) }
    val isNextActive by remember(lastCommand) { mutableStateOf(Server.NEXT == lastCommand) }
    val isPreviousActive by remember(lastCommand) { mutableStateOf(Server.PREVIOUS == lastCommand) }
    val isEndActive by remember(lastCommand) { mutableStateOf(Server.END == lastCommand) }
    val isHelloActive by remember(lastCommand) { mutableStateOf(Server.HELLO == lastCommand) }
    val isRunning by viewModel.observeAsState<Boolean>("running")

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
                Button(
                    onClick = {},
                    modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = if (isRunning) "Stop" else "Start")
                }
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    IndicatorIcon(indicator = Server.HOME, isActive = isHomeActive)
                    IndicatorIcon(
                        indicator = Server.PREVIOUS,
                        isActive = isPreviousActive,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IndicatorIcon(
                        indicator = Server.NEXT,
                        isActive = isNextActive,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IndicatorIcon(
                        indicator = Server.END,
                        isActive = isEndActive,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IndicatorIcon(
                        indicator = Server.HELLO,
                        isActive = isHelloActive,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
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

fun main() {
    val prefs = Preferences.userNodeForPackage(SwingMain::class.java)
    var secret: String? = prefs.get(KEY_SECRET, null)
    if (secret == null) {
        secret = UUID.randomUUID().toString()
        prefs.put(KEY_SECRET, secret)
    }
    val viewModel = ViewModel()
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val ui = SwingMain(viewModel, prefs)
            setDeviceAndAddress(viewModel = viewModel)
            viewModel.isRunning = false
            viewModel.secret = secret
            viewModel.port = prefs.getInt(KEY_PORT, 8087)
            viewModel.isShowQRCode = false
            ui.setLocationRelativeTo(null)
            ui.pack()
            ui.isVisible = true
        } catch (e: UnsupportedLookAndFeelException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        } catch (e: AWTException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        } catch (e: IllegalAccessException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        } catch (e: SocketException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        } catch (e: InstantiationException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        } catch (e: ClassNotFoundException) {
            LOGGER.log(Level.SEVERE, "setLookAndFeel()", e)
        }
    }
    val icon = useResource("/eu/thomaskuenneth/souffleur/Icon-App-76x76@1x.png") {
        it.buffered().use { stream ->
            BitmapPainter(loadImageBitmap(stream))
        }
    }
    singleWindowApplication(
        title = "Souffleur",
        icon = icon
    ) {
        MainWindow(viewModel)
    }
}
