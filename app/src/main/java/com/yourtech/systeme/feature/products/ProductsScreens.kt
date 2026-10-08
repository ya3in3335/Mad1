package com.yourtech.systeme.feature.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.Product
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.data.repository.CatalogRepository
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.KeyValueRow
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.ui.Launcher
import com.yourtech.systeme.ui.LocalLanguage
import com.yourtech.systeme.ui.TopBar
import com.yourtech.systeme.ui.color
import com.yourtech.systeme.ui.label
import com.yourtech.systeme.ui.money
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun ProductCard(p: Product, onClick: () -> Unit, onFavorite: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(22.dp)).background(YT.Surface).pressable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            MediaImage(p.image, p.photoKey, Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(34.dp).clip(CircleShape).background(YT.Navy.copy(alpha = 0.7f)).pressable(onClick = onFavorite),
                contentAlignment = Alignment.Center,
            ) { Icon(if (p.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (p.isFavorite) YT.Cyan else Color.White, modifier = Modifier.size(18.dp)) }
        }
        Column(Modifier.padding(12.dp)) {
            if (p.entity.brand.isNotBlank()) Text(p.entity.brand.uppercase(), style = MaterialTheme.typography.labelSmall, color = YT.Cyan)
            Text(p.entity.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                if (p.hasPrice) money(p.entity.priceDzd!!) else stringResource(R.string.product_price_on_request),
                style = MaterialTheme.typography.labelLarge, color = if (p.hasPrice) YT.White else YT.TextMuted,
            )
        }
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductsViewModel @Inject constructor(private val catalog: CatalogRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val category = MutableStateFlow<String?>(null)
    val categories: StateFlow<List<ProductCategoryEntity>> = catalog.productCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val products: StateFlow<List<Product>?> = combine(
        query.debounce(250).flatMapLatest { q -> if (q.isBlank()) catalog.products else catalog.search(q) },
        category,
    ) { list, c -> list.filter { c == null || it.entity.categoryId == c } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
}

@Composable
fun ProductsScreen(onProduct: (String) -> Unit, onQuote: () -> Unit, onFavorites: () -> Unit, viewModel: ProductsViewModel = hiltViewModel()) {
    val q by viewModel.query.collectAsStateWithLifecycle()
    val cat by viewModel.category.collectAsStateWithLifecycle()
    val cats by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.products_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = onFavorites) { Icon(Icons.Rounded.FavoriteBorder, stringResource(R.string.favorites)) }
        }
        OutlinedTextField(
            q, { viewModel.query.value = it }, singleLine = true, shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = YT.Cyan) },
            trailingIcon = { if (q.isNotEmpty()) IconButton(onClick = { viewModel.query.value = "" }) { Icon(Icons.Rounded.Close, null) } },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = YT.Outline, focusedBorderColor = YT.Cyan, unfocusedContainerColor = YT.Surface, focusedContainerColor = YT.Surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Chip(stringResource(R.string.products_all), cat == null, { viewModel.category.value = null }) }
            items(cats, key = { it.id }) { c -> Chip(c.name(l), cat == c.id, { viewModel.category.value = c.id }) }
        }
        val list = products
        when {
            list == null -> Unit
            list.isEmpty() && q.isBlank() && cat == null -> EmptyState(
                Icons.Rounded.Inventory2, stringResource(R.string.products_empty_title), stringResource(R.string.products_empty_body),
                action = stringResource(R.string.request_quote), onAction = onQuote,
            )
            list.isEmpty() -> EmptyState(Icons.Rounded.Search, stringResource(R.string.search_empty), "")
            else -> LazyVerticalGrid(
                GridCells.Adaptive(160.dp), contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.id }) { p -> ProductCard(p, { onProduct(p.id) }, { viewModel.toggleFavorite(p.id) }, Modifier.animateItem()) }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(saved: SavedStateHandle, private val catalog: CatalogRepository, content: ContentRepository) : ViewModel() {
    private val id: String = checkNotNull(saved["id"])
    val product: StateFlow<Product?> = catalog.product(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val business: StateFlow<BusinessInfoEntity> = content.business.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusinessDefaults.info)
    fun toggleFavorite() = viewModelScope.launch { catalog.toggleFavorite(id) }
}

@Composable
fun ProductDetailScreen(onBack: () -> Unit, onQuote: (String) -> Unit, viewModel: ProductDetailViewModel = hiltViewModel()) {
    val product by viewModel.product.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val p = product ?: return
    val e = p.entity
    val wa = stringResource(R.string.wa_hello) + " " + listOf(e.brand, e.name, e.model).filter { it.isNotBlank() }.joinToString(" ")
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 150.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                MediaImage(p.image, p.photoKey, Modifier.fillMaxSize())
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                    CircleButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
                    Spacer(Modifier.weight(1f))
                    CircleButton({ viewModel.toggleFavorite() }) { Icon(if (p.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (p.isFavorite) YT.Cyan else Color.White) }
                }
            }
            Column(Modifier.padding(20.dp)) {
                if (e.brand.isNotBlank()) Text(e.brand.uppercase(), style = MaterialTheme.typography.labelLarge, color = YT.Cyan)
                Text(e.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (p.hasPrice) money(e.priceDzd!!) else stringResource(R.string.product_price_on_request),
                        style = MaterialTheme.typography.titleLarge, color = if (p.hasPrice) YT.White else YT.TextMuted, modifier = Modifier.weight(1f),
                    )
                    StatusPill(stringResource(e.stockStatus.label), e.stockStatus.color)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                }
                if (e.description.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.product_description), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(e.description, style = MaterialTheme.typography.bodyLarge, color = YT.TextMuted)
                }
                val specs = buildList {
                    if (e.brand.isNotBlank()) add(stringResource(R.string.product_brand) to e.brand)
                    if (e.model.isNotBlank()) add(stringResource(R.string.product_model) to e.model)
                    addAll(p.specs)
                }
                if (specs.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.product_specs), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        specs.forEach { (k, v) -> KeyValueRow(k, v) }
                    }
                }
                if (e.warranty.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.VerifiedUser, null, tint = YT.Success)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(stringResource(R.string.product_warranty), style = MaterialTheme.typography.labelLarge)
                                Text(e.warranty, style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted)
                            }
                        }
                    }
                }
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, YT.Navy, YT.Navy)))
                .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GradientButton(stringResource(R.string.request_quote), { onQuote(p.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.RequestQuote)
            OutlineButton(stringResource(R.string.whatsapp_inquiry), { Launcher.whatsapp(context, business, wa) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Chat, tint = YT.Success)
        }
    }
}

@Composable
private fun CircleButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(YT.Navy.copy(alpha = 0.7f)).pressable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val catalog: CatalogRepository) : ViewModel() {
    val favorites: StateFlow<List<Product>?> = catalog.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun toggle(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
}

@Composable
fun FavoritesScreen(onBack: () -> Unit, onProduct: (String) -> Unit, viewModel: FavoritesViewModel = hiltViewModel()) {
    val list by viewModel.favorites.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.favorites), onBack)
        val l = list ?: return@Column
        if (l.isEmpty()) EmptyState(Icons.Rounded.FavoriteBorder, stringResource(R.string.favorites_empty), "")
        else LazyVerticalGrid(GridCells.Adaptive(160.dp), contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(l, key = { it.id }) { p -> ProductCard(p, { onProduct(p.id) }, { viewModel.toggle(p.id) }) }
        }
    }
}

