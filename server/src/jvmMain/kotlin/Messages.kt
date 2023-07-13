package eu.thomaskuenneth.souffleur

import java.util.ResourceBundle

private val resourceBundle = ResourceBundle
    .getBundle("eu.thomaskuenneth.souffleur.messages")

const val APP_NAME = "app.name"

const val BUTTON_START = "button.start"
const val BUTTON_STOP = "button.stop"

const val MENU_FILE = "menu.file"
const val MENU_ITEM_QUIT = "menu.item.quit"
const val MENU_HELP = "menu.help"
const val MENU_ITEM_ABOUT = "menu.item.about"

const val TITLE_ABOUT = "title.about"

const val SNACKBAR_OPEN_PRESENTATION = "snackbar.open_presentation"

fun stringResource(key: String): String {
    return resourceBundle.getString(key)
}
