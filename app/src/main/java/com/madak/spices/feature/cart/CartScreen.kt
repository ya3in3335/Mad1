package com.madak.spices.feature.cart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.model.CartLine
import com.madak.spices.data.model.CartSummary
import com.madak.spices.data.model.Wilayas
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.UserRepository
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.designsystem.component.ProductImage
import com.madak.spices.designsystem.component.QuantityStepper
import com.madak.spices.designsystem.component.SkeletonBox
import com.madak.spices.designsystem.component.SummaryRow
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.price
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cart: CartRepository,
    users: UserRepository,
) : ViewModel() {
    /** null while loading. Delivery fee is estimated from the saved address when available. */
    val summary: StateFlow<CartSummary?> = combine(cart.lines, users.defaultAddress) { lines, address ->
        CartSummary(lines, Wilayas.fromStorage(address?.wilaya)?.code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuantity(line: CartLine, qty: Int) = viewModelScope.launch { cart.setQuantity(line.itemId, qty) }
    fun changeWeight(line: CartLine, variantId: String) = viewModelScope.launch { cart.changeVariant(line.itemId, variantId) }
    fun remove(line: CartLine) = viewModelScope.launch { cart.remove(line.itemId) }
    fun restore(line: CartLine) = viewModelScope.launch { cart.add(line.product.id, line.variant.id, line.quantity) }
}

@Composable
fun CartScreen(onCheckout: () -> Unit, onBrowse: () -> Unit, onProduct: (String) -> Unit, viewModel: CartViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.cart_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            summary?.takeIf { !it.isEmpty }?.let {
                Text(stringResource(R.string.cart_items_count, it.itemCount), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val s = summary
        when {
            s == null -> Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { SkeletonBox(Modifier.fillMaxWidth().height(110.dp)) }
            }
            s.isEmpty -> EmptyState(
                Icons.Rounded.ShoppingBag, stringResource(R.string.cart_empty_title), stringResource(R.string.cart_empty_body),
                Modifier.fillMaxSize(), actionLabel = stringResource(R.string.action_browse), onAction = onBrowse,
            )
            else -> {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { FreeDeliveryProgress(s) }
                    items(s.lines, key = { it.itemId }) { line ->
                        CartLineCard(
                            line,
                            onQuantity = { viewModel.setQuantity(line, it) },
                            onWeight = { viewModel.changeWeight(line, it) },
                            onRemove = {
                                viewModel.remove(line)
                                scope.launch {
                                    val r = snackbar.showSnackbar(context.getString(R.string.cart_item_removed), actionLabel = context.getString(R.string.action_undo))
                                    if (r == androidx.compose.material3.SnackbarResult.ActionPerformed) viewModel.restore(line)
                                }
                            },
                            onClick = { onProduct(line.product.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                CartSummaryCard(s, onCheckout)
            }
        }
    }
}

@Composable
private fun FreeDeliveryProgress(s: CartSummary) {
    val progress by animateFloatAsState(
        (s.subtotal.toFloat() / com.madak.spices.data.model.Pricing.FREE_DELIVERY_THRESHOLD_DZD).coerceIn(0f, 1f),
        tween(600), label = "freeDelivery",
    )
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocalShipping, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (s.remainingForFreeDelivery == 0) stringResource(R.string.cart_free_delivery_unlocked)
                    else stringResource(R.string.cart_free_delivery_progress, price(s.remainingForFreeDelivery)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onQuantity: (Int) -> Unit,
    onWeight: (String) -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val haptic = LocalHapticFeedback.current
    var menu by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = modifier) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                line.product.entity.imageUrl, line.product.entity.colorArgb, line.product.id,
                Modifier.size(86.dp).clip(RoundedCornerShape(18.dp)).bounceClick(onClick = onClick),
                photoKey = line.product.photoKey,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(line.product.name(language), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRemove() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.action_remove), tint = MadakColors.Paprika)
                    }
                }
                Box {
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .bounceClick { menu = true }.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.weight_grams, line.variant.weightGrams), style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Rounded.ExpandMore, stringResource(R.string.cart_weight), modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        line.product.sortedVariants.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.weight_grams, v.weightGrams) + "  •  " + price(v.priceDzd)) },
                                onClick = { menu = false; onWeight(v.id) },
                                enabled = v.stock > 0,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(line.lineTotal, transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) }, label = "line", modifier = Modifier.weight(1f)) {
                        Text(price(it), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    QuantityStepper(line.quantity, { onQuantity(line.quantity - 1) }, { onQuantity(line.quantity + 1) }, compact = true)
                }
            }
        }
    }
}

@Composable
private fun CartSummaryCard(s: CartSummary, onCheckout: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), shadowElevation = 18.dp, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            SummaryRow(stringResource(R.string.cart_subtotal), price(s.subtotal))
            SummaryRow(
                stringResource(R.string.cart_delivery_fee),
                if (s.deliveryFee == 0) stringResource(R.string.cart_free) else price(s.deliveryFee),
                valueColor = if (s.deliveryFee == 0) MadakColors.Success else androidx.compose.ui.graphics.Color.Unspecified,
            )
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SummaryRow(stringResource(R.string.cart_total), price(s.total), emphasize = true)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onCheckout() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text(stringResource(R.string.action_checkout)) }
        }
    }
}
