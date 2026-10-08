package com.yourtech.systeme.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.ProjectEntity
import com.yourtech.systeme.data.local.entity.PromotionEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.local.entity.TestimonialEntity
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.BusinessHours
import com.yourtech.systeme.data.model.Product
import com.yourtech.systeme.data.model.address
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.data.model.summary
import com.yourtech.systeme.data.repository.CatalogRepository
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.designsystem.component.BrandLockup
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.IconTile
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.OutlineButton
import com.yourtech.systeme.designsystem.component.SectionHeader
import com.yourtech.systeme.designsystem.component.SecurityIllustration
import com.yourtech.systeme.designsystem.component.StatusPill
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.feature.products.ProductCard
import com.yourtech.systeme.ui.Launcher
import com.yourtech.systeme.ui.LocalLanguage
import com.yourtech.systeme.ui.dayName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val business: BusinessInfoEntity = BusinessDefaults.info,
    val services: List<ServiceCategoryEntity> = emptyList(),
    val products: List<Product> = emptyList(),
    val promotions: List<PromotionEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val testimonials: List<TestimonialEntity> = emptyList(),
    val unread: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val catalog: CatalogRepository, content: ContentRepository) : ViewModel() {
    val state: StateFlow<HomeState> = combine(
        content.business, catalog.services, catalog.products,
        combine(content.promotions, content.projects, content.testimonials) { a, b, c -> Triple(a, b, c) },
        content.unread,
    ) { business, services, products, (promos, projects, testimonials), unread ->
        HomeState(
            business, services, products.filter { it.entity.isFeatured }.ifEmpty { products }.take(8),
            promos, projects.take(6), testimonials, unread,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun toggleFavorite(id: String) = viewModelScope.launch { catalog.toggleFavorite(id) }
}

@Composable
fun HomeScreen(
    onInstallation: () -> Unit,
    onSupport: () -> Unit,
    onSolution: (String) -> Unit,
    onSolutions: () -> Unit,
    onProduct: (String) -> Unit,
    onProducts: () -> Unit,
    onProject: (Long) -> Unit,
    onPortfolio: () -> Unit,
    onContact: () -> Unit,
    onNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    val wa = stringResource(R.string.wa_hello)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandLockup(Modifier.height(40.dp))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNotifications) {
                    BadgedBox(badge = { if (s.unread > 0) Badge(containerColor = YT.Cyan, contentColor = YT.Navy) { Text(s.unread.toString()) } }) {
                        Icon(Icons.Rounded.Notifications, stringResource(R.string.notifications_title))
                    }
                }
            }
        }
        item { Hero(onInstallation) { Launcher.whatsapp(context, s.business, wa) } }
        item { OpenStatus(s.business) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(Icons.Rounded.Build, stringResource(R.string.quick_install), YT.Blue, Modifier.weight(1f), onInstallation)
                QuickAction(Icons.Rounded.SupportAgent, stringResource(R.string.quick_support), YT.Violet, Modifier.weight(1f), onSupport)
                QuickAction(Icons.Rounded.Chat, stringResource(R.string.quick_whatsapp), YT.Success, Modifier.weight(1f)) { Launcher.whatsapp(context, s.business, wa) }
                QuickAction(Icons.Rounded.Call, stringResource(R.string.quick_call), YT.Cyan, Modifier.weight(1f)) { Launcher.call(context, s.business.phone) }
            }
        }
        if (s.services.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.section_solutions), action = stringResource(R.string.see_all), onAction = onSolutions) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.services, key = { it.id }) { svc ->
                        Column(Modifier.width(168.dp).clip(RoundedCornerShape(22.dp)).background(YT.Surface).pressable { onSolution(svc.id) }) {
                            SecurityIllustration(svc.photoKey ?: svc.iconKey, Modifier.fillMaxWidth().aspectRatio(1.25f))
                            Column(Modifier.padding(12.dp)) {
                                Text(svc.name(l), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(svc.summary(l), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
        if (s.promotions.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.section_offers)) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.promotions, key = { it.id }) { p ->
                        Row(
                            Modifier.width(300.dp).clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF14284A))
                                .border(1.dp, YT.Gold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                .pressable { p.serviceCategoryId?.let(onSolution) ?: p.productId?.let(onProduct) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                                Text(p.body, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 3)
                            }
                            if (p.imagePath != null) {
                                Spacer(Modifier.width(10.dp))
                                MediaImage(p.imagePath, "shield", Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)))
                            }
                        }
                    }
                }
            }
        }
        if (s.products.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.section_products), action = stringResource(R.string.see_all), onAction = onProducts) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.products, key = { it.id }) { p ->
                        ProductCard(p, { onProduct(p.id) }, { viewModel.toggleFavorite(p.id) }, Modifier.width(176.dp))
                    }
                }
            }
        }
        if (s.projects.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.section_projects), action = stringResource(R.string.see_all), onAction = onPortfolio) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.projects, key = { it.id }) { pr ->
                        Column(Modifier.width(240.dp).clip(RoundedCornerShape(22.dp)).background(YT.Surface).pressable { onProject(pr.id) }) {
                            MediaImage(pr.photos.lines().firstOrNull { it.isNotBlank() }, "camera", Modifier.fillMaxWidth().aspectRatio(1.6f))
                            Column(Modifier.padding(12.dp)) {
                                Text(pr.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (pr.generalLocation.isNotBlank()) Text(pr.generalLocation, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                            }
                        }
                    }
                }
            }
        }
        if (s.testimonials.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.section_testimonials)) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.testimonials, key = { it.id }) { t ->
                        GlassCard(Modifier.width(260.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("★".repeat(t.rating.coerceIn(1, 5)), color = YT.Warning)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Rounded.Verified, null, tint = YT.Cyan, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("“${t.text}”", style = MaterialTheme.typography.bodyMedium, maxLines = 5)
                            Spacer(Modifier.height(6.dp))
                            Text("— ${t.authorName}", style = MaterialTheme.typography.labelMedium, color = YT.TextMuted)
                        }
                    }
                }
            }
        }
        item { SectionHeader(stringResource(R.string.section_visit)) }
        item { VisitCard(s.business, onContact) }
    }
}

