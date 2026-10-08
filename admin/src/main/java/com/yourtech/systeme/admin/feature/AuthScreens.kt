package com.yourtech.systeme.admin.feature

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourtech.systeme.admin.auth.AuthException
import com.yourtech.systeme.admin.auth.LoginResult
import com.yourtech.systeme.admin.ui.AdminViewModel
import com.yourtech.systeme.admin.ui.Field
import com.yourtech.systeme.designsystem.component.BrandLockup
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.IconTile
import com.yourtech.systeme.designsystem.theme.YT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun AuthFrame(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(YT.Navy).statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        BrandLockup(Modifier.fillMaxWidth(0.72f))
        Spacer(Modifier.height(8.dp))
        Text("لوحة التحكم", style = MaterialTheme.typography.labelLarge, color = YT.Cyan)
        Spacer(Modifier.height(28.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            IconTile(Icons.Rounded.Lock)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted)
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "لا توجد كلمات مرور افتراضية. البيانات محفوظة على هذا الجهاز فقط ومحمية من لقطات الشاشة.",
            style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SetupOwnerScreen(vm: AdminViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AuthFrame("إنشاء حساب المالك", "أول تشغيل: أنشئ حساب المالك. يمكنك لاحقًا إضافة حسابات للموظفين والتقنيين.") {
        Field(username, { username = it.trim() }, "اسم المستخدم (لاتيني)", maxLength = 32)
        Field(name, { name = it }, "الاسم الظاهر", maxLength = 60)
        Field(pass, { pass = it }, "كلمة المرور", password = true, hint = "8 أحرف على الأقل مع حرف ورقم", maxLength = 128)
        Field(confirm, { confirm = it }, "تأكيد كلمة المرور", password = true, maxLength = 128)
        error?.let { Text(it, color = YT.Danger, style = MaterialTheme.typography.bodySmall) }
        GradientButton(if (busy) "جارٍ الإنشاء…" else "إنشاء الحساب والدخول", {
            if (busy) return@GradientButton
            if (pass != confirm) { error = "كلمتا المرور غير متطابقتين"; return@GradientButton }
            busy = true; error = null
            scope.launch {
                try { vm.auth.createOwner(username, name, pass) } catch (e: AuthException) { error = e.message }
                busy = false
            }
        }, Modifier.fillMaxWidth(), enabled = !busy)
    }
}

@Composable
fun LoginScreen(vm: AdminViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var lockedSeconds by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(lockedSeconds) { if (lockedSeconds > 0) { delay(1000); lockedSeconds-- } }
    AuthFrame("تسجيل الدخول", "أدخل بيانات حسابك للوصول إلى لوحة التحكم.") {
        Field(username, { username = it.trim() }, "اسم المستخدم", maxLength = 32)
        Field(pass, { pass = it }, "كلمة المرور", password = true, maxLength = 128)
        when {
            lockedSeconds > 0 -> Text("محاولات كثيرة خاطئة. أعد المحاولة بعد $lockedSeconds ثانية.", color = YT.Warning, style = MaterialTheme.typography.bodySmall)
            error != null -> Text(error!!, color = YT.Danger, style = MaterialTheme.typography.bodySmall)
        }
        GradientButton(if (busy) "جارٍ التحقق…" else "دخول", {
            if (busy || lockedSeconds > 0 || username.isBlank() || pass.isEmpty()) return@GradientButton
            busy = true; error = null
            scope.launch {
                when (val r = vm.auth.login(username, pass)) {
                    is LoginResult.Success -> pass = ""
                    LoginResult.WrongCredentials -> error = "اسم المستخدم أو كلمة المرور غير صحيحة"
                    is LoginResult.LockedOut -> lockedSeconds = r.secondsLeft
                }
                busy = false
            }
        }, Modifier.fillMaxWidth(), enabled = !busy && lockedSeconds == 0L)
    }
}

/** Used both for the forced first-login change and from Settings. */
@Composable
fun ChangePasswordForm(vm: AdminViewModel, onDone: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Field(current, { current = it }, "كلمة المرور الحالية", password = true, maxLength = 128)
    Field(new, { new = it }, "كلمة المرور الجديدة", password = true, hint = "8 أحرف على الأقل مع حرف ورقم", maxLength = 128)
    Field(confirm, { confirm = it }, "تأكيد كلمة المرور الجديدة", password = true, maxLength = 128)
    error?.let { Text(it, color = YT.Danger, style = MaterialTheme.typography.bodySmall) }
    GradientButton("تغيير كلمة المرور", {
        if (busy) return@GradientButton
        if (new != confirm) { error = "كلمتا المرور غير متطابقتين"; return@GradientButton }
        busy = true; error = null
        scope.launch {
            try { vm.auth.changePassword(current, new); vm.message("تم تغيير كلمة المرور"); onDone() } catch (e: AuthException) { error = e.message }
            busy = false
        }
    }, Modifier.fillMaxWidth(), enabled = !busy)
}

@Composable
fun ForcedPasswordChangeScreen(vm: AdminViewModel) {
    AuthFrame("تغيير كلمة المرور المؤقتة", "لأمان حسابك، اختر كلمة مرور جديدة قبل المتابعة.") {
        ChangePasswordForm(vm) {}
    }
}
