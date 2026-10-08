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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.admin.ui.AdminCard
import com.madak.spices.admin.ui.dzd
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.CustomerRow
import com.madak.spices.data.local.entity.InventoryRow
import com.madak.spices.data.local.entity.ProductEntity
import com.madak.spices.data.model.Product
import com.madak.spices.data.repository.AdminRepository
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.designsystem.component.ProductImage
import com.madak.spices.designsystem.component.StatusPill
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CatalogAdminViewModel @Inject constructor(private val admin: AdminRepository) : ViewModel() {
    val products: StateFlow<List<Product>> = admin.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories: StateFlow<List<CategoryEntity>> = admin.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val inventory: StateFlow<List<InventoryRow>> = admin.inventory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val customers: StateFlow<List<CustomerRow>> = admin.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setProductActive(id: String, active: Boolean) = viewModelScope.launch { admin.setProductActive(id, active) }
    fun setCategoryActive(id: String, active: Boolean) = viewModelScope.launch { admin.setCategoryActive(id, active) }
    fun adjustStock(variantId: String, delta: Int) = viewModelScope.launch { admin.adjustStock(variantId, delta) }
    fun saveProduct(p: ProductEntity, per100: Int, stock: Int) = viewModelScope.launch { admin.saveProduct(p, per100, stock) }
    fun saveCategory(c: CategoryEntity) = viewModelScope.launch { admin.saveCategory(c) }
}

private fun slug(text: String): String =
    text.lowercase().map { if (it.isLetterOrDigit() && it.code < 128) it else '_' }.joinToString("").trim('_').ifBlank { "item_" + System.currentTimeMillis() }

// ---------------------------------------------------------------------------------------------
// Products
// ---------------------------------------------------------------------------------------------

