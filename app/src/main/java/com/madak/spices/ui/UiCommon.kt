package com.madak.spices.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.madak.spices.R
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.model.OrderStatus
import com.madak.spices.data.model.Pricing
import com.madak.spices.data.model.StoreInfo
import com.madak.spices.designsystem.theme.MadakColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ARABIC }

@Composable
@ReadOnlyComposable
fun price(dzd: Int): String = Pricing.format(dzd, LocalAppLanguage.current)

@get:StringRes
val OrderStatus.labelRes: Int
    get() = when (this) {
        OrderStatus.NEW -> R.string.status_new
        OrderStatus.CONFIRMED -> R.string.status_confirmed
        OrderStatus.PREPARING -> R.string.status_preparing
        OrderStatus.OUT_FOR_DELIVERY -> R.string.status_out_for_delivery
        OrderStatus.DELIVERED -> R.string.status_delivered
        OrderStatus.CANCELLED -> R.string.status_cancelled
    }

@get:StringRes
val OrderStatus.descriptionRes: Int
    get() = when (this) {
        OrderStatus.NEW -> R.string.status_new_desc
        OrderStatus.CONFIRMED -> R.string.status_confirmed_desc
        OrderStatus.PREPARING -> R.string.status_preparing_desc
        OrderStatus.OUT_FOR_DELIVERY -> R.string.status_out_for_delivery_desc
        OrderStatus.DELIVERED -> R.string.status_delivered_desc
        OrderStatus.CANCELLED -> R.string.tracking_cancelled_note
    }

val OrderStatus.color: Color
    get() = when (this) {
        OrderStatus.NEW -> MadakColors.Magenta
        OrderStatus.CONFIRMED -> MadakColors.Info
        OrderStatus.PREPARING -> MadakColors.Warning
        OrderStatus.OUT_FOR_DELIVERY -> MadakColors.Cinnamon
        OrderStatus.DELIVERED -> MadakColors.Success
        OrderStatus.CANCELLED -> MadakColors.Paprika
    }

fun formatDateTime(millis: Long, language: AppLanguage): String =
    SimpleDateFormat("d MMM yyyy • HH:mm", Locale.forLanguageTag(language.localeTag)).format(Date(millis))

fun formatTime(millis: Long, language: AppLanguage): String =
    SimpleDateFormat("d MMM • HH:mm", Locale.forLanguageTag(language.localeTag)).format(Date(millis))

/** Contact intents that never crash when no app can handle them. */
object Launcher {
    private fun open(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun call(context: Context, phone: String = StoreInfo.phoneDisplay) =
        open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))

    fun whatsapp(context: Context, message: String = "") =
        open(context, Intent(Intent.ACTION_VIEW, Uri.parse(StoreInfo.whatsappUrl(message))))

    fun web(context: Context, url: String) =
        open(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun tiktok(context: Context) =
        open(context, Intent(Intent.ACTION_VIEW, Uri.parse(StoreInfo.tiktokUrl)))

    fun instagram(context: Context) =
        open(context, Intent(Intent.ACTION_VIEW, Uri.parse(StoreInfo.instagramUrl)))

    fun email(context: Context) =
        open(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${StoreInfo.EMAIL}")))

    fun maps(context: Context) {
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse(StoreInfo.mapsUri))
        if (geo.resolveActivity(context.packageManager) != null) open(context, geo)
        else open(context, Intent(Intent.ACTION_VIEW, Uri.parse(StoreInfo.mapsWebUrl)))
    }
}
