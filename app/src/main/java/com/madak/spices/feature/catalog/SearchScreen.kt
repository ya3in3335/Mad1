package com.madak.spices.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.ProductCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {
    val query = MutableStateFlow("")

    val results: StateFlow<List<Product>?> = query
        .debounce(250)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(null) else catalog.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
    fun quickAdd(p: Product) = viewModelScope.launch { p.defaultVariant?.let { cart.add(p.id, it.id) } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onProduct: (String) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }
    val language = LocalAppLanguage.current
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.query.value = it },
                placeholder = { Text(stringResource(R.string.home_search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { viewModel.query.value = "" }) { Icon(Icons.Rounded.Close, null) }
                },
                modifier = Modifier.weight(1f).focusRequester(focus),
            )
        }
        val list = results
        when {
            list == null -> {
                Text(stringResource(R.string.search_suggestions), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(20.dp))
                val suggestions = if (language.isRtl) listOf("كمون", "دجاج", "رأس الحانوت", "بابريكا", "قرفة", "سمك") else listOf("Cumin", "Poulet", "Ras el hanout", "Paprika", "Cannelle", "Poisson")
                FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { s -> MadakChip(s, false, { viewModel.query.value = s }) }
                }
            }
            list.isEmpty() -> EmptyState(Icons.Rounded.SearchOff, stringResource(R.string.search_empty_title), stringResource(R.string.search_empty_body))
            else -> LazyVerticalGrid(
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
