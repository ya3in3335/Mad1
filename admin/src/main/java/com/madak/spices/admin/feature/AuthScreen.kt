package com.madak.spices.admin.feature

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.admin.auth.AdminAuthRepository
import com.madak.spices.admin.auth.LoginResult
import com.madak.spices.admin.auth.PasswordHasher
import com.madak.spices.designsystem.component.MadakWordmark
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AdminAuthRepository) : ViewModel() {
    val configured: StateFlow<Boolean?> = auth.isConfigured.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun setup(name: String, password: String, confirm: String) {
        error.value = when {
            name.isBlank() -> "أدخل اسم المسؤول"
            !PasswordHasher.isStrongEnough(password) -> "كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل مع حروف وأرقام"
            password != confirm -> "كلمتا المرور غير متطابقتين"
            else -> null
        }
        if (error.value != null) return
        viewModelScope.launch {
            busy.value = true
            runCatching { auth.createOwner(name, password) }.onFailure { error.value = "تعذر إنشاء الحساب" }
            busy.value = false
        }
    }

    fun login(password: String) = viewModelScope.launch {
        busy.value = true
        error.value = when (val r = auth.login(password)) {
            is LoginResult.Success -> null
            LoginResult.WrongPassword -> "كلمة المرور غير صحيحة"
            is LoginResult.LockedOut -> "محاولات كثيرة. أعد المحاولة بعد ${r.secondsLeft} ثانية"
        }
        busy.value = false
    }
}

@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val configured by viewModel.configured.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(MadakColors.Ink).statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(40.dp))
        MadakWordmark(Modifier.width(200.dp))
        Spacer(Modifier.height(12.dp))
        Text("لوحة التحكم", style = MaterialTheme.typography.titleLarge, color = MadakColors.Gold)
        Spacer(Modifier.height(36.dp))
        val isSetup = configured == false
        AnimatedContent(configured, label = "auth") { state ->
            if (state == null) {
                CircularProgressIndicator(color = MadakColors.Magenta)
                return@AnimatedContent
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isSetup) "مرحباً! أنشئ حساب المالك لتأمين لوحة التحكم. لا توجد كلمة مرور افتراضية."
                    else "أدخل كلمة المرور للمتابعة",
                    color = MadakColors.Beige, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(18.dp))
                if (isSetup) {
                    DarkField(name, { name = it }, "اسم المسؤول")
                }
                PasswordField(password, { password = it }, "كلمة المرور")
                if (isSetup) PasswordField(confirm, { confirm = it }, "تأكيد كلمة المرور")
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFFF8A70), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { if (isSetup) viewModel.setup(name, password, confirm) else viewModel.login(password) },
                    enabled = !busy && password.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    if (busy) CircularProgressIndicator(Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp, color = Color.White)
                    else {
                        Icon(Icons.Rounded.Lock, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isSetup) "إنشاء الحساب" else "دخول")
                    }
                }
            }
        }
    }
}

@Composable
private fun DarkField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value, onChange, label = { Text(label) }, singleLine = true,
        colors = darkFieldColors(), shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value, onChange, label = { Text(label) }, singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = MadakColors.Beige)
            }
        },
        colors = darkFieldColors(), shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = MadakColors.Magenta, unfocusedBorderColor = MadakColors.Smoke,
    focusedLabelColor = MadakColors.Gold, unfocusedLabelColor = MadakColors.Beige.copy(alpha = 0.7f),
    cursorColor = MadakColors.Magenta,
)
