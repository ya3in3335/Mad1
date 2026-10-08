package com.yourtech.systeme.admin.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourtech.systeme.admin.auth.AdminPermission
import com.yourtech.systeme.admin.ui.AdminLauncher
import com.yourtech.systeme.admin.ui.AdminTopBar
import com.yourtech.systeme.admin.ui.AdminViewModel
import com.yourtech.systeme.admin.ui.Field
import com.yourtech.systeme.admin.ui.FormSection
import com.yourtech.systeme.admin.ui.ListCard
import com.yourtech.systeme.admin.ui.ar
import com.yourtech.systeme.admin.ui.dateAr
import com.yourtech.systeme.admin.ui.ltr
import com.yourtech.systeme.admin.ui.tint
import com.yourtech.systeme.data.local.entity.RequestWithDetails
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.BusinessHours
import com.yourtech.systeme.data.model.DashboardStats
import com.yourtech.systeme.data.model.Money
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.Wilayas
import com.yourtech.systeme.data.model.displayPhone
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.IconTile
import com.yourtech.systeme.designsystem.component.KeyValueRow
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.SectionHeader
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.theme.YT
import java.util.Calendar

// ---- Dashboard ---------------------------------------------------------------------------------

data class Shortcut(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String, val permission: AdminPermission)

@Composable
fun DashboardScreen(vm: AdminViewModel, shortcuts: List<Shortcut>, onOpen: (String) -> Unit, onRequest: (Long) -> Unit) {
    val user by vm.session.collectAsStateWithLifecycle()
    val stats by remember { vm.state(vm.repo.stats(), DashboardStats()) }.collectAsStateWithLifecycle()
    val requests by remember { vm.state(vm.repo.requests, emptyList()) }.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().background(YT.Navy), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            AdminTopBar("لوحة القيادة")
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("مرحبًا ${user?.displayName.orEmpty()}", style = MaterialTheme.typography.headlineSmall)
                Text(user?.role?.labelAr.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = YT.Cyan)
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("طلبات اليوم", stats.newToday, Icons.Rounded.Today, YT.Cyan, Modifier.weight(1f))
                StatCard("بانتظار الدراسة", stats.submitted, Icons.Rounded.NewReleases, YT.Warning, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("طلبات مفتوحة", stats.open, Icons.Rounded.PendingActions, YT.Blue, Modifier.weight(1f))
                StatCard("مواعيد اليوم", stats.scheduledToday, Icons.Rounded.Event, YT.Violet, Modifier.weight(1f))
                StatCard("مكتملة", stats.completed, Icons.Rounded.CheckCircle, YT.Success, Modifier.weight(1f))
            }
        }
        val allowed = shortcuts.filter { user?.can(it.permission) == true }
        if (allowed.isNotEmpty()) {
            item { SectionHeader("إدارة سريعة") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(allowed) { s ->
                        ListCard({ onOpen(s.route) }, Modifier.size(width = 120.dp, height = 104.dp)) {
                            IconTile(s.icon, size = 38)
                            Spacer(Modifier.height(8.dp))
                            Text(s.title, style = MaterialTheme.typography.labelLarge, maxLines = 2)
                        }
                    }
                }
            }
        }
        item { SectionHeader("أحدث الطلبات") }
        if (requests.isEmpty()) {
            item { EmptyState(Icons.Rounded.Assignment, "لا توجد طلبات بعد", "تظهر هنا طلبات التركيب والصيانة عند ربط التطبيق بالخادم.") }
        }
        items(requests.take(6), key = { it.request.id }) { r ->
            RequestRow(r, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) { onRequest(r.request.id) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    GlassCard(modifier, padding = PaddingValues(12.dp)) {
        IconTile(icon, tint, size = 34)
        Spacer(Modifier.height(8.dp))
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelMedium, color = YT.TextMuted, maxLines = 1)
    }
}

@Composable
fun RequestRow(r: RequestWithDetails, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val q = r.request
    ListCard(onClick, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${q.type.ar} • ${q.customerName}", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(ltr(q.reference), style = MaterialTheme.typography.labelMedium, color = YT.TextMuted)
            }
            StatusPill(q.status.ar, q.status.tint)
        }
        Spacer(Modifier.height(6.dp))
        val wilaya = Wilayas.fromStorage(q.wilaya)?.label(AppLanguage.ARABIC) ?: q.wilaya
        Text("$wilaya • ${q.commune} • ${dateAr(q.createdAt)}", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 1)
        q.scheduledAt?.let { Text("الموعد: ${dateAr(it, true)}", style = MaterialTheme.typography.bodySmall, color = YT.Cyan) }
    }
}

// ---- Requests list -------------------------------------------------------------------------------

