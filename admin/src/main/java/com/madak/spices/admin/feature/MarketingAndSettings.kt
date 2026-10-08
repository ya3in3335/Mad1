package com.madak.spices.admin.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.admin.auth.AdminAuthRepository
import com.madak.spices.admin.auth.AdminUser
import com.madak.spices.admin.auth.PasswordHasher
import com.madak.spices.admin.ui.AdminCard
import com.madak.spices.admin.ui.LabeledValue
import com.madak.spices.admin.ui.adminDate
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.repository.NotificationRepository
import com.madak.spices.data.repository.OfferRepository
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MarketingViewModel @Inject constructor(
    private val offers: OfferRepository,
    private val notifications: NotificationRepository,
) : ViewModel() {
    val allOffers: StateFlow<List<OfferEntity>> = offers.all.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sent: StateFlow<List<NotificationEntity>> = notifications.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setActive(id: String, active: Boolean) = viewModelScope.launch { offers.setActive(id, active) }
    fun delete(id: String) = viewModelScope.launch { offers.delete(id) }
    fun saveOffer(offer: OfferEntity) = viewModelScope.launch { offers.save(offer) }
    fun broadcast(title: String, body: String, titleFr: String, bodyFr: String) =
        viewModelScope.launch { notifications.broadcast(title, body, titleFr, bodyFr, NotificationEntity.TYPE_OFFER) }
}

@Composable
fun OffersAdminScreen(viewModel: MarketingViewModel = hiltViewModel()) {
    val offers by viewModel.allOffers.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(offers, key = { it.id }) { o ->
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(o.colorArgb), MadakColors.Ink))).padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(o.titleAr, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(o.subtitleAr, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            val meta = listOfNotNull(
                                o.discountPercent.takeIf { it > 0 }?.let { "-$it%" },
                                o.promoCode?.let { "الرمز: $it" },
                            ).joinToString(" • ")
                            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.labelLarge, color = MadakColors.GoldSoft)
                        }
                        IconButton(onClick = { viewModel.delete(o.id) }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = Color.White) }
                        Switch(o.isActive, { viewModel.setActive(o.id, it) })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("عرض جديد") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
    if (adding) {
        var title by remember { mutableStateOf("") }
        var subtitle by remember { mutableStateOf("") }
        var percent by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("عرض جديد") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, singleLine = true)
                    OutlinedTextField(subtitle, { subtitle = it }, label = { Text("الوصف") })
                    OutlinedTextField(percent, { percent = it.filter(Char::isDigit).take(2) }, label = { Text("نسبة الخصم %") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(code, { code = it.uppercase().filter { c -> c.isLetterOrDigit() } }, label = { Text("رمز ترويجي (اختياري)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = title.isNotBlank(), onClick = {
                    viewModel.saveOffer(
                        OfferEntity(
                            id = "offer_" + System.currentTimeMillis(), titleAr = title.trim(), titleFr = "", subtitleAr = subtitle.trim(), subtitleFr = "",
                            discountPercent = percent.toIntOrNull() ?: 0, promoCode = code.ifBlank { null }, colorArgb = 0xFFD6247CL,
                        )
                    )
                    adding = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
fun NotificationsAdminScreen(viewModel: MarketingViewModel = hiltViewModel()) {
    val sent by viewModel.sent.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var titleFr by remember { mutableStateOf("") }
    var bodyFr by remember { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AdminCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("إرسال إشعار للزبائن", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان (عربي)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(body, { body = it }, label = { Text("النص (عربي)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(titleFr, { titleFr = it }, label = { Text("Titre (FR)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(bodyFr, { bodyFr = it }, label = { Text("Message (FR)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        onClick = { viewModel.broadcast(title.trim(), body.trim(), titleFr.trim(), bodyFr.trim()); title = ""; body = ""; titleFr = ""; bodyFr = "" },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Icon(Icons.AutoMirrored.Rounded.Send, null); Spacer(Modifier.width(6.dp)); Text("إرسال") }
                    Text(
                        "يُحفظ الإشعار محلياً حالياً، ويُرسل لجميع الأجهزة بعد ربط خدمة FCM / Supabase.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item { Text("السجل", style = MaterialTheme.typography.titleMedium) }
        items(sent, key = { it.id }) { n ->
            AdminCard {
                Column {
                    Text(n.titleAr, style = MaterialTheme.typography.titleSmall)
                    Text(n.bodyAr, style = MaterialTheme.typography.bodyMedium)
                    Text(adminDate(n.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Settings / security
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(private val auth: AdminAuthRepository) : ViewModel() {
    suspend fun changePassword(current: String, new: String) = auth.changePassword(current, new)
    fun logout() = auth.logout()
}

@Composable
fun AdminSettingsScreen(user: AdminUser, viewModel: AdminSettingsViewModel = hiltViewModel()) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminCard {
            Column {
                Text("الحساب", style = MaterialTheme.typography.titleMedium)
                LabeledValue("الاسم", user.name)
                LabeledValue("الدور", user.role.labelAr)
                LabeledValue("الصلاحيات", user.role.permissions.size.toString() + " / 9")
            }
        }
        AdminCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("تغيير كلمة المرور", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(current, { current = it }, label = { Text("كلمة المرور الحالية") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(new, { new = it }, label = { Text("كلمة المرور الجديدة") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Button(enabled = current.isNotEmpty() && new.isNotEmpty(), onClick = {
                    scope.launch {
                        message = when {
                            !PasswordHasher.isStrongEnough(new) -> "8 أحرف على الأقل مع حروف وأرقام"
                            viewModel.changePassword(current, new) -> { current = ""; new = ""; "تم تغيير كلمة المرور ✓" }
                            else -> "كلمة المرور الحالية غير صحيحة"
                        }
                    }
                }) { Text("تحديث") }
            }
        }
        AdminCard {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.Security, null, tint = MadakColors.Success, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "كلمات المرور مخزنة بتشفير PBKDF2 مع ملح عشوائي، قفل تلقائي بعد 5 دقائق في الخلفية، " +
                        "حظر مؤقت بعد المحاولات الخاطئة، ومنع لقطات الشاشة. للإنتاج: اربط Supabase Auth أو Firebase Auth مع صلاحيات على الخادم.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MadakColors.Paprika)
            Spacer(Modifier.width(6.dp))
            Text("تسجيل الخروج", color = MadakColors.Paprika)
        }
    }
}
