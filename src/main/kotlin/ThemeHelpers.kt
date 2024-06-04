package eu.thomaskuenneth.souffleur

import androidx.compose.runtime.*
import com.github.tkuenneth.nativeparameterstoreaccess.Dconf
import com.github.tkuenneth.nativeparameterstoreaccess.Dconf.HAS_DCONF
import com.github.tkuenneth.nativeparameterstoreaccess.MacOSDefaults
import com.github.tkuenneth.nativeparameterstoreaccess.NativeParameterStoreAccess.IS_WINDOWS
import com.github.tkuenneth.nativeparameterstoreaccess.WindowsRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun isSystemInDarkTheme(): Boolean {
    var toggle by remember { mutableStateOf(true) }
    val result by remember(toggle) { mutableStateOf(getFromNativeParameterStore()) }
    LaunchedEffect(result) {
        launch {
            while (isActive) {
                delay(1000)
                if (getFromNativeParameterStore() != result) {
                    toggle = !toggle
                }
            }
        }
    }
    return result
}

private fun getFromNativeParameterStore() = when {
    IS_WINDOWS -> {
        val result = WindowsRegistry.getWindowsRegistryEntry(
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
            "AppsUseLightTheme"
        )
        result == 0x0
    }

    IS_MACOS -> {
        val result = MacOSDefaults.getDefaultsEntry("AppleInterfaceStyle")
        result == "Dark"
    }

    HAS_DCONF -> {
        val result = Dconf.getDconfEntry("/org/gnome/desktop/interface/gtk-theme")
        result.lowercase(Locale.getDefault()).contains("dark")
    }

    else -> false
}
