package cd.ghost.safebox.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class Event<T>(
    value: T
) {
    private var _value: T? = value
    fun get(): T? = _value.also { _value = null }
}

typealias LiveEvent<T> = LiveData<Event<T>>
typealias MutableLiveEvent<T> = MutableLiveData<Event<T>>

fun <T> MutableLiveEvent<T>.publish(value: T) {
    this.value = Event(value)
}

suspend fun <T> Flow<Event<T>>.collectEvents(
    catch: ((Throwable) -> Unit)? = null,
    block: suspend (T) -> Unit
) {
    catch { if (catch != null) catch(it) }
    collect { event ->
        event.get()?.let {
            block(it)
        }
    }
}

@Composable
fun <T> LiveData<Event<T>>.observeEvent(): T? = observeAsState().value?.get()

@Composable
fun <T> LiveData<Event<T>>.LaunchEvent(block: (T) -> Unit) {
    observeAsState().value?.get()?.let {
        block(it)
    }
}

