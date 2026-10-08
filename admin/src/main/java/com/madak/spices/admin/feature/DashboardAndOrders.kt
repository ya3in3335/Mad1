package com.madak.spices.admin.feature

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.admin.ui.AdminCard
import com.madak.spices.admin.ui.LabeledValue
import com.madak.spices.admin.ui.StatCard
import com.madak.spices.admin.ui.actionAr
import com.madak.spices.admin.ui.adminDate
import com.madak.spices.admin.ui.dzd
import com.madak.spices.admin.ui.labelAr
import com.madak.spices.admin.ui.tint
import com.madak.spices.data.local.entity.BestSellerRow
import com.madak.spices.data.local.entity.OrderWithItems
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.model.DashboardStats
import com.madak.spices.data.model.OrderStatus
import com.madak.spices.data.model.wilayaLabel
import com.madak.spices.data.repository.AdminRepository
import com.madak.spices.data.repository.OrderRepository
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.designsystem.component.StatusPill
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(admin: AdminRepository, orders: OrderRepository) : ViewModel() {
    val stats: StateFlow<DashboardStats> = admin.dashboardStats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())
    val bestSellers: StateFlow<List<BestSellerRow>> = admin.bestSellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val pending: StateFlow<List<OrderWithItems>> = orders.ordersByStatus(OrderStatus.pendingStatuses)
        .map { it.take(5) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val lowStock: StateFlow<Int> = admin.inventory.map { list -> list.count { it.stock <= it.lowStockThreshold } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@Composable
fun DashboardScreen(adminName: String, onOrder: (Long) -> Unit, onInventory: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val best by viewModel.bestSellers.collectAsStateWithLifecycle()
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    val lowStock by viewModel.lowStock.collectAsStateWithLifecycle()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("مرحباً $adminName 👋", style = MaterialTheme.typography.headlineSmall)
            Text("ملخص نشاط متجر مذاق اليوم", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("طلبات اليوم", stats.todayOrders, Icons.Rounded.ShoppingCart, MadakColors.Magenta, Modifier.weight(1f))
                StatCard("مبيعات اليوم", stats.todaySalesDzd, Icons.Rounded.Payments, MadakColors.Gold, Modifier.weight(1f), money = true)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("طلبات قيد المعالجة", stats.pendingOrders, Icons.Rounded.HourglassTop, MadakColors.Warning, Modifier.weight(1f))
                StatCard("طلبات مكتملة", stats.completedOrders, Icons.Rounded.CheckCircle, MadakColors.Success, Modifier.weight(1f))
            }
        }
        if (lowStock > 0) {
            item {
                AdminCard(Modifier.bounceClick(onClick = onInventory)) {
                    Text("⚠️ $lowStock عناصر مخزونها منخفض — اضغط للمراجعة", color = MadakColors.Paprika, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        item {
            AdminCard {
                Column {
                    Text("الأكثر مبيعاً", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    val max = best.maxOfOrNull { it.quantity }?.coerceAtLeast(1) ?: 1
                    if (best.isEmpty()) Text("لا توجد مبيعات بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    best.forEachIndexed { i, row ->
                        val fraction by animateFloatAsState(row.quantity / max.toFloat(), tween(900, delayMillis = i * 80), label = "best")
                        Column(Modifier.padding(vertical = 5.dp)) {
                            Row {
                                Text("${i + 1}. ${row.productNameAr}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("${row.quantity} قطعة • ${dzd(row.revenueDzd)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(50)).background(MadakColors.Magenta))
                            }
                        }
                    }
                }
            }
        }
        item { Text("طلبات تنتظر المعالجة", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp)) }
        items(pending, key = { it.order.id }) { OrderRow(it, onClick = { onOrder(it.order.id) }) }
    }
}

@Composable
fun OrderRow(o: OrderWithItems, onClick: () -> Unit) {
    AdminCard(Modifier.bounceClick(onClick = onClick)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(o.order.orderNumber, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusPill(o.order.status.labelAr, o.order.status.tint)
            }
            Text("${o.order.customerName} • ${o.order.phone}", style = MaterialTheme.typography.bodyMedium)
            Row {
                Text(adminDate(o.order.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(dzd(o.order.totalDzd), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersAdminViewModel @Inject constructor(private val orders: OrderRepository) : ViewModel() {
    val filter = MutableStateFlow<OrderStatus?>(null)
    val list: StateFlow<List<OrderWithItems>> = filter.flatMapLatest { f ->
        if (f == null) orders.orders else orders.ordersByStatus(listOf(f))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun order(id: Long) = orders.order(id)
    fun move(id: Long, status: OrderStatus) = viewModelScope.launch { orders.updateStatus(id, status, "admin") }
}

@Composable
fun OrdersAdminScreen(openOrderId: Long?, onOrderOpened: (Long?) -> Unit, viewModel: OrdersAdminViewModel = hiltViewModel()) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val list by viewModel.list.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { MadakChip("الكل", filter == null, { viewModel.filter.value = null }) }
            items(OrderStatus.entries) { s -> MadakChip(s.labelAr, filter == s, { viewModel.filter.value = s }) }
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (list.isEmpty()) item { Text("لا توجد طلبات", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
            items(list, key = { it.order.id }) { o -> OrderRow(o) { onOrderOpened(o.order.id) } }
        }
    }
    if (openOrderId != null) {
        val order by remember(openOrderId) { viewModel.order(openOrderId) }.collectAsStateWithLifecycle(null)
        order?.let { OrderDetailDialog(it, onMove = { s -> viewModel.move(it.order.id, s) }, onDismiss = { onOrderOpened(null) }) }
    }
}

@Composable
private fun OrderDetailDialog(o: OrderWithItems, onMove: (OrderStatus) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var confirmCancel by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(o.order.orderNumber, modifier = Modifier.weight(1f))
                StatusPill(o.order.status.labelAr, o.order.status.tint)
            }
        },
        text = {
            LazyColumn {
                item {
                    LabeledValue("الزبون", o.order.customerName)
                    LabeledValue("الهاتف", o.order.phone)
                    LabeledValue("الولاية", o.order.wilayaLabel(AppLanguage.ARABIC))
                    LabeledValue("البلدية", o.order.commune)
                    LabeledValue("العنوان", o.order.address)
                    if (o.order.notes.isNotBlank()) LabeledValue("ملاحظات", o.order.notes)
                    LabeledValue("الدفع", "عند الاستلام")
                    LabeledValue("التاريخ", adminDate(o.order.createdAt))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
                items(o.items) { i ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Text("${i.quantity} × ${i.productNameAr} (${i.weightGrams} غ)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(dzd(i.lineTotalDzd), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    LabeledValue("التوصيل", dzd(o.order.deliveryFeeDzd))
                    LabeledValue("الإجمالي", dzd(o.order.totalDzd))
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        runCatching {
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${o.order.phone}")).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Call, null); Spacer(Modifier.width(6.dp)); Text("الاتصال بالزبون للتأكيد")
                    }
                    o.order.status.next()?.let { next ->
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { onMove(next) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = next.tint)) {
                            Text(next.actionAr)
                        }
                    }
                    if (o.order.status.canTransitionTo(OrderStatus.CANCELLED)) {
                        TextButton(onClick = { confirmCancel = true }, modifier = Modifier.fillMaxWidth()) { Text("إلغاء الطلب", color = MadakColors.Paprika) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } },
    )
    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("تأكيد الإلغاء") },
            text = { Text("سيتم إلغاء الطلب وإرجاع الكميات إلى المخزون.") },
            confirmButton = { TextButton(onClick = { onMove(OrderStatus.CANCELLED); confirmCancel = false }) { Text("إلغاء الطلب", color = MadakColors.Paprika) } },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("رجوع") } },
        )
    }
}
