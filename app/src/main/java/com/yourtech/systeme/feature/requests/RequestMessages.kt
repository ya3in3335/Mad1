package com.yourtech.systeme.feature.requests

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.ServiceRequestEntity
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.Wilayas
import com.yourtech.systeme.ui.formatDate
import com.yourtech.systeme.ui.label
import java.io.File

/** Human-readable request summary sent to the company on WhatsApp (no secrets, only form data). */
fun requestMessage(context: Context, r: ServiceRequestEntity, systemName: String?, language: AppLanguage): String = buildString {
    appendLine("🛡️ " + context.getString(R.string.wa_request_header))
    appendLine("${context.getString(R.string.wa_ref)}: ${r.reference}")
    appendLine("• ${context.getString(r.type.label)}")
    appendLine("• ${context.getString(R.string.field_name)}: ${r.customerName}")
    appendLine("• ${context.getString(R.string.field_phone)}: ${r.phone}")
    appendLine("• ${context.getString(R.string.field_wilaya)}: ${Wilayas.fromStorage(r.wilaya)?.label(language) ?: r.wilaya} — ${r.commune}")
    if (r.address.isNotBlank()) appendLine("• ${context.getString(R.string.field_address)}: ${r.address}")
    r.propertyType?.let { appendLine("• ${context.getString(R.string.field_property)}: ${context.getString(it.label)}") }
    systemName?.let { appendLine("• ${context.getString(R.string.field_system)}: $it") }
    r.deviceCount?.let { appendLine("• ${context.getString(R.string.field_devices)}: $it") }
    if (r.problemDescription.isNotBlank()) appendLine("• ${context.getString(R.string.field_problem)}: ${r.problemDescription}")
    r.preferredDate?.let { appendLine("• ${context.getString(R.string.field_date)}: ${formatDate(it, language)}") }
    if (r.notes.isNotBlank()) appendLine("• ${context.getString(R.string.field_notes)}: ${r.notes}")
}.trim()

/** Shares the request photos (app-private files via FileProvider) to WhatsApp or any app. */
fun sharePhotos(context: Context, paths: List<String>, text: String) {
    val uris = paths.map { FileProvider.getUriForFile(context, context.packageName + ".files", File(it)) }
    if (uris.isEmpty()) return
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/jpeg"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
