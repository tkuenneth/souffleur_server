package eu.thomaskuenneth.souffleur

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import eu.thomaskuenneth.souffleur.ViewModel.*
import java.awt.AWTException
import java.net.SocketException
import java.util.*
import java.util.logging.Level
import java.util.prefs.Preferences
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import kotlin.math.min


const val VERSION = "1.0.7"
const val KEY_SECRET = "secret"
const val KEY_PORT = "port"

private val prefs = Preferences.userNodeForPackage(SwingMain::class.java)

@Composable
fun IndicatorIcon(indicator: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        imageVector = when (indicator) {
            Server.HOME -> Icons.Default.FirstPage
            Server.PREVIOUS -> Icons.Default.NavigateBefore
            Server.NEXT -> Icons.Default.NavigateNext
            Server.END -> Icons.Default.LastPage
            else -> Icons.Default.Favorite
        },
        contentDescription = indicator,
        tint = if (isActive)
            MaterialTheme.colors.primary
        else
            MaterialTheme.colors.onBackground,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.MainScreen(viewModel: ViewModel, exit: () -> Unit) {
    val qrCodeVisible by viewModel.observeAsState<Boolean>(SHOW_QR_CODE)
    val showAboutDialog = remember { mutableStateOf(false) }
    MaterialTheme {
        if (!IS_MACOS) {
            MenuBar {
                Menu(text = stringResource(MENU_FILE)) {
                    Item(
                        text = stringResource(MENU_ITEM_QUIT),
                        onClick = exit,
                        shortcut = KeyShortcut(Key.F4, alt = true)
                    )
                }
                Menu(text = stringResource(MENU_HELP)) {
                    Item(
                        text = stringResource(MENU_ITEM_ABOUT),
                        onClick = {
                            showAboutDialog.value = true
                        }
                    )
                }
            }
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = qrCodeVisible) { isVisible ->
                when (isVisible) {
                    false -> MainControlsScreen(viewModel)
                    true -> QRCodeScreen(viewModel)
                }
            }
        }
        AboutDialog(showAboutDialog)
    }
}

@Composable
fun MainControlsScreen(viewModel: ViewModel) {
    val device by viewModel.observeAsState<String>(DEVICE)
    val address by viewModel.observeAsState<String>(ADDRESS)
    var portAsString by remember { mutableStateOf(Utils.nullSafeString(viewModel.port)) }
    viewModel.observePort { port ->
        portAsString = Utils.nullSafeString(port)
        viewModel.isStartStopButtonEnabled = port != null
        port?.let {
            prefs.putInt(KEY_PORT, it)
        }
    }
    val lastCommand by viewModel.observeAsState<String?>(LAST_COMMAND)
    val isRunning by viewModel.observeAsState<Boolean>(RUNNING)
    viewModel.observeRunning {
        viewModel.isShowQRCode = isRunning
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1.0F).padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            FirstColumn(
                device = device,
                address = address,
                port = portAsString,
                portEnabled = !isRunning
            ) { newValue ->
                with(newValue.filter { it.isDigit() }) {
                    if (isEmpty())
                        viewModel.port = null
                    else
                        viewModel.port = min(this.toInt(), 65535)
                }
            }
            SecondColumn(
                lastCommand = lastCommand,
                isRunning = isRunning,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                viewModel.isRunning = !isRunning
            }
            if (isRunning) {
                viewModel.startServer()
            } else {
                viewModel.stopServer()
            }
        }
    }
}

@Composable
fun QRCodeScreen(viewModel: ViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val qrCode = Utils.generateQRCode(viewModel.qrCodeAsString)
            Image(qrCode.toComposeImageBitmap(), null)
            Button(onClick = {
                viewModel.isRunning = false
            }) {
                Text(text = stringResource(BUTTON_STOP))
            }
        }
    }
}

@Composable
fun FirstColumn(
    device: String,
    address: String,
    port: String,
    portEnabled: Boolean,
    onPortChange: (String) -> Unit
) {
    Column {
        InfoText(
            label = DEVICE, info = device
        )
        InfoText(
            label = ADDRESS, info = address,
            modifier = Modifier.padding(top = 16.dp)
        )
        OutlinedTextField(
            value = port,
            modifier = Modifier.padding(top = 16.dp),
            onValueChange = onPortChange,
            label = { Text(text = "Port") },
            enabled = portEnabled
        )
    }
}

@Composable
fun RowScope.SecondColumn(
    modifier: Modifier = Modifier,
    lastCommand: String?,
    isRunning: Boolean,
    onStartStopClick: () -> Unit
) {
    val isHomeActive by remember(lastCommand) { mutableStateOf(Server.HOME == lastCommand) }
    val isNextActive by remember(lastCommand) { mutableStateOf(Server.NEXT == lastCommand) }
    val isPreviousActive by remember(lastCommand) { mutableStateOf(Server.PREVIOUS == lastCommand) }
    val isEndActive by remember(lastCommand) { mutableStateOf(Server.END == lastCommand) }
    val isHelloActive by remember(lastCommand) { mutableStateOf(Server.HELLO == lastCommand) }
    Column(modifier = modifier.weight(1.0f)) {
        Button(
            onClick = onStartStopClick,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(key = if (isRunning) BUTTON_STOP else BUTTON_START))
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
    var secret: String? = prefs.get(KEY_SECRET, null)
    if (secret == null) {
        secret = UUID.randomUUID().toString()
        prefs.put(KEY_SECRET, secret)
    }
    val viewModel = ViewModel()
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            // val ui = SwingMain(viewModel, prefs)
            setDeviceAndAddress(viewModel = viewModel)
            viewModel.isRunning = false
            viewModel.secret = secret
            viewModel.port = prefs.getInt(KEY_PORT, 8087)
            viewModel.isShowQRCode = false
//            ui.setLocationRelativeTo(null)
//            ui.pack()
//            ui.isVisible = true
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
    application {
        val icon = useResource("/eu/thomaskuenneth/souffleur/appicon.svg") {
            it.buffered().use { stream ->
                loadSvgPainter(stream, LocalDensity.current)
            }
        }
        val exit = {
            viewModel.stopServer()
            exitApplication()
        }
        Window(
            onCloseRequest = exit,
            title = stringResource(key = APP_NAME),
            icon = icon,
            resizable = false,
            state = WindowState(size = DpSize(600.dp, 340.dp)),
        ) {
            MainScreen(viewModel, exit)
        }
    }
}

@Composable
fun getAppIcon() = useResource("/eu/thomaskuenneth/souffleur/appicon.svg") {
    it.buffered().use { stream ->
        loadSvgPainter(stream, LocalDensity.current)
    }
}