@Composable
private fun Hero(onInstallation: () -> Unit, onWhatsApp: () -> Unit) {
    Box(
        Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF15233A), YT.Surface)))
            .border(1.dp, YT.GlassBorder, RoundedCornerShape(28.dp)),
    ) {
        SecurityIllustration("shield", Modifier.align(Alignment.TopEnd).size(118.dp).padding(6.dp), background = false)
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.hero_kicker).uppercase(), style = MaterialTheme.typography.labelMedium, color = YT.Gold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth(0.68f))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.hero_body), style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted, modifier = Modifier.fillMaxWidth(0.68f))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton(stringResource(R.string.request_installation), onInstallation, Modifier.weight(1f))
                OutlineButton(stringResource(R.string.quick_whatsapp), onWhatsApp, Modifier.weight(0.7f), icon = Icons.Rounded.Chat, tint = YT.Success)
            }
        }
    }
}

@Composable
fun OpenStatus(info: BusinessInfoEntity, modifier: Modifier = Modifier.padding(horizontal = 20.dp)) {
    val l = LocalLanguage.current
    val st = BusinessHours.status(info.openingHours)
    val text = if (st.isOpen) stringResource(R.string.open_now, BusinessHours.hhmm(st.closesAtMinutes ?: 0))
    else {
        val today = java.util.Calendar.getInstance(BusinessHours.ALGIERS).get(java.util.Calendar.DAY_OF_WEEK)
        val day = st.nextOpenDay?.let { if (it == today) stringResource(R.string.today) else dayName(it, l) } ?: ""
        stringResource(R.string.closed_now, day, st.opensAtMinutes?.let(BusinessHours::hhmm) ?: "")
    }
    StatusPill(text, if (st.isOpen) YT.Success else YT.Warning, modifier)
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(YT.Surface).pressable(onClick = onClick).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(icon, tint)
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}


@Composable
fun VisitCard(info: BusinessInfoEntity, onContact: () -> Unit) {
    val l = LocalLanguage.current
    val context = LocalContext.current
    Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YT.Surface).pressable(onClick = onContact)) {
        MapPreview(Modifier.fillMaxWidth().aspectRatio(1.8f))
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Place, null, tint = YT.Cyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(info.address(l), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, tint = YT.TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                OpenStatus(info, Modifier)
            }
            Spacer(Modifier.height(12.dp))
            GradientButton(stringResource(R.string.contact_directions), { Launcher.directions(context, info) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Directions)
        }
    }
}

/** Offline map of the shop surroundings (OpenStreetMap, bundled) with a pulsing marker. */
@Composable
fun MapPreview(modifier: Modifier) {
    Box(modifier) {
        Image(painterResource(R.drawable.store_map), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(YT.Navy.copy(alpha = 0.18f)))
        SecurityIllustration("pin", Modifier.align(Alignment.Center).size(64.dp), background = false)
        Box(Modifier.align(Alignment.Center).size(18.dp).clip(CircleShape).background(YT.Blue))
        Box(Modifier.align(Alignment.Center).size(8.dp).clip(CircleShape).background(Color.White))
        Text(
            stringResource(R.string.map_attribution), style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomEnd).background(Color.White.copy(alpha = 0.7f)).padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}
