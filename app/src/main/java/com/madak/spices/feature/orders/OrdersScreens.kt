package com.madak.spices.feature.orders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.BuildConfig
import com.madak.spices.R
import com.madak.spices.data.local.entity.OrderWithItems
import com.madak.spices.data.model.OrderStatus
import com.madak.spices.data.model.wilayaLabel
import com.madak.spices.data.repository.OrderRepository
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.designsystem.component.SkeletonBox
import com.madak.spices.designsystem.component.StatusPill
import com.madak.spices.designsystem.component.SummaryRow
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.Launcher
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.MadakTopBar
import com.madak.spices.ui.color
import com.madak.spices.ui.descriptionRes
import com.madak.spices.ui.formatDateTime
import com.madak.spices.ui.formatTime
import com.madak.spices.ui.labelRes
import com.madak.spices.ui.price
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OrdersViewModel @Inject constructor(orders: OrderRepository) : ViewModel() {
    val orders: StateFlow<List<OrderWithItems>?> = orders.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val orders: OrderRepository,
) : ViewModel() {
    private val orderId: Long = checkNotNull(savedState.get<String>("orderId")).toLong()
    val order: StateFlow<OrderWithItems?> = orders.order(orderId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun cancel() = viewModelScope.launch { orders.cancel(orderId) }

    /** Debug builds only: lets testers walk through the lifecycle without a back office. */
    fun simulateNext() = viewModelScope.launch {
        order.value?.order?.status?.next()?.let { orders.updateStatus(orderId, it, "demo") }
    }
}

@Composable
fun OrdersScreen(onOrder: (Long) -> Unit, onBrowse: () -> Unit, viewModel: OrdersViewModel = hiltViewModel()) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(stringResource(R.string.orders_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(20.dp))
        val list = orders
        when {
            list == null -> Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { SkeletonBox(Modifier.fillMaxWidth().height(110.dp)) }
            }
            list.isEmpty() -> EmptyState(
                Icons.AutoMirrored.Rounded.ReceiptLong, stringResource(R.string.orders_empty_title), stringResource(R.string.orders_empty_body),
                Modifier.fillMaxSize(), actionLabel = stringResource(R.string.action_browse), onAction = onBrowse,
            )
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list, key = { it.order.id }) { o ->
                    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.animateItem().bounceClick { onOrder(o.order.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(o.order.orderNumber, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                StatusPill(stringResource(o.order.status.labelRes), o.order.status.color)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(formatDateTime(o.order.createdAt, language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                o.items.joinToString(" • ") { language.pick(it.productNameAr, it.productNameFr) },
                                style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.orders_items, o.items.sumOf { it.quantity }), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text(price(o.order.totalDzd), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            if (!o.order.status.isTerminal) {
                                Spacer(Modifier.height(10.dp))
                                MiniProgress(o.order.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniProgress(status: OrderStatus) {
    val index = OrderStatus.timeline.indexOf(status)
    val target = (index + 1f) / OrderStatus.timeline.size
    val progress by animateFloatAsState(target, tween(800), label = "orderProgress")
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(50)).background(status.color))
    }
}

@Composable
fun OrderConfirmationScreen(onTrack: (Long) -> Unit, onContinue: () -> Unit, viewModel: OrderDetailViewModel = hiltViewModel()) {
    val order by viewModel.order.collectAsStateWithLifecycle()
    val circle = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val ring = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { ring.animateTo(1f, tween(900)) }
        circle.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        check.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
    }
    Column(
        Modifier.fillMaxSize().background(MadakColors.Ink).statusBarsPadding().navigationBarsPadding().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(MadakColors.Gold.copy(alpha = 1f - ring.value), radius = size.minDimension / 2f * (0.5f + ring.value * 0.5f), style = Stroke(3f))
            }
            Box(
                Modifier.size(112.dp).graphicsLayer { scaleX = circle.value; scaleY = circle.value }.clip(CircleShape).background(MadakColors.Magenta),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(64.dp).graphicsLayer { scaleX = check.value; scaleY = check.value })
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.confirm_title), style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.confirm_body), style = MaterialTheme.typography.bodyLarge, color = MadakColors.Beige.copy(alpha = 0.85f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        order?.let { o ->
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.07f)) {
                Column(Modifier.padding(horizontal = 28.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.confirm_order_number), color = MadakColors.GoldSoft, style = MaterialTheme.typography.labelMedium)
                    Text(o.order.orderNumber, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(price(o.order.totalDzd) + " • " + stringResource(R.string.checkout_cod), color = MadakColors.Beige, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { order?.let { onTrack(it.order.id) } },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) { Text(stringResource(R.string.action_track_order)) }
        TextButton(onClick = onContinue, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.action_continue_shopping), color = MadakColors.Beige)
        }
    }
}

@Composable
fun OrderTrackingScreen(onBack: () -> Unit, viewModel: OrderDetailViewModel = hiltViewModel()) {
    val order by viewModel.order.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        MadakTopBar(stringResource(R.string.tracking_title), onBack)
        val o = order ?: return@Column
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Surface(shape = RoundedCornerShape(26.dp), color = MadakColors.Ink) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(o.order.orderNumber, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
                            StatusPill(stringResource(o.order.status.labelRes), if (o.order.status == OrderStatus.CANCELLED) MadakColors.Paprika else MadakColors.GoldSoft)
                        }
                        Text(formatDateTime(o.order.createdAt, language), style = MaterialTheme.typography.bodySmall, color = MadakColors.Beige.copy(alpha = 0.7f))
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(o.order.status.descriptionRes), style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                }
            }
            item { Timeline(o) }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.tracking_items), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        o.items.forEach { i ->
                            SummaryRow("${i.quantity} × ${language.pick(i.productNameAr, i.productNameFr)} (${stringResource(R.string.weight_grams, i.weightGrams)})", price(i.lineTotalDzd))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        SummaryRow(stringResource(R.string.cart_subtotal), price(o.order.subtotalDzd))
                        SummaryRow(stringResource(R.string.cart_delivery_fee), if (o.order.deliveryFeeDzd == 0) stringResource(R.string.cart_free) else price(o.order.deliveryFeeDzd))
                        SummaryRow(stringResource(R.string.cart_total), price(o.order.totalDzd), emphasize = true)
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(stringResource(R.string.tracking_delivery_to), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(o.order.customerName + " • " + o.order.phone, style = MaterialTheme.typography.bodyMedium)
                        Text("${o.order.address}، ${o.order.commune}، ${o.order.wilayaLabel(language)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (o.order.notes.isNotBlank()) Text(o.order.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SupportAgent, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.tracking_need_help), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { Launcher.whatsapp(context, o.order.orderNumber) }) { Text(stringResource(R.string.action_whatsapp)) }
                    }
                }
            }
            if (o.order.status.canTransitionTo(OrderStatus.CANCELLED) && o.order.status == OrderStatus.NEW) {
                item {
                    OutlinedButton(onClick = viewModel::cancel, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Icon(Icons.Rounded.Close, null, tint = MadakColors.Paprika)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_cancel_order), color = MadakColors.Paprika)
                    }
                }
            }
            if (BuildConfig.DEBUG && o.order.status.next() != null) {
                item {
                    TextButton(onClick = viewModel::simulateNext, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tracking_demo_advance)) }
                }
            }
        }
    }
}

