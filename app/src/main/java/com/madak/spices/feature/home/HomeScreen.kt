package com.madak.spices.feature.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.local.entity.OfferEntity
import com.madak.spices.data.model.Dish
import com.madak.spices.data.model.Product
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.CatalogRepository
import com.madak.spices.data.repository.NotificationRepository
import com.madak.spices.data.repository.UserRepository
import com.madak.spices.designsystem.component.AnimatedCountBadge
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.designsystem.component.MadakWordmark
import com.madak.spices.designsystem.component.SectionHeader
import com.madak.spices.designsystem.component.SkeletonBox
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.ProductCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val userName: String? = null,
    val unread: Int = 0,
    val categories: List<CategoryEntity> = emptyList(),
    val offers: List<OfferEntity> = emptyList(),
    val bestSellers: List<Product> = emptyList(),
    val featured: List<Product> = emptyList(),
    val allProducts: List<Product> = emptyList(),
) {
    val isCatalogEmpty: Boolean get() = allProducts.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
    users: UserRepository,
    notifications: NotificationRepository,
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val _events = MutableSharedFlow<CatalogRepository.RefreshResult>()
    val events: SharedFlow<CatalogRepository.RefreshResult> = _events

    val state: StateFlow<HomeUiState> = combine(
        catalog.products, catalog.categories, catalog.activeOffers,
        combine(users.currentUser, notifications.unreadCount) { u, n -> u to n },
        refreshing,
    ) { products, categories, offers, (user, unread), isRefreshing ->
        HomeUiState(
            loading = false,
            refreshing = isRefreshing,
            userName = user?.fullName?.substringBefore(' '),
            unread = unread,
            categories = categories,
            offers = offers,
            bestSellers = products.filter { it.entity.isBestSeller },
            featured = products.filter { it.entity.isFeatured },
            allProducts = products,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            val result = catalog.refresh()
            refreshing.value = false
            _events.emit(result)
        }
    }

    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }

    fun quickAdd(product: Product) = viewModelScope.launch {
        product.defaultVariant?.let { cart.add(product.id, it.id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onProduct: (String) -> Unit,
    onCategory: (String) -> Unit,
    onSeeAll: () -> Unit,
    onDish: (Dish) -> Unit,
    onNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { result ->
            val msg = if (result == CatalogRepository.RefreshResult.OFFLINE) R.string.home_offline else R.string.home_refreshed
            snackbar.showSnackbar(context.getString(msg))
        }
    }

    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { HomeHeader(state.userName, state.unread, onSearch, onNotifications) }
            if (state.offers.isNotEmpty()) item { OffersPager(state.offers, onProduct) }
            item { CookTodayCard(onDish) }
            item { SectionHeader(stringResource(R.string.home_categories)) }
            item { CategoriesRow(state.categories, state.loading, onCategory) }
            if (!state.loading && state.bestSellers.isEmpty() && state.featured.isEmpty() && state.isCatalogEmpty) {
                item {
                    EmptyState(
                        Icons.Rounded.Storefront, stringResource(R.string.catalog_empty_title), stringResource(R.string.catalog_empty_body),
                    )
                }
                return@LazyColumn
            }
            if (state.bestSellers.isNotEmpty() || state.loading) item { SectionHeader(stringResource(R.string.home_best_sellers), action = stringResource(R.string.action_see_all), onAction = onSeeAll) }
            if (state.loading || state.bestSellers.isNotEmpty()) item {
                if (state.loading) SkeletonRow()
                else LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(state.bestSellers, key = { it.id }) { p ->
                        ProductCard(
                            p, onClick = { onProduct(p.id) }, onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                            onQuickAdd = { viewModel.quickAdd(p) }, modifier = Modifier.width(172.dp).animateItem(),
                        )
                    }
                }
            }
            val featured = state.featured.ifEmpty { state.allProducts.filterNot { it.entity.isBestSeller } }
            if (featured.isNotEmpty() || state.loading) item { SectionHeader(stringResource(R.string.home_featured), action = stringResource(R.string.action_see_all), onAction = onSeeAll) }
            if (state.loading) item { SkeletonRow() }
            featured.chunked(2).forEach { row ->
                item(key = "featured_" + row.joinToString { it.id }) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp).animateItem(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { p ->
                            ProductCard(
                                p, onClick = { onProduct(p.id) }, onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                                onQuickAdd = { viewModel.quickAdd(p) }, modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String?, unread: Int, onSearch: () -> Unit, onNotifications: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(Brush.verticalGradient(listOf(MadakColors.Ink, MadakColors.Charcoal)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MadakWordmark(Modifier.height(44.dp))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNotifications) {
                AnimatedCountBadge(unread) {
                    Icon(Icons.Rounded.Notifications, stringResource(R.string.notifications_title), tint = Color.White)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (userName.isNullOrBlank()) stringResource(R.string.home_greeting) else stringResource(R.string.home_greeting_name, userName),
            style = MaterialTheme.typography.headlineSmall, color = Color.White,
        )
        Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = MadakColors.GoldSoft.copy(alpha = 0.8f))
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.09f))
                .bounceClick(onClick = onSearch)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, tint = MadakColors.Gold)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.home_search_hint), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OffersPager(offers: List<OfferEntity>, onProduct: (String) -> Unit) {
    val language = LocalAppLanguage.current
    val pager = rememberPagerState { offers.size }
    LaunchedEffect(offers.size) {
        while (offers.size > 1) {
            delay(4_000)
            pager.animateScrollToPage((pager.currentPage + 1) % offers.size)
        }
    }
    HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 12.dp, modifier = Modifier.padding(top = 18.dp)) { page ->
        val offer = offers[page]
        val color = Color(offer.colorArgb)
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(color, MadakColors.Ink)))
                .bounceClick { offer.productId?.let(onProduct) }
                .padding(20.dp),
        ) {
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.72f)) {
                Text(language.pick(offer.titleAr, offer.titleFr), style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text(language.pick(offer.subtitleAr, offer.subtitleFr), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                offer.promoCode?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.offer_code, it),
                        style = MaterialTheme.typography.labelLarge, color = MadakColors.Ink,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(MadakColors.GoldSoft).padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
            if (offer.discountPercent > 0) {
                Box(
                    Modifier.align(Alignment.CenterEnd).size(78.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) { Text(stringResource(R.string.offer_badge, offer.discountPercent), color = Color.White, fontSize = 22.sp, style = MaterialTheme.typography.headlineSmall) }
            }
        }
    }
}

