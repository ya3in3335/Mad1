package com.madak.spices.admin.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.model.OrderStatus
import com.madak.spices.data.model.Pricing
import com.madak.spices.designsystem.theme.MadakColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun dzd(value: Int) = Pricing.format(value, AppLanguage.ARABIC)

fun adminDate(millis: Long): String = SimpleDateFormat("d MMM • HH:mm", Locale.forLanguageTag("ar-DZ-u-nu-latn")).format(Date(millis))

val OrderStatus.labelAr: String
    get() = when (this) {
        OrderStatus.NEW -> "جديد"
        OrderStatus.CONFIRMED -> "مؤكد"
        OrderStatus.PREPARING -> "قيد التحضير"
        OrderStatus.OUT_FOR_DELIVERY -> "في الطريق"
        OrderStatus.DELIVERED -> "تم التوصيل"
        OrderStatus.CANCELLED -> "ملغى"
    }

/** Label of the button that moves an order to this status. */
val OrderStatus.actionAr: String
    get() = when (this) {
        OrderStatus.CONFIRMED -> "تأكيد الطلب"
        OrderStatus.PREPARING -> "بدء التحضير"
        OrderStatus.OUT_FOR_DELIVERY -> "إرسال للتوصيل"
        OrderStatus.DELIVERED -> "تم التسليم"
        OrderStatus.CANCELLED -> "إلغاء"
        OrderStatus.NEW -> "جديد"
    }

val OrderStatus.tint: Color
    get() = when (this) {
        OrderStatus.NEW -> MadakColors.Magenta
        OrderStatus.CONFIRMED -> MadakColors.Info
        OrderStatus.PREPARING -> MadakColors.Warning
        OrderStatus.OUT_FOR_DELIVERY -> MadakColors.Cinnamon
        OrderStatus.DELIVERED -> MadakColors.Success
        OrderStatus.CANCELLED -> MadakColors.Paprika
    }

@Composable
fun StatCard(title: String, value: Int, icon: ImageVector, color: Color, modifier: Modifier = Modifier, money: Boolean = false) {
    val animated by animateIntAsState(value, tween(900), label = "stat")
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.height(12.dp))
            Text(if (money) dzd(animated) else animated.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp, modifier = modifier.fillMaxWidth()) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(110.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