@Composable
private fun Timeline(o: OrderWithItems) {
    val language = LocalAppLanguage.current
    val status = o.order.status
    val reached = if (status == OrderStatus.CANCELLED) setOf(OrderStatus.NEW) else OrderStatus.timeline.take(OrderStatus.timeline.indexOf(status) + 1).toSet()
    val steps = if (status == OrderStatus.CANCELLED) listOf(OrderStatus.NEW, OrderStatus.CANCELLED) else OrderStatus.timeline
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(18.dp)) {
            steps.forEachIndexed { index, step ->
                val done = step in reached || step == status
                val current = step == status
                val event = o.events.lastOrNull { it.status == step }
                val pulse by animateFloatAsState(if (current) 1.15f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "step")
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(28.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }.clip(CircleShape)
                                .background(if (done) step.color else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) Icon(if (step == OrderStatus.CANCELLED) Icons.Rounded.Close else Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        if (index < steps.lastIndex) {
                            Box(Modifier.width(2.dp).height(36.dp).background(if (steps[index + 1] in reached || steps[index + 1] == status) step.color else MaterialTheme.colorScheme.surfaceVariant))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.padding(top = 2.dp)) {
                        Text(
                            stringResource(step.labelRes), style = MaterialTheme.typography.titleSmall,
                            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            event?.let { formatTime(it.timestamp, language) } ?: stringResource(step.descriptionRes),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
