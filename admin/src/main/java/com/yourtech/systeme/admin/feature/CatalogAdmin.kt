package com.yourtech.systeme.admin.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.yourtech.systeme.admin.ui.ar
import com.yourtech.systeme.admin.ui.tint
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ProductEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.media.ImageImporter
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.Money
import com.yourtech.systeme.data.model.SecurityVisuals
import com.yourtech.systeme.data.model.StockStatus
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.SecurityIllustration
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.theme.YT
import kotlinx.coroutines.launch

fun importMessage(r: ImageImporter.Result): String? = when (r) {
    is ImageImporter.Result.Ok -> null
    ImageImporter.Result.UnsupportedType -> "نوع الصورة غير مدعوم (JPEG / PNG / WEBP / HEIC فقط)"
    ImageImporter.Result.TooLarge -> "الصورة كبيرة جدًا (الحد 20 ميغابايت)"
    ImageImporter.Result.Unreadable -> "تعذّر قراءة الصورة"
}

@Composable
private fun AddFab(text: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(onClick, containerColor = YT.Blue, contentColor = YT.White, modifier = Modifier.navigationBarsPadding()) {
        Icon(Icons.Rounded.Add, null); Spacer(Modifier.size(6.dp)); Text(text)
    }
}

// ---- Products ------------------------------------------------------------------------------------

