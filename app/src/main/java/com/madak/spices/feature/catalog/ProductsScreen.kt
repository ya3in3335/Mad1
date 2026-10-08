package com.madak.spices.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.local.entity.CategoryEntity
import com.madak.spices.data.model.Product
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.CatalogRepository
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.designsystem.component.SkeletonBox
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.ProductCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val label: Int) { POPULAR(R.string.sort_popular), PRICE_LOW(R.string.sort_price_low), PRICE_HIGH(R.string.sort_price_high) }

data class ProductsUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: String? = null,
    val sort: SortMode = SortMode.POPULAR,
    val products: List<Product> = emptyList(),
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {
    private val category = MutableStateFlow(savedState.get<String>("category")?.takeIf { it.isNotBlank() })
    private val sort = MutableStateFlow(SortMode.POPULAR)
    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<ProductsUiState> = combine(catalog.products, catalog.categories, category, sort, refreshing) { products, cats, cat, s, r ->
        val filtered = products.filter { cat == null || it.entity.categoryId == cat }
        val sorted = when (s) {
            SortMode.POPULAR -> filtered.sortedWith(compareByDescending<Product> { it.entity.isBestSeller }.thenByDescending { it.entity.reviewsCount })
            SortMode.PRICE_LOW -> filtered.sortedBy { it.startingPrice }
            SortMode.PRICE_HIGH -> filtered.sortedByDescending { it.startingPrice }
        }
        ProductsUiState(false, r, cats, cat, s, sorted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun selectCategory(id: String?) { category.value = id }
    fun setSort(mode: SortMode) { sort.value = mode }
    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
    fun quickAdd(p: Product) = viewModelScope.launch { p.defaultVariant?.let { cart.add(p.id, it.id) } }
    fun refresh() = viewModelScope.launch { refreshing.value = true; catalog.refresh(); refreshing.value = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(onProduct: (String) -> Unit, onSearch: () -> Unit, viewModel: ProductsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.products_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.products_count, state.products.size),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, stringResource(R.string.search_title)) }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { MadakChip(stringResource(R.string.products_all), state.selectedCategory == null, { viewModel.selectCategory(null) }) }
            items(state.categories, key = { it.id }) { c ->
                MadakChip(language.pick(c.nameAr, c.nameFr), state.selectedCategory == c.id, { viewModel.selectCategory(c.id) })
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SortMode.entries) { m -> MadakChip(stringResource(m.label), state.sort == m, { viewModel.setSort(m) }) }
        }
        PullToRefreshBox(state.refreshing, onRefresh = { viewModel.refresh() }, modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.loading) {
                    items(6) { SkeletonBox(Modifier.fillMaxWidth().height(230.dp)) }
                }
                items(state.products, key = { it.id }) { p ->
                    ProductCard(
                        p, onClick = { onProduct(p.id) }, onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                        onQuickAdd = { viewModel.quickAdd(p) }, modifier = Modifier.animateItem(),
                    )
                }
                if (!state.loading && state.products.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.Storefront, stringResource(R.string.catalog_empty_title), stringResource(R.string.catalog_empty_body))
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Column(Modifier.height(8.dp)) {} }
            }
        }
    }
}
