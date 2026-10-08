package com.yourtech.systeme.admin.feature

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourtech.systeme.admin.auth.AdminPermission
import com.yourtech.systeme.admin.auth.AdminRole
import com.yourtech.systeme.admin.ui.AdminTopBar
import com.yourtech.systeme.admin.ui.AdminViewModel
import com.yourtech.systeme.admin.ui.ChoiceRow
import com.yourtech.systeme.admin.ui.EditorPage
import com.yourtech.systeme.admin.ui.Field
import com.yourtech.systeme.admin.ui.FormSection
import com.yourtech.systeme.admin.ui.ListCard
import com.yourtech.systeme.admin.ui.SwitchRow
import com.yourtech.systeme.admin.ui.dateAr
import com.yourtech.systeme.admin.ui.ltr
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.theme.YT

@Composable
fun SettingsHubScreen(vm: AdminViewModel, onOpen: (String) -> Unit) {
    val user by vm.session.collectAsStateWithLifecycle()
    HubScreen(
        "الإعدادات",
        listOfNotNull(
            Triple("تغيير كلمة المرور", Icons.Rounded.Key, "password"),
            Triple("الحسابات والصلاحيات", Icons.Rounded.Group, "accounts").takeIf { user?.can(AdminPermission.ACCOUNTS) == true },
            Triple("سجل العمليات", Icons.Rounded.History, "audit").takeIf { user?.can(AdminPermission.AUDIT_LOG) == true },
        ),
        onOpen,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${user?.displayName.orEmpty()} • ${ltr(user?.username.orEmpty())} • ${user?.role?.labelAr.orEmpty()}", style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted)
            Spacer(Modifier.height(12.dp))
            OutlineButton("تسجيل الخروج", { vm.logout() }, Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Rounded.Logout, tint = YT.Danger)
            Spacer(Modifier.height(16.dp))
            Text(
                "ملاحظة: هذه النسخة تحفظ البيانات محليًا على هذا الجهاز. لمزامنة المنتجات والطلبات مع تطبيق العملاء يلزم ربط خادم (Supabase أو Firebase) — راجع README.",
                style = MaterialTheme.typography.bodySmall, color = YT.TextMuted,
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(vm: AdminViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(YT.Navy).imePadding()) {
        AdminTopBar("تغيير كلمة المرور", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChangePasswordForm(vm, onBack)
        }
    }
}

@Composable
fun AccountsScreen(vm: AdminViewModel, onBack: () -> Unit, onNew: () -> Unit) {
    val accounts by remember { vm.state(vm.auth.accounts, emptyList()) }.collectAsStateWithLifecycle()
    val me by vm.session.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().background(YT.Navy), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            AdminTopBar("الحسابات والصلاحيات", onBack)
            Column(Modifier.padding(horizontal = 16.dp)) {
                AdminRole.entries.forEach { r ->
                    Text("• ${r.labelAr}: ${r.permissions.joinToString("، ") { it.ar }}", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                }
                Spacer(Modifier.height(10.dp))
                com.yourtech.systeme.designsystem.component.GradientButton("حساب جديد", onNew, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
        }
        items(accounts, key = { it.id }) { a ->
            var temp by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            val role = AdminRole.of(a.role)
            ListCard({ expanded = !expanded }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(a.displayName, style = MaterialTheme.typography.titleSmall)
                        Text("${ltr(a.username)} • ${role.labelAr}", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                    }
                    when {
                        !a.isActive -> StatusPill("معطّل", YT.Danger)
                        a.lockedUntil > System.currentTimeMillis() -> StatusPill("مقفل مؤقتًا", YT.Warning)
                        a.mustChangePassword -> StatusPill("كلمة مرور مؤقتة", YT.Warning)
                        else -> StatusPill("نشط", YT.Success)
                    }
                }
                if (expanded && a.id != me?.id && role != AdminRole.OWNER) {
                    Spacer(Modifier.height(10.dp))
                    SwitchRow("الحساب مفعّل", a.isActive, { v -> vm.run(if (v) "تم التفعيل" else "تم التعطيل") { vm.auth.setActive(a.id, v) } })
                    Field(temp, { temp = it }, "كلمة مرور مؤقتة جديدة", password = true, maxLength = 128)
                    OutlineButton("إعادة تعيين كلمة المرور", { vm.run("تمت إعادة التعيين — يجب تغييرها عند الدخول", { temp = "" }) { vm.auth.resetPassword(a.id, temp) } }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun NewAccountScreen(vm: AdminViewModel, onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(AdminRole.STAFF) }
    var temp by remember { mutableStateOf("") }
    EditorPage("حساب جديد", onBack, saveText = "إنشاء الحساب", onSave = { vm.run("تم إنشاء الحساب", onBack) { vm.auth.createAccount(username, name, role, temp) } }) {
        FormSection("الحساب") {
            Field(username, { username = it.trim() }, "اسم المستخدم (لاتيني)", maxLength = 32)
            Field(name, { name = it }, "الاسم الظاهر", maxLength = 60)
            ChoiceRow("الدور", AdminRole.entries - AdminRole.OWNER, role, { it.labelAr }) { role = it }
            Text(role.permissions.joinToString("، ") { it.ar }, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
            Field(temp, { temp = it }, "كلمة مرور مؤقتة", password = true, hint = "سيُطلب من المستخدم تغييرها عند أول دخول", maxLength = 128)
        }
    }
}

@Composable
fun AuditLogScreen(vm: AdminViewModel, onBack: () -> Unit) {
    val log by remember { vm.state(vm.repo.auditLog, emptyList()) }.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().background(YT.Navy), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AdminTopBar("سجل العمليات", onBack) }
        if (log.isEmpty()) item { EmptyState(Icons.Rounded.History, "السجل فارغ", "تُسجَّل هنا كل عمليات الدخول والتعديل.") }
        items(log, key = { it.id }) { e ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ltr(e.action), style = MaterialTheme.typography.labelLarge, color = if (e.action.contains("FAILED") || e.action.contains("DELETED")) YT.Danger else YT.Cyan, modifier = Modifier.weight(1f))
                    Text(dateAr(e.timestamp, true), style = MaterialTheme.typography.labelSmall, color = YT.TextMuted)
                }
                Text("${ltr(e.actor)} (${AdminRole.of(e.role).labelAr}) • ${e.entityType} ${ltr(e.entityId)}", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                if (e.details.isNotBlank()) Text(e.details, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

val AdminPermission.ar: String get() = when (this) {
    AdminPermission.DASHBOARD -> "لوحة القيادة"
    AdminPermission.REQUESTS -> "الطلبات"
    AdminPermission.PRODUCTS -> "المنتجات"
    AdminPermission.CATEGORIES -> "الفئات"
    AdminPermission.SERVICES -> "الخدمات"
    AdminPermission.INVENTORY -> "المخزون"
    AdminPermission.PROMOTIONS -> "العروض"
    AdminPermission.TESTIMONIALS -> "الآراء"
    AdminPermission.BUSINESS_INFO -> "معلومات الشركة"
    AdminPermission.ACCOUNTS -> "الحسابات"
    AdminPermission.AUDIT_LOG -> "السجل"
}
