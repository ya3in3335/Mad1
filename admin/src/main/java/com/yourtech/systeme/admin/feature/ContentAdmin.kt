package com.yourtech.systeme.admin.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourtech.systeme.admin.auth.AdminPermission
import com.yourtech.systeme.admin.ui.AdminTopBar
import com.yourtech.systeme.admin.ui.AdminViewModel
import com.yourtech.systeme.admin.ui.ChoiceRow
import com.yourtech.systeme.admin.ui.DeleteButton
import com.yourtech.systeme.admin.ui.EditorPage
import com.yourtech.systeme.admin.ui.Field
import com.yourtech.systeme.admin.ui.FormSection
import com.yourtech.systeme.admin.ui.ListCard
import com.yourtech.systeme.admin.ui.PickImageButton
import com.yourtech.systeme.admin.ui.SwitchRow
import com.yourtech.systeme.admin.ui.dateAr
import com.yourtech.systeme.data.local.entity.PromotionEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity
import com.yourtech.systeme.data.media.ImageImporter
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.BusinessHours
import com.yourtech.systeme.data.model.displayPhone
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.theme.YT
import java.util.Calendar
import kotlinx.coroutines.launch

@Composable
fun ContentHubScreen(vm: AdminViewModel, onOpen: (String) -> Unit) {
    val user by vm.session.collectAsStateWithLifecycle()
    HubScreen(
        "المحتوى",
        listOf(
            Triple("العروض الترويجية", Icons.Rounded.Campaign, "promotions") to AdminPermission.PROMOTIONS,
            Triple("آراء العملاء", Icons.Rounded.RateReview, "testimonials") to AdminPermission.TESTIMONIALS,
            Triple("معلومات الشركة", Icons.Rounded.Business, "business") to AdminPermission.BUSINESS_INFO,
        ).filter { user?.can(it.second) == true }.map { it.first },
        onOpen,
    )
}

@Composable
private fun ListScaffold(title: String, onBack: () -> Unit, addText: String, onAdd: () -> Unit, header: @Composable () -> Unit = {}, body: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Scaffold(containerColor = YT.Navy, floatingActionButton = {
        ExtendedFloatingActionButton(onAdd, containerColor = YT.Blue, contentColor = YT.White, modifier = Modifier.navigationBarsPadding()) {
            Icon(Icons.Rounded.Add, null); Spacer(Modifier.size(6.dp)); Text(addText)
        }
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp)) {
            item { AdminTopBar(title, onBack); header() }
            body()
        }
    }
}

// ---- Promotions ----------------------------------------------------------------------------------

@Composable
fun PromotionsAdminScreen(vm: AdminViewModel, onBack: () -> Unit, onEdit: (Long?) -> Unit) {
    val list by remember { vm.state(vm.repo.promotions, emptyList()) }.collectAsStateWithLifecycle()
    ListScaffold("العروض الترويجية", onBack, "عرض جديد", { onEdit(null) }) {
        if (list.isEmpty()) item { EmptyState(Icons.Rounded.Campaign, "لا توجد عروض", "أضف عرضًا حقيقيًا ليظهر في الصفحة الرئيسية.") }
        items(list, key = { it.id }) { p ->
            val now = System.currentTimeMillis()
            val live = p.isActive && p.startsAt <= now && (p.endsAt == null || p.endsAt!! > now)
            ListCard({ onEdit(p.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.title, style = MaterialTheme.typography.titleSmall)
                        Text(p.endsAt?.let { "حتى ${dateAr(it)}" } ?: "بدون تاريخ نهاية", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                    }
                    StatusPill(if (live) "نشط" else "متوقف", if (live) YT.Success else YT.TextMuted)
                }
            }
        }
    }
}

@Composable
fun PromotionEditorScreen(vm: AdminViewModel, id: Long?, onBack: () -> Unit) {
    val list by remember { vm.state(vm.repo.promotions, emptyList()) }.collectAsStateWithLifecycle()
    val base = list.firstOrNull { it.id == id } ?: if (id == null) PromotionEntity(title = "") else null
    if (base == null) { Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("عرض", onBack) }; return }
    var p by remember(base.id) { mutableStateOf(base) }
    val scope = rememberCoroutineScope()
    EditorPage(if (id == null) "عرض جديد" else "تعديل عرض", onBack, onSave = { vm.run("تم الحفظ", onBack) { vm.repo.savePromotion(p) } }) {
        FormSection("العرض") {
            Field(p.title, { p = p.copy(title = it) }, "العنوان *", maxLength = 100)
            Field(p.body, { p = p.copy(body = it) }, "التفاصيل", singleLine = false, minLines = 3, maxLength = 500)
            MediaImage(p.imagePath, "shield", Modifier.size(width = 220.dp, height = 120.dp).clip(RoundedCornerShape(14.dp)))
            PickImageButton { uri ->
                scope.launch {
                    when (val r = vm.repo.importImage(uri, "promotions")) {
                        is ImageImporter.Result.Ok -> { if (p.imagePath != base.imagePath) vm.repo.deleteImage(p.imagePath); p = p.copy(imagePath = r.path) }
                        else -> vm.message(importMessage(r).orEmpty())
                    }
                }
            }
        }
        FormSection("المدة") {
            ChoiceRow("ينتهي بعد", listOf(0, 7, 14, 30, 60), when (val e = p.endsAt) { null -> 0; else -> ((e - p.startsAt) / 86_400_000L).toInt() },
                { if (it == 0) "بدون نهاية" else "$it يومًا" }) { d -> p = p.copy(endsAt = if (d == 0) null else p.startsAt + d * 86_400_000L) }
            SwitchRow("مفعّل", p.isActive, { p = p.copy(isActive = it) })
        }
        if (id != null) DeleteButton("حذف العرض", { vm.run("تم الحذف", onBack) { vm.repo.deletePromotion(base) } })
    }
}

