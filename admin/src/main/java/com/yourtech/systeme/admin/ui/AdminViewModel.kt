package com.yourtech.systeme.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.admin.auth.AdminAuthRepository
import com.yourtech.systeme.admin.auth.AuthException
import com.yourtech.systeme.admin.data.AdminException
import com.yourtech.systeme.admin.data.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Activity-scoped entry point for every admin screen: data streams + guarded actions with user feedback. */
@HiltViewModel
class AdminViewModel @Inject constructor(val repo: AdminRepository, val auth: AdminAuthRepository) : ViewModel() {
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    val session = auth.session
    val accountCount: StateFlow<Int?> = auth.accountCount.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun <T> state(flow: kotlinx.coroutines.flow.Flow<T>, initial: T): StateFlow<T> = flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    /** Runs a guarded action; validation/permission errors become a snackbar, success optionally too. */
    fun run(success: String? = null, onDone: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                success?.let { _messages.send(it) }
                onDone()
            } catch (e: AdminException) {
                _messages.send(e.message.orEmpty())
            } catch (e: AuthException) {
                _messages.send(e.message.orEmpty())
            } catch (e: IllegalArgumentException) {
                _messages.send(e.message ?: "بيانات غير صالحة")
            }
        }
    }

    fun message(text: String) { viewModelScope.launch { _messages.send(text) } }
    fun logout() = auth.logout()
}
