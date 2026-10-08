package com.madak.spices.feature.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.model.Dish
import com.madak.spices.data.model.Product
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.CatalogRepository
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.designsystem.component.ProductImage
import com.madak.spices.designsystem.component.QuantityStepper
import com.madak.spices.designsystem.component.RatingBadge
import com.madak.spices.designsystem.component.SkeletonBox
import com.madak.spices.designsystem.component.StatusPill
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.FavoriteButton
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.price
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {
    private val productId: String = checkNotNull(savedState["id"])
    val product: StateFlow<Product?> = catalog.product(productId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite() = viewModelScope.launch { catalog.toggleFavorite(productId) }
    suspend fun addToCart(variantId: String, quantity: Int) = cart.add(productId, variantId, quantity)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(onBack: () -> Unit, onDish: (Dish) -> Unit, viewModel: ProductDetailViewModel = hiltViewModel()) {
    val product by viewModel.product.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val p = product
    if (p == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
            SkeletonBox(Modifier.fillMaxWidth().aspectRatio(1f))
            Spacer(Modifier.height(16.dp))
            SkeletonBox(Modifier.fillMaxWidth(0.6f).height(28.dp))
        }
        return
    }
    var selectedVariantId by remember(p.id) { mutableStateOf(p.defaultVariant?.id) }
    var quantity by remember(p.id) { mutableStateOf(1) }
    val variant = p.variants.firstOrNull { it.id == selectedVariantId } ?: p.defaultVariant
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = 110.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                ProductImage(
                    p.entity.imageUrl, p.entity.colorArgb, p.id,
                    Modifier.fillMaxSize().graphicsLayer {
                        translationY = scroll.value * 0.45f
                        val s = 1f + (scroll.value / 3000f)
                        scaleX = s; scaleY = s
                    },
                )
            }
            Column(
                Modifier
                    .offset(y = (-28).dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name(language), style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        if (p.entity.reviewsCount > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingBadge(p.entity.rating)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.detail_reviews, p.entity.reviewsCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    StockPill(variant?.stock ?: 0, variant?.lowStockThreshold ?: 10)
                }
                Spacer(Modifier.height(22.dp))
                Text(stringResource(R.string.detail_choose_weight), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.sortedVariants.forEach { v ->
                        MadakChip(
                            stringResource(R.string.weight_grams, v.weightGrams) + " • " + price(v.priceDzd),
                            selected = v.id == variant?.id, enabled = v.stock > 0,
                            onClick = { selectedVariantId = v.id },
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.detail_quantity), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    QuantityStepper(quantity, { if (quantity > 1) quantity-- }, { if (quantity < (variant?.stock ?: 1).coerceAtMost(99)) quantity++ })
                }
                InfoSection(stringResource(R.string.detail_description), p.description(language))
                InfoSection(stringResource(R.string.detail_usage), language.pick(p.entity.usageAr, p.entity.usageFr))
                InfoSection(stringResource(R.string.detail_origin), language.pick(p.entity.originAr, p.entity.originFr))
                val dishes = Dish.entries.filter { it.relevance(p.entity.nameAr, p.entity.nameFr, p.entity.tags) != null }
                if (dishes.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text(stringResource(R.string.detail_pairs_with), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dishes.forEach { d -> MadakChip(d.name(language), false, { onDish(d) }, leading = d.emoji) }
                    }
                }
            }
        }

        // Floating top controls
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)).bounceClick(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
            Spacer(Modifier.weight(1f))
            FavoriteButton(p.isFavorite, { viewModel.toggleFavorite() }, Modifier.size(42.dp))
        }

        AddToCartBar(
            total = (variant?.priceDzd ?: 0) * quantity,
            enabled = variant != null && variant.stock > 0,
            onAdd = { variant?.let { viewModel.addToCart(it.id, quantity) } },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun StockPill(stock: Int, threshold: Int) {
    when {
        stock <= 0 -> StatusPill(stringResource(R.string.detail_out_of_stock), MadakColors.Paprika)
        stock <= threshold -> StatusPill(stringResource(R.string.detail_low_stock, stock), MadakColors.Warning)
        else -> StatusPill(stringResource(R.string.detail_in_stock), MadakColors.Success)
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    if (body.isBlank()) return
    Spacer(Modifier.height(18.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun AddToCartBar(total: Int, enabled: Boolean, onAdd: suspend () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var added by remember { mutableStateOf(false) }
    LaunchedEffect(added) { if (added) { kotlinx.coroutines.delay(1400); added = false } }
    val pulse by animateFloatAsState(if (added) 1.04f else 1f, label = "addPulse")

    Surface(modifier.fillMaxWidth(), shadowElevation = 16.dp, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Row(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.4f)) {
                Text(stringResource(R.string.detail_total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedContent(total, transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) }, label = "total") {
                    Text(price(it), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        onAdd()
                        added = true
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(context.getString(R.string.action_added))
                    }
                },
                enabled = enabled,
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(0.6f).height(54.dp).graphicsLayer { scaleX = pulse; scaleY = pulse },
            ) {
                Icon(Icons.Rounded.ShoppingBag, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (added) R.string.action_added else R.string.action_add_to_cart))
            }
        }
    }
}
