package cd.ghost.safebox.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cd.ghost.safebox.R
import cd.ghost.safebox.core.Resources
import cd.ghost.safebox.domain.AppSettings
import cd.ghost.safebox.domain.LiveEvent
import cd.ghost.safebox.domain.MutableLiveEvent
import cd.ghost.safebox.domain.publish
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val resources: Resources
) : ViewModel() {


    private val _event = MutableLiveEvent<Event>()
    val event: LiveEvent<Event> = _event

    private var passwordValue: String = ""
    var turnCount = 0

    fun changePassword(newPassword: String) {
        if (turnCount == 0) {
            this.passwordValue = newPassword
            turnCount++
            _event.publish(Event.RepeatPassword(resources(R.string.repeat_again)))
            return
        }
        if (this.passwordValue == newPassword) {
            viewModelScope.launch {
                appSettings.savePassword(passwordValue)
                turnCount = 0
                _event.publish(Event.NavigateToPasswordScreen)
            }
        } else {
            _event.publish(Event.Message(resources(R.string.wrong_password)))
            turnCount = 0
        }
    }

    sealed class Event {
        object NavigateToPasswordScreen : Event()
        class RepeatPassword(val message: String) : Event()
        class Message(val message: String) : Event()
    }

}