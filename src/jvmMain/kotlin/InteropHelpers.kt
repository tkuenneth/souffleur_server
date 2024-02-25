package eu.thomaskuenneth.souffleur

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.beans.PropertyChangeSupport


inline fun <reified T> Any.observeAsState(propertyName: String): State<T> {
    val property = this.javaClass.getDeclaredField(propertyName)
    property.trySetAccessible()
    val clazz = property.type
    if (T::class.java != clazz) throw IllegalArgumentException("Types do not match")
    val current = property.get(this) as T
    val state = mutableStateOf(current)
    val fields = this.javaClass.declaredFields
    for (field in fields) {
        if (field.type == PropertyChangeSupport::class.java) {
            val pcs = this.javaClass.getDeclaredField(field.name)
            pcs.trySetAccessible()
            val instance = pcs.get(this) as PropertyChangeSupport
            instance.addPropertyChangeListener(propertyName) {
                state.value = it.newValue as T
            }
            break
        }
    }
    return state
}
