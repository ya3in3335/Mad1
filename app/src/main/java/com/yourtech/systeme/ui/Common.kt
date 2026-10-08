package com.yourtech.systeme.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.Money
import com.yourtech.systeme.data.model.PropertyType
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.StockStatus
import com.yourtech.systeme.data.model.directionsUrl
import com.yourtech.systeme.data.model.navigationUri
import com.yourtech.systeme.data.model.whatsappUrl
import com.yourtech.systeme.designsystem.theme.YT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val LocalLanguage = staticCompositionLocalOf { AppLanguage.ARABIC }
val LocalSnackbar = staticCompositionLocalOf { SnackbarHostState() }

@Composable @ReadOnlyComposable
fun money(dzd: Int) = Money.format(dzd, LocalLanguage.current)

@get:StringRes val RequestStatus.label: Int get() = when (this) {
    RequestStatus.SUBMITTED -> R.string.status_SUBMITTED
    RequestStatus.UNDER_REVIEW -> R.string.status_UNDER_REVIEW
    RequestStatus.QUOTATION_PREPARED -> R.string.status_QUOTATION_PREPARED
    RequestStatus.APPROVED -> R.string.status_APPROVED
    RequestStatus.SCHEDULED -> R.string.status_SCHEDULED
    RequestStatus.IN_PROGRESS -> R.string.status_IN_PROGRESS
    RequestStatus.COMPLETED -> R.string.status_COMPLETED
    RequestStatus.CANCELLED -> R.string.status_CANCELLED
}

val RequestStatus.color: Color get() = when (this) {
    RequestStatus.SUBMITTED -> YT.Cyan
    RequestStatus.UNDER_REVIEW -> YT.Violet
    RequestStatus.QUOTATION_PREPARED -> YT.Warning
    RequestStatus.APPROVED -> YT.Blue
    RequestStatus.SCHEDULED -> Color(0xFF38BDF8)
    RequestStatus.IN_PROGRESS -> Color(0xFFF97316)
    RequestStatus.COMPLETED -> YT.Success
    RequestStatus.CANCELLED -> YT.Danger
}

@get:StringRes val RequestType.label: Int get() = when (this) {
    RequestType.INSTALLATION -> R.string.type_INSTALLATION
    RequestType.MAINTENANCE -> R.string.type_MAINTENANCE
    RequestType.CONSULTATION -> R.string.type_CONSULTATION
    RequestType.QUOTE -> R.string.type_QUOTE
}

@get:StringRes val PropertyType.label: Int get() = when (this) {
    PropertyType.HOUSE -> R.string.property_HOUSE
    PropertyType.APARTMENT -> R.string.property_APARTMENT
    PropertyType.SHOP -> R.string.property_SHOP
    PropertyType.OFFICE -> R.string.property_OFFICE
    PropertyType.WAREHOUSE -> R.string.property_WAREHOUSE
    PropertyType.FACTORY -> R.string.property_FACTORY
    PropertyType.OTHER -> R.string.property_OTHER
}

@get:StringRes val StockStatus.label: Int get() = when (this) {
    StockStatus.IN_STOCK -> R.string.stock_in
    StockStatus.LOW_STOCK -> R.string.stock_low
    StockStatus.OUT_OF_STOCK -> R.string.stock_out
    StockStatus.ON_ORDER -> R.string.stock_order
    StockStatus.UNKNOWN -> R.string.stock_unknown
}

val StockStatus.color: Color get() = when (this) {
    StockStatus.IN_STOCK -> YT.Success
    StockStatus.LOW_STOCK -> YT.Warning
    StockStatus.OUT_OF_STOCK -> YT.Danger
    StockStatus.ON_ORDER -> YT.Cyan
    StockStatus.UNKNOWN -> YT.TextMuted
}

fun formatDate(millis: Long, language: AppLanguage, withTime: Boolean = false): String =
    SimpleDateFormat(if (withTime) "EEE d MMM yyyy • HH:mm" else "EEE d MMM yyyy", Locale.forLanguageTag(language.localeTag)).format(Date(millis))

/** Weekday name for java.util.Calendar day constants. */
fun dayName(day: Int, language: AppLanguage): String =
    java.text.DateFormatSymbols(Locale.forLanguageTag(language.localeTag)).weekdays[day]

/** Contact intents that never crash when no app can handle them. */
object Launcher {
    private fun open(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.no_app, Toast.LENGTH_SHORT).show(); false
    }

    fun call(context: Context, phoneInternational: String) = open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:+${phoneInternational.filter(Char::isDigit)}")))
    fun whatsapp(context: Context, info: BusinessInfoEntity, message: String = "") = open(context, Intent(Intent.ACTION_VIEW, Uri.parse(info.whatsappUrl(message))))
    fun web(context: Context, url: String) = open(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    fun directions(context: Context, info: BusinessInfoEntity) {
        val nav = Intent(Intent.ACTION_VIEW, Uri.parse(info.navigationUri())).setPackage("com.google.android.apps.maps")
        if (nav.resolveActivity(context.packageManager) != null) open(context, nav) else web(context, info.directionsUrl())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}