@Composable
fun ProductsAdminScreen(vm: AdminViewModel, onBack: () -> Unit, onEdit: (String?) -> Unit) {
    val products by remember { vm.state(vm.repo.products, emptyList()) }.collectAsStateWithLifecycle()
    val categories by remember { vm.state(vm.repo.productCategories, emptyList()) }.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val list = products.filter { p ->
        (category == null || p.categoryId == category) &&
            (query.isBlank() || listOf(p.name, p.brand, p.model).any { it.contains(query.trim(), ignoreCase = true) })
    }
    androidx.compose.material3.Scaffold(containerColor = YT.Navy, floatingActionButton = { AddFab("منتج جديد") { onEdit(null) } }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                AdminTopBar("المنتجات (${products.size})", onBack)
                Field(query, { query = it }, "بحث بالاسم أو العلامة أو الطراز", Modifier.fillMaxWidth().padding(horizontal = 16.dp), maxLength = 60)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Chip("الكل", category == null, { category = null }) }
                    items(categories, key = { it.id }) { c -> Chip(c.nameAr, category == c.id, { category = c.id }) }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (list.isEmpty()) item {
                EmptyState(Icons.Rounded.Inventory2, if (products.isEmpty()) "الكتالوج فارغ" else "لا نتائج",
                    if (products.isEmpty()) "أضف المعدات الحقيقية المتوفرة لدى الشركة: الاسم، العلامة، السعر والضمان كما هي فعلًا." else "جرّب كلمات أخرى.")
            }
            items(list, key = { it.id }) { p ->
                ListCard({ onEdit(p.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaImage(p.localImagePath ?: p.imageUrl, p.photoKey ?: SecurityVisuals.keyFor(p.name, p.categoryId) ?: "camera", Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)))
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text(listOf(p.brand, p.model).filter { it.isNotBlank() }.joinToString(" • ").ifEmpty { "—" }, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 1)
                            Text(p.priceDzd?.let { Money.format(it, AppLanguage.ARABIC) } ?: "السعر عند الطلب", style = MaterialTheme.typography.labelLarge, color = YT.Cyan)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusPill(p.stockStatus.ar, p.stockStatus.tint)
                            if (!p.isActive) Text("مخفي", style = MaterialTheme.typography.labelSmall, color = YT.Danger)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditorScreen(vm: AdminViewModel, id: String?, onBack: () -> Unit) {
    val categories by remember { vm.state(vm.repo.productCategories, emptyList()) }.collectAsStateWithLifecycle()
    val existing by remember(id) { vm.state(if (id != null) vm.repo.product(id) else kotlinx.coroutines.flow.flowOf(null), null) }.collectAsStateWithLifecycle()
    if (id != null && existing == null) { Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("تعديل منتج", onBack) }; return }
    val base = existing ?: ProductEntity(id = "", categoryId = categories.firstOrNull()?.id.orEmpty(), name = "")
    var name by remember(base.id) { mutableStateOf(base.name) }
    var brand by remember(base.id) { mutableStateOf(base.brand) }
    var model by remember(base.id) { mutableStateOf(base.model) }
    var categoryId by remember(base.id) { mutableStateOf(base.categoryId) }
    var description by remember(base.id) { mutableStateOf(base.description) }
    var specs by remember(base.id) { mutableStateOf(base.specs) }
    var price by remember(base.id) { mutableStateOf(base.priceDzd?.toString().orEmpty()) }
    var stock by remember(base.id) { mutableStateOf(base.stockStatus) }
    var qty by remember(base.id) { mutableStateOf(base.stockQty?.toString().orEmpty()) }
    var warranty by remember(base.id) { mutableStateOf(base.warranty) }
    var install by remember(base.id) { mutableStateOf(base.installationAvailable) }
    var imageUrl by remember(base.id) { mutableStateOf(base.imageUrl.orEmpty()) }
    var localImage by remember(base.id) { mutableStateOf(base.localImagePath) }
    var photoKey by remember(base.id) { mutableStateOf(base.photoKey) }
    var featured by remember(base.id) { mutableStateOf(base.isFeatured) }
    var active by remember(base.id) { mutableStateOf(base.isActive) }
    val scope = rememberCoroutineScope()

    EditorPage(if (id == null) "منتج جديد" else "تعديل منتج", onBack, onSave = {
        if (categoryId.isBlank()) { vm.message("اختر الفئة"); return@EditorPage }
        val priceValue = price.takeIf { it.isNotBlank() }?.toIntOrNull()
        if (price.isNotBlank() && priceValue == null) { vm.message("السعر غير صالح"); return@EditorPage }
        vm.run("تم حفظ المنتج", onBack) {
            vm.repo.saveProduct(
                base.copy(
                    name = name, brand = brand, model = model, categoryId = categoryId, description = description, specs = specs,
                    priceDzd = priceValue, stockStatus = stock, stockQty = qty.toIntOrNull(), warranty = warranty,
                    installationAvailable = install, imageUrl = imageUrl, localImagePath = localImage, photoKey = photoKey,
                    isFeatured = featured, isActive = active,
                ),
                isNew = id == null,
            )
        }
    }) {
        Text("أدخل فقط معلومات حقيقية مؤكدة: لا أسعار أو علامات أو ضمانات تقديرية.", style = MaterialTheme.typography.bodySmall, color = YT.Warning)
        FormSection("المعلومات الأساسية") {
            Field(name, { name = it }, "اسم المنتج *", maxLength = 120)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(brand, { brand = it }, "العلامة", Modifier.weight(1f), maxLength = 60)
                Field(model, { model = it }, "الطراز", Modifier.weight(1f), maxLength = 60)
            }
            ChoiceRow("الفئة *", categories, categories.firstOrNull { it.id == categoryId }, { it.nameAr }) { categoryId = it.id }
            Field(description, { description = it }, "الوصف", singleLine = false, minLines = 3)
            Field(specs, { specs = it }, "المواصفات", singleLine = false, minLines = 3, hint = "سطر لكل مواصفة، مثال: الدقة: 4 ميغابكسل")
        }
        FormSection("السعر والمخزون") {
            Field(price, { price = it.filter(Char::isDigit) }, "السعر (د.ج)", keyboard = KeyboardType.Number, maxLength = 9,
                hint = price.toIntOrNull()?.let { Money.format(it, AppLanguage.ARABIC) } ?: "اتركه فارغًا ليظهر «السعر عند الطلب»")
            ChoiceRow("حالة المخزون", StockStatus.entries, stock, { it.ar }) { stock = it }
            Field(qty, { qty = it.filter(Char::isDigit) }, "الكمية (اختياري، داخلي)", keyboard = KeyboardType.Number, maxLength = 7)
            Field(warranty, { warranty = it }, "الضمان (فقط ما تقدمه الشركة فعلًا)", maxLength = 200)
            SwitchRow("التركيب متاح", install, { install = it })
        }
        FormSection("الصورة") {
            MediaImage(localImage ?: imageUrl.ifBlank { null }, photoKey ?: SecurityVisuals.keyFor(name) ?: "camera", Modifier.fillMaxWidth().aspectRatio(16 / 10f).clip(RoundedCornerShape(16.dp)))
            PickImageButton { uri ->
                scope.launch {
                    when (val r = vm.repo.importImage(uri, "products")) {
                        is ImageImporter.Result.Ok -> { if (localImage != base.localImagePath) vm.repo.deleteImage(localImage); localImage = r.path }
                        else -> vm.message(importMessage(r).orEmpty())
                    }
                }
            }
            if (localImage != null) DeleteButton("إزالة الصورة", { if (localImage != base.localImagePath) vm.repo.deleteImage(localImage); localImage = null })
            Field(imageUrl, { imageUrl = it.trim() }, "أو رابط صورة (https)", keyboard = KeyboardType.Uri, maxLength = 500)
            ChoiceRow("رسم توضيحي بديل", SecurityVisuals.all, SecurityVisuals.entry(photoKey), { it.label }) { photoKey = it.key }
        }
        FormSection("العرض") {
            SwitchRow("منتج مميز (يظهر في الرئيسية)", featured, { featured = it })
            SwitchRow("ظاهر للعملاء", active, { active = it })
        }
        if (id != null) ProductDeleteRow(vm, id, onBack)
    }
}

@Composable
fun ProductDeleteRow(vm: AdminViewModel, id: String, onDeleted: () -> Unit) {
    DeleteButton("حذف المنتج", { vm.run("تم حذف المنتج", onDeleted) { vm.repo.deleteProduct(id) } })
}

// ---- Inventory -----------------------------------------------------------------------------------

@Composable
fun InventoryScreen(vm: AdminViewModel, onBack: () -> Unit) {
    val products by remember { vm.state(vm.repo.products, emptyList()) }.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().background(YT.Navy), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AdminTopBar("المخزون", onBack) }
        if (products.isEmpty()) item { EmptyState(Icons.Rounded.Inventory2, "لا توجد منتجات", "أضف المنتجات أولًا من قسم المنتجات.") }
        items(products, key = { it.id }) { p ->
            var qty by remember(p.id, p.stockQty) { mutableStateOf(p.stockQty?.toString().orEmpty()) }
            ListCard(null, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Text(p.name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(StockStatus.entries) { s -> Chip(s.ar, s == p.stockStatus, { vm.run { vm.repo.setStock(p.id, s, qty.toIntOrNull()) } }) }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Field(qty, { qty = it.filter(Char::isDigit) }, "الكمية", Modifier.weight(1f), keyboard = KeyboardType.Number, maxLength = 7)
                    Spacer(Modifier.size(8.dp))
                    com.yourtech.systeme.designsystem.component.OutlineButton("حفظ", { vm.run("تم تحديث المخزون") { vm.repo.setStock(p.id, p.stockStatus, qty.toIntOrNull()) } })
                }
            }
        }
    }
}