@Composable
private fun CookTodayCard(onDish: (Dish) -> Unit) {
    val language = LocalAppLanguage.current
    Column(
        Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            .padding(vertical = 18.dp),
    ) {
        Text(stringResource(R.string.cook_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 18.dp))
        Text(
            stringResource(R.string.cook_subtitle), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 18.dp),
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Dish.entries) { dish ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.bounceClick { onDish(dish) }) {
                    Box(
                        Modifier.size(62.dp).clip(CircleShape).background(Color(dish.colorArgb).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(dish.emoji, fontSize = 28.sp) }
                    Spacer(Modifier.height(6.dp))
                    Text(dish.name(language), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun CategoriesRow(categories: List<CategoryEntity>, loading: Boolean, onCategory: (String) -> Unit) {
    val language = LocalAppLanguage.current
    if (loading) {
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { SkeletonBox(Modifier.weight(1f).height(96.dp)) }
        }
        return
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories, key = { it.id }) { c ->
            val color = Color(c.colorArgb)
            Column(
                Modifier
                    .width(150.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.55f))))
                    .bounceClick { onCategory(c.id) }
                    .padding(16.dp),
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)))
                Spacer(Modifier.height(26.dp))
                Text(language.pick(c.nameAr, c.nameFr), style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    language.pick(c.descriptionAr, c.descriptionFr), style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SkeletonRow() {
    Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(2) {
            Column(Modifier.weight(1f)) {
                SkeletonBox(Modifier.fillMaxWidth().height(160.dp))
                Spacer(Modifier.height(8.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
                Spacer(Modifier.height(6.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.4f).height(14.dp).graphicsLayer { alpha = 0.7f })
            }
        }
    }
}
