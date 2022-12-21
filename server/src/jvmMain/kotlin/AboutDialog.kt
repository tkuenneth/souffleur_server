package eu.thomaskuenneth.souffleur

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AboutDialog(showAboutDialog: MutableState<Boolean>) {
    if (showAboutDialog.value) {
        Dialog(
            onCloseRequest = { showAboutDialog.value = false },
            icon = getAppIcon(),
            resizable = false,
            title = stringResource(TITLE_ABOUT)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = getAppIcon(),
                    null,
                    modifier = Modifier.requiredSize(96.dp)
                )
                Text(
                    text = stringResource(APP_NAME),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = VERSION
                )
            }
        }
    }
}
