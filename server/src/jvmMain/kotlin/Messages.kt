package eu.thomaskuenneth.souffleur

import java.util.*

private val resourceBundle = ResourceBundle
    .getBundle("eu.thomaskuenneth.souffleur.messages")

const val APP_NAME = "app.name"
const val BUTTON_START = "button.start"
const val BUTTON_STOP = "button.stop"

fun stringResource(key: String): String {
    return resourceBundle.getString(key)
}