// ---- Product categories --------------------------------------------------------------------------

@Composable
fun CategoriesScreen(vm: AdminViewModel, onBack: () -> Unit, onEdit: (String?) -> Unit) {
    val list by remember { vm.state(vm.repo.productCategories, emptyList()) }.collectAsStateWithLifecycle()
    androidx.compose.material3.Scaffold(containerColor = YT.Navy, floatingActionButton = { AddFab("فئة جديدة") { onEdit(null) } }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp)) {
            item { AdminTopBar("فئات المعدات", onBack) }
            items(list, key = { it.id }) { c ->
                ListCard({ onEdit(c.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SecurityIllustration(c.iconKey, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)), animated = false)
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.nameAr, style = MaterialTheme.typography.titleSmall)
                            Text("${c.nameFr} • ${c.nameEn}", style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                        }
                        if (!c.isActive) Text("مخفية", color = YT.Danger, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryEditorScreen(vm: AdminViewModel, id: String?, onBack: () -> Unit) {
    val list by remember { vm.state(vm.repo.productCategories, emptyList()) }.collectAsStateWithLifecycle()
    val base = list.firstOrNull { it.id == id } ?: if (id == null) ProductCategoryEntity("", "", "", "", sortOrder = list.size) else null
    if (base == null) { Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("فئة", onBack) }; return }
    var ar by remember(base.id) { mutableStateOf(base.nameAr) }
    var fr by remember(base.id) { mutableStateOf(base.nameFr) }
    var en by remember(base.id) { mutableStateOf(base.nameEn) }
    var icon by remember(base.id) { mutableStateOf(base.iconKey) }
    var order by remember(base.id) { mutableStateOf(base.sortOrder.toString()) }
    var active by remember(base.id) { mutableStateOf(base.isActive) }
    EditorPage(if (id == null) "فئة جديدة" else "تعديل فئة", onBack, onSave = {
        vm.run("تم الحفظ", onBack) {
            vm.repo.saveProductCategory(base.copy(nameAr = ar, nameFr = fr, nameEn = en, iconKey = icon, sortOrder = order.toIntOrNull() ?: 0, isActive = active), id == null)
        }
    }) {
        FormSection("الاسم") {
            Field(ar, { ar = it }, "بالعربية *", maxLength = 60)
            Field(fr, { fr = it }, "Français *", maxLength = 60)
            Field(en, { en = it }, "English", maxLength = 60)
        }
        FormSection("العرض") {
            ChoiceRow("الأيقونة", SecurityVisuals.all, SecurityVisuals.entry(icon), { it.label }) { icon = it.key }
            Field(order, { order = it.filter(Char::isDigit) }, "الترتيب", keyboard = KeyboardType.Number, maxLength = 3)
            SwitchRow("ظاهرة للعملاء", active, { active = it })
        }
        if (id != null) DeleteButton("حذف الفئة", { vm.run("تم الحذف", onBack) { vm.repo.deleteProductCategory(id) } })
    }
}

// ---- Service categories (security solutions) -----------------------------------------------------

@Composable
fun ServicesScreen(vm: AdminViewModel, onBack: () -> Unit, onEdit: (String?) -> Unit) {
    val list by remember { vm.state(vm.repo.services, emptyList()) }.collectAsStateWithLifecycle()
    androidx.compose.material3.Scaffold(containerColor = YT.Navy, floatingActionButton = { AddFab("خدمة جديدة") { onEdit(null) } }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                AdminTopBar("الخدمات والحلول", onBack)
                Text(
                    "هذه قائمة مقترحة. فعّل فقط الخدمات التي تقدمها الشركة فعلًا؛ الخدمات المعطّلة لا تظهر للعملاء.",
                    style = MaterialTheme.typography.bodySmall, color = YT.Warning, modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
            items(list, key = { it.id }) { s ->
                ListCard({ onEdit(s.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SecurityIllustration(s.photoKey ?: s.iconKey, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)), animated = false)
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.nameAr, style = MaterialTheme.typography.titleSmall)
                            Text(s.summaryAr, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 2)
                        }
                        StatusPill(if (s.isActive) "مفعّلة" else "بانتظار التأكيد", if (s.isActive) YT.Success else YT.Warning)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceEditorScreen(vm: AdminViewModel, id: String?, onBack: () -> Unit) {
    val list by remember { vm.state(vm.repo.services, emptyList()) }.collectAsStateWithLifecycle()
    val base = list.firstOrNull { it.id == id }
        ?: if (id == null) ServiceCategoryEntity("svc_" + java.util.UUID.randomUUID().toString().take(8), "", "", "", "", "", "", sortOrder = list.size, isActive = false) else null
    if (base == null) { Column(Modifier.fillMaxSize().background(YT.Navy)) { AdminTopBar("خدمة", onBack) }; return }
    var s by remember(base.id) { mutableStateOf(base) }
    var lang by rememberSaveable { mutableStateOf(AppLanguage.ARABIC) }
    EditorPage(if (id == null) "خدمة جديدة" else "تعديل خدمة", onBack, onSave = { vm.run("تم الحفظ", onBack) { vm.repo.saveService(s) } }) {
        FormSection("الحالة") {
            SwitchRow("الشركة تقدم هذه الخدمة (إظهارها للعملاء)", s.isActive, { s = s.copy(isActive = it) }, "لا تفعّلها قبل التأكد")
            SwitchRow("مميزة في الصفحة الرئيسية", s.isFeatured, { s = s.copy(isFeatured = it) })
            ChoiceRow("الرسم التوضيحي", SecurityVisuals.all, SecurityVisuals.entry(s.photoKey), { it.label }) { s = s.copy(photoKey = it.key, iconKey = it.key) }
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { l -> Chip(l.pick("العربية", "Français", "English"), lang == l, { lang = l }) }
        }
        FormSection("المحتوى") {
            when (lang) {
                AppLanguage.ARABIC -> {
                    Field(s.nameAr, { s = s.copy(nameAr = it) }, "الاسم *", maxLength = 80)
                    Field(s.summaryAr, { s = s.copy(summaryAr = it) }, "الوصف", singleLine = false, minLines = 3, maxLength = 600)
                    Field(s.benefitsAr, { s = s.copy(benefitsAr = it) }, "المزايا (سطر لكل ميزة)", singleLine = false, minLines = 3)
                    Field(s.useCasesAr, { s = s.copy(useCasesAr = it) }, "حالات الاستخدام (سطر لكل حالة)", singleLine = false, minLines = 3)
                    Field(s.specsAr, { s = s.copy(specsAr = it) }, "المواصفات (سطر لكل مواصفة)", singleLine = false, minLines = 3)
                }
                AppLanguage.FRENCH -> {
                    Field(s.nameFr, { s = s.copy(nameFr = it) }, "Nom *", maxLength = 80)
                    Field(s.summaryFr, { s = s.copy(summaryFr = it) }, "Description", singleLine = false, minLines = 3, maxLength = 600)
                    Field(s.benefitsFr, { s = s.copy(benefitsFr = it) }, "Avantages (une ligne chacun)", singleLine = false, minLines = 3)
                    Field(s.useCasesFr, { s = s.copy(useCasesFr = it) }, "Cas d'usage (une ligne chacun)", singleLine = false, minLines = 3)
                    Field(s.specsFr, { s = s.copy(specsFr = it) }, "Caractéristiques (une ligne chacune)", singleLine = false, minLines = 3)
                }
                AppLanguage.ENGLISH -> {
                    Field(s.nameEn, { s = s.copy(nameEn = it) }, "Name", maxLength = 80)
                    Field(s.summaryEn, { s = s.copy(summaryEn = it) }, "Description", singleLine = false, minLines = 3, maxLength = 600)
                    Field(s.benefitsEn, { s = s.copy(benefitsEn = it) }, "Benefits (one per line)", singleLine = false, minLines = 3)
                    Field(s.useCasesEn, { s = s.copy(useCasesEn = it) }, "Use cases (one per line)", singleLine = false, minLines = 3)
                    Field(s.specsEn, { s = s.copy(specsEn = it) }, "Specifications (one per line)", singleLine = false, minLines = 3)
                }
            }
        }
    }
}

/** Hub screen grouping the catalog sections. */
@Composable
fun CatalogHubScreen(vm: AdminViewModel, onOpen: (String) -> Unit) {
    val user by vm.session.collectAsStateWithLifecycle()
    HubScreen(
        "الكتالوج",
        listOf(
            Triple("المنتجات", Icons.Rounded.Inventory2, "products") to com.yourtech.systeme.admin.auth.AdminPermission.PRODUCTS,
            Triple("المخزون", Icons.Rounded.Search, "inventory") to com.yourtech.systeme.admin.auth.AdminPermission.INVENTORY,
            Triple("فئات المعدات", Icons.Rounded.Category, "categories") to com.yourtech.systeme.admin.auth.AdminPermission.CATEGORIES,
            Triple("الخدمات والحلول", Icons.Rounded.Security, "services") to com.yourtech.systeme.admin.auth.AdminPermission.SERVICES,
        ).filter { user?.can(it.second) == true }.map { it.first },
        onOpen,
    )
}

@Composable
fun HubScreen(title: String, entries: List<Triple<String, androidx.compose.ui.graphics.vector.ImageVector, String>>, onOpen: (String) -> Unit, footer: @Composable () -> Unit = {}) {
    LazyColumn(Modifier.fillMaxSize().background(YT.Navy), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AdminTopBar(title) }
        items(entries, key = { it.third }) { (label, icon, route) ->
            ListCard({ onOpen(route) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.yourtech.systeme.designsystem.component.IconTile(icon)
                    Spacer(Modifier.size(14.dp))
                    Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                }
            }
        }
        item { footer() }
    }
}
