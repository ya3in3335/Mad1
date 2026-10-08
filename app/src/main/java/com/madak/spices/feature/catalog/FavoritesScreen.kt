package com.madak.spices.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.model.Product
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.CatalogRepository
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.ui.MadakTopBar
import com.madak.spices.ui.ProductCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {
    val favorites: StateFlow<List<Product>?> = catalog.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
    fun quickAdd(p: Product) = viewModelScope.launch { p.defaultVariant?.let { cart.add(p.id, it.id) } }
}

@Composable
fun FavoritesScreen(onBack: () -> Unit, onProduct: (String) -> Unit, onBrowse: () -> Unit, viewModel: FavoritesViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        MadakTopBar(stringResource(R.string.favorites_title), onBack)
        val list = favorites ?: return@Column
        if (list.isEmpty()) {
            EmptyState(
                Icons.Rounded.FavoriteBorder, stringResource(R.string.favorites_empty_title), stringResource(R.string.favorites_empty_body),
                actionLabel = stringResource(R.string.action_browse), onAction = onBrowse,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(list, key = { it.id }) { p ->
                    ProductCard(p, { onProduct(p.id) }, { viewModel.toggleFavorite(p.id) }, { viewModel.quickAdd(p) }, Modifier.animateItem())
                }
            }
        }
    }
}