@Composable
fun RequestsAdminScreen(vm: AdminViewModel, onRequest: (Long) -> Unit, onAppointments: () -> Unit) {
    val all by remember { vm.state(vm.repo.requests, emptyList()) }.collectAsStateWithLifecycle()
    var status by rememberSaveable { mutableStateOf<String?>("OPEN") }
    var type by rememberSaveable { mutableStateOf<String?>(null) }
    val list = all.filter { r ->
        val s = r.request.status
        (status == null || (status == "OPEN" && s.isOpen) || s.name == status) && (type == null || r.request.type.name == type)
    }
    Column(Modifier.fillMaxSize().background(YT.Navy)) {
        AdminTopBar("الطلبات", actions = {
            androidx.compose.material3.IconButton(onClick = onAppointments) { androidx.compose.material3.Icon(Icons.Rounded.CalendarMonth, "المواعيد", tint = YT.Cyan) }
        })
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("المفتوحة", status == "OPEN", { status = "OPEN" })
            Chip("الكل", status == null, { status = null })
            RequestStatus.entries.forEach { s -> Chip("${s.ar} (${all.count { it.request.status == s }})", status == s.name, { status = s.name }) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("كل الأنواع", type == null, { type = null })
            RequestType.entries.forEach { t -> Chip(t.ar, type == t.name, { type = t.name }) }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (list.isEmpty()) item { EmptyState(Icons.Rounded.Inventory2, "لا توجد طلبات", "لا توجد طلبات مطابقة لهذا الفلتر.") }
            items(list, key = { it.request.id }) { r -> RequestRow(r) { onRequest(r.request.id) } }
        }
    }
}

@Composable
fun AppointmentsScreen(vm: AdminViewModel, onBack: () -> Unit, onRequest: (Long) -> Unit) {
    val list by remember { vm.state(vm.repo.appointments, emptyList()) }.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(YT.Navy)) {
        AdminTopBar("المواعيد", onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (list.isEmpty()) item { EmptyState(Icons.Rounded.CalendarMonth, "لا توجد مواعيد", "حدّد موعدًا من صفحة الطلب ليظهر هنا.") }
            var lastDay = ""
            list.forEach { r ->
                val day = dateAr(r.request.scheduledAt!!)
                if (day != lastDay) {
                    lastDay = day
                    item(key = "d$day") { Text(day, style = MaterialTheme.typography.titleSmall, color = YT.Cyan, modifier = Modifier.padding(top = 6.dp)) }
                }
                item(key = r.request.id) {
                    RequestRow(r) { onRequest(r.request.id) }
                }
            }
        }
    }
}

