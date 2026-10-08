package com.yourtech.systeme.feature.solutions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.benefits
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.data.model.specs
import com.yourtech.systeme.data.model.summary
import com.yourtech.systeme.data.model.useCases
import com.yourtech.systeme.data.repository.CatalogRepository
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.SecurityIllustration
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.ui.Launcher
import com.yourtech.systeme.ui.LocalLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SolutionsViewModel @Inject constructor(catalog: CatalogRepository) : ViewModel() {
    val services: StateFlow<List<ServiceCategoryEntity>?> = catalog.services.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun SolutionsScreen(onSolution: (String) -> Unit, viewModel: SolutionsViewModel = hiltViewModel()) {
    val services by viewModel.services.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(stringResource(R.string.section_solutions), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.hero_body), style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted)
        }
        items(services.orEmpty(), key = { it.id }) { svc ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YT.Surface).pressable { onSolution(svc.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SecurityIllustration(svc.photoKey ?: svc.iconKey, Modifier.size(112.dp))
                Column(Modifier.weight(1f).padding(14.dp)) {
                    Text(svc.name(l), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(svc.summary(l), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 3)
                }
            }
        }
    }
}

@HiltViewModel
class SolutionDetailViewModel @Inject constructor(saved: SavedStateHandle, catalog: CatalogRepository, content: ContentRepository) : ViewModel() {
    val service: StateFlow<ServiceCategoryEntity?> = catalog.service(checkNotNull(saved.get<String>("id"))).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val business: StateFlow<BusinessInfoEntity> = content.business.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusinessDefaults.info)
}

@Composable
fun SolutionDetailScreen(
    onBack: () -> Unit,
    onQuote: (String) -> Unit,
    onProducts: () -> Unit,
    viewModel: SolutionDetailViewModel = hiltViewModel(),
) {
    val svc by viewModel.service.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    val s = svc ?: return
    val waText = stringResource(R.string.wa_hello) + " " + s.name(l)
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 150.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1.15f)) {
                SecurityIllustration(s.photoKey ?: s.iconKey, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, YT.Navy))))
                Box(
                    Modifier.statusBarsPadding().padding(16.dp).size(42.dp).clip(CircleShape).background(YT.Glass).pressable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
            }
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(s.name(l), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(s.summary(l), style = MaterialTheme.typography.bodyLarge, color = YT.TextMuted)
                ListCard(stringResource(R.string.solution_benefits), s.benefits(l), Icons.Rounded.CheckCircle, YT.Success)
                ListCard(stringResource(R.string.solution_use_cases), s.useCases(l), Icons.Rounded.CheckCircle, YT.Cyan)
                ListCard(stringResource(R.string.solution_specs), s.specs(l), Icons.Rounded.Tune, YT.Blue)
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, YT.Navy, YT.Navy)))
                .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GradientButton(stringResource(R.string.request_quote), { onQuote(s.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.RequestQuote)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton(stringResource(R.string.tab_products), onProducts, Modifier.weight(1f))
                OutlineButton(stringResource(R.string.whatsapp_inquiry), { Launcher.whatsapp(context, business, waText) }, Modifier.weight(1f), icon = Icons.Rounded.Chat, tint = YT.Success)
            }
        }
    }
}

@Composable
private fun ListCard(title: String, items: List<String>, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    if (items.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    GlassCard(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        items.forEach {
            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Spacer(Modifier.width(10.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
