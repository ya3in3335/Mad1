package com.madak.spices.feature.cook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.madak.spices.designsystem.component.ProductImage
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.MadakTopBar
import com.madak.spices.ui.QuickAddButton
import com.madak.spices.ui.price
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CookTodayViewModel @Inject constructor(
    savedState: SavedStateHandle,
    catalog: CatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {
    val dish = MutableStateFlow(Dish.entries.firstOrNull { it.name == savedState.get<String>("dish") } ?: Dish.CHICKEN)

    val recommendations: StateFlow<List<Product>> = dish
        .flatMapLatest { catalog.recommendationsFor(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(p: Product) = viewModelScope.launch { p.defaultVariant?.let { cart.add(p.id, it.id) } }

    suspend fun addAll(): Int {
        val list = recommendations.value.filter { it.inStock }
        list.forEach { p -> p.defaultVariant?.let { cart.add(p.id, it.id) } }
        return list.size
    }
}

@Composable
fun CookTodayScreen(onBack: () -> Unit, onProduct: (String) -> Unit, viewModel: CookTodayViewModel = hiltViewModel()) {
    val dish by viewModel.dish.collectAsStateWithLifecycle()
    val products by viewModel.recommendations.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        MadakTopBar(stringResource(R.string.cook_title), onBack)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Dish.entries) { d ->
                val selected = d == dish
                val bg by animateColorAsState(if (selected) Color(d.colorArgb) else MaterialTheme.colorScheme.surface, label = "dishBg")
                Column(
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(bg)
                        .bounceClick {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.dish.value = d
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(d.emoji, fontSize = 30.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(d.name(language), style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        AnimatedContent(
            dish,
            transitionSpec = { (fadeIn() + slideInHorizontally { it / 6 }) togetherWith fadeOut() },
            label = "dish",
            modifier = Modifier.weight(1f),
        ) { current ->
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(Color(current.colorArgb), MadakColors.Ink)))
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Lightbulb, null, tint = MadakColors.GoldSoft)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(stringResource(R.string.cook_tip), style = MaterialTheme.typography.labelLarge, color = MadakColors.GoldSoft)
                            Text(current.tip(language), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.cook_recommended, current.name(language)), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                }
                if (products.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.cook_empty), style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
                items(products, key = { it.id }) { p ->
                    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.animateItem().bounceClick { onProduct(p.id) }) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProductImage(p.entity.imageUrl, p.entity.colorArgb, p.id, Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)))
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.name(language), style = MaterialTheme.typography.titleMedium)
                                Text(p.description(language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                p.defaultVariant?.let {
                                    Text(stringResource(R.string.weight_grams, it.weightGrams) + " • " + price(it.priceDzd), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            QuickAddButton({ viewModel.add(p) }, enabled = p.inStock)
                        }
                    }
                }
            }
        }
        AnimatedContent(products.isNotEmpty(), transitionSpec = { scaleIn() togetherWith fadeOut() }, label = "addAll") { show ->
            if (show) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            val n = viewModel.addAll()
                            snackbar.showSnackbar(context.getString(R.string.cook_added_all, n))
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(54.dp),
                ) {
                    Icon(Icons.Rounded.AddShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.cook_add_all))
                }
            }
        }
    }
}
