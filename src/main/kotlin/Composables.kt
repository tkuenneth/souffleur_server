package eu.thomaskuenneth.souffleur

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onBackground,
    )
}