// ---- Testimonials --------------------------------------------------------------------------------

@Composable
fun TestimonialsAdminScreen(vm: AdminViewModel, onBack: () -> Unit, onEdit: (Long?) -> Unit) {
    val list by remember { vm.state(vm.repo.testimonials, emptyList()) }.collectAsStateWithLifecycle()
    ListScaffold("آراء العملاء", onBack, "إضافة رأي", { onEdit(null) }, header = {
        Text("لا يظهر أي رأي للعملاء إلا بعد التحقق منه. لا تضف آراء غير حقيقية.", style = MaterialTheme.typography.bodySmall, color = YT.Warning, modifier = Modifier.padding(horizontal = 16.dp))
    }) {
        if (list.isEmpty()) item { EmptyState(Icons.Rounded.RateReview, "لا توجد آراء", "أضف آراء العملاء الحقيقية بعد موافقتهم.") }
        items(list, key = { it.id }) { t ->
            ListCard({ onEdit(t.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${t.authorName} • ${"★".repeat(t.rating)}", style = MaterialTheme.typography.titleSmall)
                        Text(t.text, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 2)
                    }
                    StatusPill(if (t.isVerified) "موثّق" else "غير موثّق", if (t.isVerified) YT.Success else YT.Warning)
                }
            }
        }
    }
}

@Composable
fun TestimonialEditorScreen(vm: AdminViewModel, id: Long?, onBack: () -> Unit) {
    val list by remember { vm.state(vm.repo.testimonials, emptyList()) }.collectAsStateWithLifecycle()
    val base = list.firstOrNull { it.id == id } ?: if (id == null) TestimonialEntity(authorName = "", text = "") else null
    if (base == null) { Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("رأي", onBack) }; return }
    var t by remember(base.id) { mutableStateOf(base) }
    EditorPage(if (id == null) "رأي جديد" else "تعديل رأي", onBack, onSave = { vm.run("تم الحفظ", onBack) { vm.repo.saveTestimonial(t) } }) {
        FormSection("الرأي") {
            Field(t.authorName, { t = t.copy(authorName = it) }, "الاسم كما وافق العميل على ظهوره *", maxLength = 60)
            Field(t.text, { t = t.copy(text = it) }, "النص *", singleLine = false, minLines = 3, maxLength = 600)
            ChoiceRow("التقييم", (1..5).toList(), t.rating, { "★".repeat(it) }) { t = t.copy(rating = it) }
            SwitchRow("تم التحقق (عميل حقيقي وموافق على النشر)", t.isVerified, { t = t.copy(isVerified = it) })
        }
        if (id != null) DeleteButton("حذف", { vm.run("تم الحذف", onBack) { vm.repo.deleteTestimonial(base) } })
    }
}

// ---- Business info -------------------------------------------------------------------------------

