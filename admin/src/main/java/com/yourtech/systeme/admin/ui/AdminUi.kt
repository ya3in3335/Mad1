package com.yourtech.systeme.admin.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.yourtech.systeme.data.model.PropertyType
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.StockStatus
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// ---- Arabic labels -------------------------------------------------------------------------------

val RequestStatus.ar: String get() = when (this) {
    RequestStatus.SUBMITTED -> "جديد"
    RequestStatus.UNDER_REVIEW -> "قيد الدراسة"
    RequestStatus.QUOTATION_PREPARED -> "عرض السعر جاهز"
    RequestStatus.APPROVED -> "موافق عليه"
    RequestStatus.SCHEDULED -> "مُجدول"
    RequestStatus.IN_PROGRESS -> "قيد التنفيذ"
    RequestStatus.COMPLETED -> "مكتمل"
    RequestStatus.CANCELLED -> "ملغى"
}

val RequestStatus.tint: Color get() = when (this) {
    RequestStatus.SUBMITTED -> YT.Cyan
    RequestStatus.UNDER_REVIEW, RequestStatus.QUOTATION_PREPARED -> YT.Warning
    RequestStatus.APPROVED, RequestStatus.SCHEDULED -> YT.Blue
    RequestStatus.IN_PROGRESS -> YT.Violet
    RequestStatus.COMPLETED -> YT.Success
    RequestStatus.CANCELLED -> YT.Danger
}

val RequestType.ar: String get() = when (this) {
    RequestType.INSTALLATION -> "تركيب"
    RequestType.MAINTENANCE -> "صيانة"
    RequestType.CONSULTATION -> "استشارة"
    RequestType.QUOTE -> "طلب عرض سعر"
}

val PropertyType.ar: String get() = when (this) {
    PropertyType.HOUSE -> "منزل"
    PropertyType.APARTMENT -> "شقة"
    PropertyType.SHOP -> "محل"
    PropertyType.OFFICE -> "مكتب"
    PropertyType.WAREHOUSE -> "مستودع"
    PropertyType.FACTORY -> "مصنع"
    PropertyType.OTHER -> "أخرى"
}

val StockStatus.ar: String get() = when (this) {
    StockStatus.IN_STOCK -> "متوفر"
    StockStatus.LOW_STOCK -> "كمية محدودة"
    StockStatus.OUT_OF_STOCK -> "غير متوفر"
    StockStatus.ON_ORDER -> "حسب الطلب"
    StockStatus.UNKNOWN -> "غير محدد"
}

val StockStatus.tint: Color get() = when (this) {
    StockStatus.IN_STOCK -> YT.Success
    StockStatus.LOW_STOCK -> YT.Warning
    StockStatus.OUT_OF_STOCK -> YT.Danger
    StockStatus.ON_ORDER -> YT.Blue
    StockStatus.UNKNOWN -> YT.TextMuted
}

private val arLocale: Locale = Locale.forLanguageTag("ar-DZ-u-nu-latn")
fun dateAr(millis: Long, withTime: Boolean = false): String =
    SimpleDateFormat(if (withTime) "EEE d MMM yyyy • HH:mm" else "EEE d MMM yyyy", arLocale).format(Date(millis))

/** Wraps LTR values (phones, usernames, references) so RTL text does not reorder them. */
fun ltr(text: String) = "⁦$text⁩"

object AdminLauncher {
    private fun open(context: Context, intent: Intent) = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "لا يوجد تطبيق لفتح هذا الإجراء", Toast.LENGTH_SHORT).show()
    }

    fun call(context: Context, phone: String) = open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    fun whatsapp(context: Context, phone: String, text: String) {
        val intl = com.yourtech.systeme.data.model.Validation.toInternational(phone) ?: phone.filter(Char::isDigit)
        open(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$intl?text=" + Uri.encode(text))))
    }
}

// ---- Layout helpers ------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "رجوع") } },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = YT.Navy, titleContentColor = YT.White),
    )
}

/** Scrollable editor page with a pinned save button. */
@Composable
fun EditorPage(title: String, onBack: () -> Unit, saveText: String = "حفظ", onSave: () -> Unit, extra: @Composable () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(YT.Navy).imePadding()) {
        AdminTopBar(title, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
            extra()
            Spacer(Modifier.height(8.dp))
        }
        GradientButton(saveText, onSave, Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp))
    }
}

@Composable
fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = YT.Cyan, unfocusedBorderColor = YT.Outline, focusedLabelColor = YT.Cyan, cursorColor = YT.Cyan,
    focusedContainerColor = YT.Surface, unfocusedContainerColor = YT.Surface,
)

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    hint: String? = null,
    password: Boolean = false,
    maxLength: Int = 2000,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onChange(it) },
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        supportingText = hint?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = adminFieldColors(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = if (enabled) YT.White else YT.TextMuted)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
        }
        Switch(checked, onChange, enabled = enabled, colors = SwitchDefaults.colors(checkedTrackColor = YT.Blue, checkedThumbColor = YT.White))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceRow(label: String, options: List<T>, selected: T?, text: (T) -> String, onSelect: (T) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = YT.TextMuted)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { Chip(text(it), it == selected, { onSelect(it) }) }
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = YT.Cyan)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Two-step delete without a dialog window: first tap arms, second tap (within 4 s) confirms. */
@Composable
fun DeleteButton(text: String = "حذف", onConfirm: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    var armed by remember { mutableStateOf(false) }
    LaunchedEffect(armed) { if (armed) { delay(4_000); armed = false } }
    OutlineButton(
        if (armed) "اضغط مجددًا لتأكيد $text" else text,
        { if (armed) { armed = false; onConfirm() } else armed = true },
        modifier, icon = Icons.Rounded.DeleteOutline, tint = YT.Danger,
    )
}

@Composable
fun PickImageButton(text: String = "اختيار صورة من المعرض", onPicked: (Uri) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) onPicked(uri) }
    OutlineButton(text, { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.AddPhotoAlternate)
}

@Composable
fun ListCard(onClick: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(
        modifier.fillMaxWidth().let { if (onClick != null) it.pressable(haptic = false, onClick = onClick) else it },
        padding = PaddingValues(14.dp), content = content,
    )
}

@Composable
fun Dot(color: Color) = androidx.compose.foundation.layout.Box(Modifier.size(8.dp).background(color, MaterialTheme.shapes.extraSmall))

@Composable
fun Gap(w: Int) = Spacer(Modifier.width(w.dp))