// ---- Request detail ------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestAdminScreen(vm: AdminViewModel, id: Long, onBack: () -> Unit) {
    val data by remember(id) { vm.state(vm.repo.request(id), null) }.collectAsStateWithLifecycle()
    val services by remember { vm.state(vm.repo.services, emptyList()) }.collectAsStateWithLifecycle()
    val categories by remember { vm.state(vm.repo.productCategories, emptyList()) }.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val r = data ?: run {
        Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("الطلب", onBack) }
        return
    }
    val q = r.request
    var note by remember(q.id) { mutableStateOf("") }
    var scheduled by remember(q.id, q.scheduledAt) { mutableStateOf(q.scheduledAt) }
    var quote by remember(q.id) { mutableStateOf(q.quoteAmountDzd?.toString().orEmpty()) }
    var companyNote by remember(q.id) { mutableStateOf(q.companyNote) }
    var assigned by remember(q.id) { mutableStateOf(q.assignedTo) }
    var showDate by remember { mutableStateOf(false) }
    val system = q.systemType?.let { sys -> services.firstOrNull { it.id == sys }?.name(AppLanguage.ARABIC) ?: categories.firstOrNull { it.id == sys }?.name(AppLanguage.ARABIC) ?: sys }

    Column(Modifier.fillMaxSize().background(YT.Navy).imePadding()) {
        AdminTopBar(ltr(q.reference), onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(q.type.ar, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                StatusPill(q.status.ar, q.status.tint)
            }
            FormSection("العميل") {
                KeyValueRow("الاسم", q.customerName)
                KeyValueRow("الهاتف", ltr(displayPhone(com.yourtech.systeme.data.model.Validation.toInternational(q.phone) ?: q.phone)))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlineButton("اتصال", { AdminLauncher.call(context, q.phone) }, Modifier.weight(1f), icon = Icons.Rounded.Call)
                    OutlineButton("واتساب", {
                        AdminLauncher.whatsapp(context, q.phone, "السلام عليكم ${q.customerName}، معكم YOURTECH SYSTEME بخصوص طلبكم ${q.reference}.")
                    }, Modifier.weight(1f), icon = Icons.Rounded.Chat, tint = YT.Success)
                }
            }
            FormSection("التفاصيل") {
                val wilaya = Wilayas.fromStorage(q.wilaya)?.label(AppLanguage.ARABIC) ?: q.wilaya
                KeyValueRow("الموقع", "$wilaya • ${q.commune}")
                if (q.address.isNotBlank()) KeyValueRow("العنوان", q.address)
                q.propertyType?.let { KeyValueRow("نوع العقار", it.ar) }
                system?.let { KeyValueRow("النظام / المعدات", it) }
                q.deviceCount?.let { KeyValueRow("عدد الأجهزة", it.toString()) }
                q.productId?.let { KeyValueRow("المنتج", it) }
                q.preferredDate?.let { KeyValueRow("التاريخ المفضل", dateAr(it)) }
                if (q.problemDescription.isNotBlank()) KeyValueRow("وصف المشكلة", q.problemDescription)
                if (q.notes.isNotBlank()) KeyValueRow("ملاحظات العميل", q.notes)
                KeyValueRow("تاريخ الإرسال", dateAr(q.createdAt, true))
                if (r.photos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(r.photos) { p -> MediaImage(p.path, "camera", Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)), ContentScale.Crop) }
                    }
                }
            }
            if (vm.session.value?.can(AdminPermission.REQUESTS) == true) {
                FormSection("تحديث الحالة") {
                    Field(note, { note = it }, "ملاحظة للعميل (اختياري)", singleLine = false, minLines = 2, maxLength = 500)
                    q.status.next()?.let { next ->
                        GradientButton("نقل إلى: ${next.ar}", { vm.run("تم تحديث الحالة", { note = "" }) { vm.repo.updateStatus(q.id, next, note) } }, Modifier.fillMaxWidth())
                    }
                    val jumps = RequestStatus.entries.filter { it != q.status.next() && it != RequestStatus.CANCELLED && q.status.canTransitionTo(it) }
                    if (jumps.isNotEmpty()) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            jumps.forEach { s -> Chip(s.ar, false, { vm.run("تم تحديث الحالة", { note = "" }) { vm.repo.updateStatus(q.id, s, note) } }) }
                        }
                    }
                    if (q.status.canTransitionTo(RequestStatus.CANCELLED)) {
                        com.yourtech.systeme.admin.ui.DeleteButton("إلغاء الطلب", { vm.run("تم إلغاء الطلب") { vm.repo.updateStatus(q.id, RequestStatus.CANCELLED, note) } })
                    }
                }
                FormSection("الموعد وعرض السعر") {
                    OutlineButton(scheduled?.let { "الموعد: ${dateAr(it, true)}" } ?: "تحديد تاريخ الموعد", { showDate = true }, Modifier.fillMaxWidth(), icon = Icons.Rounded.CalendarMonth)
                    if (scheduled != null) {
                        Text("الساعة", style = MaterialTheme.typography.labelLarge, color = YT.TextMuted)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val cal = Calendar.getInstance(BusinessHours.ALGIERS).apply { timeInMillis = scheduled!! }
                            val current = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                            (16..34).map { it * 30 }.forEach { m ->
                                Chip(BusinessHours.hhmm(m), m == current, {
                                    scheduled = cal.apply { set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60) }.timeInMillis
                                })
                            }
                        }
                        TextButton({ scheduled = null }) { Text("إزالة الموعد", color = YT.Danger) }
                    }
                    Field(quote, { quote = it.filter(Char::isDigit) }, "مبلغ عرض السعر (د.ج)", keyboard = KeyboardType.Number, maxLength = 9,
                        hint = quote.toIntOrNull()?.let { Money.format(it, AppLanguage.ARABIC) })
                    Field(assigned, { assigned = it }, "التقني المكلّف", maxLength = 80)
                    Field(companyNote, { companyNote = it }, "ملاحظة الشركة (تظهر للعميل)", singleLine = false, minLines = 2, maxLength = 1000)
                    GradientButton("حفظ", { vm.run("تم الحفظ") { vm.repo.saveRequestDetails(q.id, scheduled, quote, companyNote, assigned) } }, Modifier.fillMaxWidth())
                }
            }
            FormSection("السجل") {
                r.events.sortedBy { it.timestamp }.forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(e.status.ar, e.status.tint)
                        Spacer(Modifier.size(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dateAr(e.timestamp, true), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                            if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = scheduled ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let { utc ->
                        val day = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
                        scheduled = Calendar.getInstance(BusinessHours.ALGIERS).apply {
                            clear(); set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), 9, 0)
                        }.timeInMillis
                    }
                    showDate = false
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton({ showDate = false }) { Text("إلغاء") } },
        ) { DatePicker(state) }
    }
}