private val weekDays = listOf(Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
private fun dayAr(d: Int) = when (d) {
    Calendar.SATURDAY -> "السبت"; Calendar.SUNDAY -> "الأحد"; Calendar.MONDAY -> "الاثنين"; Calendar.TUESDAY -> "الثلاثاء"
    Calendar.WEDNESDAY -> "الأربعاء"; Calendar.THURSDAY -> "الخميس"; else -> "الجمعة"
}

private fun parseHhmm(s: String): Int? = Regex("^(\\d{1,2}):(\\d{2})$").find(s.trim())?.destructured?.let { (h, m) ->
    val hh = h.toInt(); val mm = m.toInt(); if (hh in 0..23 && mm in 0..59) hh * 60 + mm else null
}

@Composable
fun BusinessEditorScreen(vm: AdminViewModel, onBack: () -> Unit) {
    val current by remember { vm.state(vm.repo.business, BusinessDefaults.info) }.collectAsStateWithLifecycle()
    var b by remember(current.updatedAt) { mutableStateOf(current.copy(phone = "0" + current.phone.removePrefix("213"), whatsapp = "0" + current.whatsapp.removePrefix("213"))) }
    val hours = remember(current.updatedAt) {
        val parsed = BusinessHours.parse(current.openingHours)
        androidx.compose.runtime.mutableStateMapOf<Int, Pair<String, String>>().apply {
            weekDays.forEach { d -> parsed[d]?.let { put(d, BusinessHours.hhmm(it.first) to BusinessHours.hhmm(it.second)) } }
        }
    }
    EditorPage("معلومات الشركة", onBack, onSave = {
        val map = mutableMapOf<Int, Pair<Int, Int>>()
        for ((d, r) in hours) {
            val o = parseHhmm(r.first); val c = parseHhmm(r.second)
            if (o == null || c == null || c <= o) { vm.message("توقيت غير صالح ليوم ${dayAr(d)}"); return@EditorPage }
            map[d] = o to c
        }
        vm.run("تم حفظ معلومات الشركة") { vm.repo.saveBusiness(b.copy(openingHours = BusinessHours.format(map))) }
    }) {
        Text("تحقّق من كل معلومة قبل النشر. تظهر هذه البيانات للعملاء في صفحة التواصل.", style = MaterialTheme.typography.bodySmall, color = YT.Warning)
        FormSection("الهوية") {
            Field(b.companyName, { b = b.copy(companyName = it) }, "اسم الشركة *", maxLength = 80)
            Field(b.taglineAr, { b = b.copy(taglineAr = it) }, "الشعار بالعربية", maxLength = 120)
            Field(b.taglineFr, { b = b.copy(taglineFr = it) }, "Slogan (FR)", maxLength = 120)
            Field(b.taglineEn, { b = b.copy(taglineEn = it) }, "Tagline (EN)", maxLength = 120)
        }
        FormSection("التواصل") {
            Field(b.phone, { b = b.copy(phone = it) }, "الهاتف *", keyboard = KeyboardType.Phone, maxLength = 16,
                hint = com.yourtech.systeme.data.model.Validation.toInternational(b.phone)?.let { displayPhone(it) } ?: "مثال: 0561034149")
            Field(b.whatsapp, { b = b.copy(whatsapp = it) }, "واتساب *", keyboard = KeyboardType.Phone, maxLength = 16)
            Field(b.email, { b = b.copy(email = it.trim()) }, "البريد الإلكتروني", keyboard = KeyboardType.Email, maxLength = 120)
        }
        FormSection("العنوان والموقع") {
            Field(b.addressAr, { b = b.copy(addressAr = it) }, "العنوان بالعربية", maxLength = 200)
            Field(b.addressFr, { b = b.copy(addressFr = it) }, "Adresse (FR)", maxLength = 200)
            Field(b.addressEn, { b = b.copy(addressEn = it) }, "Address (EN)", maxLength = 200)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                var lat by remember(current.updatedAt) { mutableStateOf(b.latitude.toString()) }
                var lng by remember(current.updatedAt) { mutableStateOf(b.longitude.toString()) }
                Field(lat, { lat = it; it.toDoubleOrNull()?.let { v -> b = b.copy(latitude = v) } }, "خط العرض", Modifier.weight(1f), keyboard = KeyboardType.Decimal, maxLength = 14)
                Field(lng, { lng = it; it.toDoubleOrNull()?.let { v -> b = b.copy(longitude = v) } }, "خط الطول", Modifier.weight(1f), keyboard = KeyboardType.Decimal, maxLength = 14)
            }
        }
        FormSection("ساعات العمل") {
            weekDays.forEach { d ->
                val r = hours[d]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Chip(dayAr(d), r != null, { if (r != null) hours.remove(d) else hours[d] = "08:00" to "17:30" })
                    Spacer(Modifier.size(8.dp))
                    if (r != null) {
                        Field(r.first, { hours[d] = it to r.second }, "من", Modifier.weight(1f), maxLength = 5)
                        Spacer(Modifier.size(6.dp))
                        Field(r.second, { hours[d] = r.first to it }, "إلى", Modifier.weight(1f), maxLength = 5)
                    } else Text("مغلق", color = YT.TextMuted, modifier = Modifier.weight(1f))
                }
            }
        }
        FormSection("الشبكات الاجتماعية (عند التأكد فقط)") {
            Field(b.facebookUrl, { b = b.copy(facebookUrl = it.trim()) }, "Facebook (https://…)", keyboard = KeyboardType.Uri, maxLength = 300)
            Field(b.instagramUrl, { b = b.copy(instagramUrl = it.trim()) }, "Instagram (https://…)", keyboard = KeyboardType.Uri, maxLength = 300)
            Field(b.tiktokUrl, { b = b.copy(tiktokUrl = it.trim()) }, "TikTok (https://…)", keyboard = KeyboardType.Uri, maxLength = 300)
            Field(b.websiteUrl, { b = b.copy(websiteUrl = it.trim()) }, "الموقع الإلكتروني (https://…)", keyboard = KeyboardType.Uri, maxLength = 300)
        }
    }
}
