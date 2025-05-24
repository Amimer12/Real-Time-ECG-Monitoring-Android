package dev.atick.compose.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.compose.ui.login.data.LoginState
import dev.atick.core.ui.BaseViewModel
import dev.atick.core.utils.Event
import dev.atick.compose.ui.utils.Property
import dev.atick.network.data.LoginRequest
import dev.atick.network.repository.CardiacZoneRepository
import dev.atick.storage.preferences.UserPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val cardiacZoneRepository: CardiacZoneRepository,
    private val userPreferences: UserPreferences
) : BaseViewModel() {
    val username = Property(mutableStateOf(""))
    val password = Property(mutableStateOf(""))
    val loginState = mutableStateOf(LoginState.LOGGED_OUT)

    private val _userId = MutableLiveData(Event("-1"))
    val userId: LiveData<Event<String>>
        get() = _userId

    init {
        viewModelScope.launch {
            userPreferences.getUserId().collect { id ->
                Logger.w("USER ID: $id")
                _userId.postValue(Event(id))
            }
        }
    }

    fun login() {
        loginState.value = LoginState.LOGGING_IN

        viewModelScope.launch {
            // Simulate a short delay (optional)
            kotlinx.coroutines.delay(1000)

            // Fake a user ID and mark login as successful
            val fakeUserId = "123456789"  // or any ID longer than 8 characters
            _userId.postValue(Event(fakeUserId))
            userPreferences.saveUserId(fakeUserId)
            loginState.value = LoginState.LOGIN_SUCCESSFUL
        }
    }

}