package com.madak.spices.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.madak.spices.designsystem.theme.MadakColors
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.data.settings.SettingsRepository
import com.madak.spices.designsystem.splash.MadakMotionSplash
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SplashState(val ready: Boolean = false, val onboardingDone: Boolean = false, val sound: Boolean = true)

@HiltViewModel
class SplashViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    val state: StateFlow<SplashState> = combine(settings.onboardingDone, settings.splashSoundEnabled) { done, sound ->
        SplashState(ready = true, onboardingDone = done, sound = sound)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SplashState())
}

@Composable
fun SplashScreen(onFinished: (onboardingDone: Boolean) -> Unit, viewModel: SplashViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Wait for preferences (a few ms) so the sound setting is honoured from the first frame.
    if (!state.ready) {
        Box(Modifier.fillMaxSize().background(MadakColors.Ink))
        return
    }
    MadakMotionSplash(
        playSound = state.sound,
        onFinished = { onFinished(viewModel.state.value.onboardingDone) },
    )
}
