package eu.thomaskuenneth.souffleur

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import eu.thomaskuenneth.souffleur.souffleur_server.generated.resources.Res
import eu.thomaskuenneth.souffleur.souffleur_server.generated.resources.about_title
import eu.thomaskuenneth.souffleur.souffleur_server.generated.resources.app_name
import eu.thomaskuenneth.souffleur.souffleur_server.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutDialog(onCloseRequest: () -> Unit) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        icon = painterResource(Res.drawable.logo),
        resizable = false,
        title = stringResource(Res.string.about_title)
    ) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                null,
                modifier = Modifier.requiredSize(96.dp)
            )
            Text(
                text = stringResource(Res.string.app_name),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = VERSION
            )
        }
    }
}