@Composable
fun ProductsAdminScreen(viewModel: CatalogAdminViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (products.isEmpty()) {
                item {
                    AdminCard {
                        Column {
                            Text("لا توجد منتجات بعد", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "أضف أول منتج بالضغط على «منتج جديد». يكفي إدخال سعر 100 غ وتُحسب باقي الأوزان تلقائياً.",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            items(products, key = { it.id }) { p ->
                AdminCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductImage(p.entity.imageUrl, p.entity.colorArgb, p.id, Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.entity.nameAr, style = MaterialTheme.typography.titleSmall)
                            Text(p.sortedVariants.joinToString(" • ") { "${it.weightGrams}غ ${dzd(it.priceDzd)}" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("المخزون: ${p.variants.sumOf { it.stock }}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { editing = p }) { Icon(Icons.Rounded.Edit, "تعديل") }
                        Switch(p.entity.isActive, { viewModel.setProductActive(p.id, it) })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text("منتج جديد") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
    if (editing != null || creating) {
        ProductEditor(
            product = editing, categories = categories,
            onDismiss = { editing = null; creating = false },
            onSave = { entity, per100, stock -> viewModel.saveProduct(entity, per100, stock); editing = null; creating = false },
        )
    }
}

@Composable
private fun ProductEditor(product: Product?, categories: List<CategoryEntity>, onDismiss: () -> Unit, onSave: (ProductEntity, Int, Int) -> Unit) {
    val e = product?.entity
    var nameAr by remember { mutableStateOf(e?.nameAr.orEmpty()) }
    var nameFr by remember { mutableStateOf(e?.nameFr.orEmpty()) }
    var descAr by remember { mutableStateOf(e?.descriptionAr.orEmpty()) }
    var category by remember { mutableStateOf(e?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var per100 by remember { mutableStateOf(product?.variants?.firstOrNull { it.weightGrams == 100 }?.priceDzd?.toString().orEmpty()) }
    var stock by remember { mutableStateOf("50") }
    var featured by remember { mutableStateOf(e?.isFeatured ?: false) }
    var best by remember { mutableStateOf(e?.isBestSeller ?: false) }
    val valid = nameAr.isNotBlank() && (per100.toIntOrNull() ?: 0) > 0 && category.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "إضافة منتج" else "تعديل المنتج") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(nameAr, { nameAr = it }, label = { Text("الاسم بالعربية") }, singleLine = true)
                OutlinedTextField(nameFr, { nameFr = it }, label = { Text("الاسم بالفرنسية") }, singleLine = true)
                OutlinedTextField(descAr, { descAr = it }, label = { Text("الوصف") }, minLines = 2)
                OutlinedTextField(per100, { per100 = it.filter(Char::isDigit) }, label = { Text("سعر 100 غ (د.ج)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                if (product == null) {
                    OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("المخزون الأولي لكل وزن") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Text("القسم", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { c -> MadakChip(c.nameAr, category == c.id, { category = c.id }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(featured, { featured = it }); Text("مختارات مذاق") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(best, { best = it }); Text("الأكثر مبيعاً") }
                Text("تُحسب أسعار 50/250/500 غ تلقائياً من سعر 100 غ.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val entity = (e ?: ProductEntity(
                    id = slug(nameFr.ifBlank { nameAr }), categoryId = category, nameAr = nameAr, nameFr = nameFr,
                    descriptionAr = descAr, descriptionFr = "", colorArgb = listOf(0xFFC0632DL, 0xFFE3A21AL, 0xFF9C6B30L, 0xFF6BA539L, 0xFF8B4A2BL).random(),
                )).copy(
                    categoryId = category, nameAr = nameAr.trim(), nameFr = nameFr.trim(), descriptionAr = descAr.trim(),
                    isFeatured = featured, isBestSeller = best, tags = listOf(nameAr, nameFr).joinToString(","),
                )
                onSave(entity, per100.toInt(), stock.toIntOrNull() ?: 0)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}

// ---------------------------------------------------------------------------------------------
// Categories
// ---------------------------------------------------------------------------------------------

@Composable
fun CategoriesAdminScreen(viewModel: CatalogAdminViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories, key = { it.id }) { c ->
                AdminCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(c.colorArgb)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.nameAr + " / " + c.nameFr, style = MaterialTheme.typography.titleSmall)
                            Text("${products.count { it.entity.categoryId == c.id }} منتج", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(c.isActive, { viewModel.setCategoryActive(c.id, it) })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { adding = true }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("قسم جديد") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
    if (adding) {
        var ar by remember { mutableStateOf("") }
        var fr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("إضافة قسم") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(ar, { ar = it }, label = { Text("الاسم بالعربية") }, singleLine = true)
                    OutlinedTextField(fr, { fr = it }, label = { Text("الاسم بالفرنسية") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = ar.isNotBlank(), onClick = {
                    viewModel.saveCategory(CategoryEntity(slug(fr.ifBlank { ar }), ar.trim(), fr.trim(), colorArgb = 0xFF9C6B30L, sortOrder = categories.size))
                    adding = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text("إلغاء") } },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Inventory
// ---------------------------------------------------------------------------------------------

@Composable
fun InventoryScreen(viewModel: CatalogAdminViewModel = hiltViewModel()) {
    val rows by viewModel.inventory.collectAsStateWithLifecycle()
    var lowOnly by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MadakChip("الكل (${rows.size})", !lowOnly, { lowOnly = false })
            MadakChip("مخزون منخفض (${rows.count { it.stock <= it.lowStockThreshold }})", lowOnly, { lowOnly = true })
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows.filter { !lowOnly || it.stock <= it.lowStockThreshold }, key = { it.variantId }) { r ->
                AdminCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${r.productNameAr} • ${r.weightGrams} غ", style = MaterialTheme.typography.titleSmall)
                            Text("${r.sku} • ${dzd(r.priceDzd)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            if (r.stock <= 0) StatusPill("نفد المخزون", MadakColors.Paprika)
                            else if (r.stock <= r.lowStockThreshold) StatusPill("منخفض", MadakColors.Warning)
                        }
                        FilledTonalButton(onClick = { viewModel.adjustStock(r.variantId, -1) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)) { Text("−") }
                        Text(r.stock.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 10.dp))
                        FilledTonalButton(onClick = { viewModel.adjustStock(r.variantId, 1) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)) { Text("+") }
                        TextButton(onClick = { viewModel.adjustStock(r.variantId, 10) }) { Text("+10") }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Customers
// ---------------------------------------------------------------------------------------------

@Composable
fun CustomersScreen(viewModel: CatalogAdminViewModel = hiltViewModel()) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("${customers.size} زبون", style = MaterialTheme.typography.titleMedium) }
        items(customers, key = { it.phone }) { c ->
            AdminCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(MadakColors.MagentaSoft), contentAlignment = Alignment.Center) {
                        Text(c.customerName.take(1), color = MadakColors.MagentaDeep, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.customerName, style = MaterialTheme.typography.titleSmall)
                        Text("${c.phone} • ${c.wilaya}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${c.ordersCount} طلب", style = MaterialTheme.typography.labelLarge)
                        Text(dzd(c.totalSpentDzd), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
