package com.yourtech.systeme.feature.more

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.BuildConfig
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.NotificationEntity
import com.yourtech.systeme.data.local.entity.ProjectEntity
import com.yourtech.systeme.data.model.AppLanguage
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.BusinessHours
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.address
import com.yourtech.systeme.data.model.phoneDisplay
import com.yourtech.systeme.data.model.tagline
import com.yourtech.systeme.data.model.whatsappDisplay
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.designsystem.component.BrandLockup
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.IconTile
import com.yourtech.systeme.designsystem.component.KeyValueRow
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.feature.home.MapPreview
import com.yourtech.systeme.feature.home.OpenStatus
import com.yourtech.systeme.ui.Launcher
import com.yourtech.systeme.ui.LocalLanguage
import com.yourtech.systeme.ui.TopBar
import com.yourtech.systeme.ui.dayName
import com.yourtech.systeme.ui.formatDate
import com.yourtech.systeme.ui.label
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BusinessViewModel @Inject constructor(content: ContentRepository) : ViewModel() {
    val business: StateFlow<BusinessInfoEntity> = content.business.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusinessDefaults.info)
}

@Composable
fun MoreScreen(
    onContact: () -> Unit, onPortfolio: () -> Unit, onFavorites: () -> Unit, onNotifications: () -> Unit,
    onAbout: () -> Unit, onPrivacy: () -> Unit, onLanguage: (AppLanguage) -> Unit,
) {
    val l = LocalLanguage.current
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        BrandLockup(Modifier.fillMaxWidth(0.7f))
        Spacer(Modifier.height(20.dp))
        MenuGroup {
            MenuItem(Icons.Rounded.Storefront, stringResource(R.string.contact_title), onContact)
            MenuItem(Icons.Rounded.PhotoLibrary, stringResource(R.string.portfolio_title), onPortfolio)
            MenuItem(Icons.Rounded.Favorite, stringResource(R.string.favorites), onFavorites)
            MenuItem(Icons.Rounded.Notifications, stringResource(R.string.more_notifications), onNotifications)
        }
        Spacer(Modifier.height(14.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Rounded.Language)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.more_language), style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("العربية", l == AppLanguage.ARABIC, { onLanguage(AppLanguage.ARABIC) })
                Chip("Français", l == AppLanguage.FRENCH, { onLanguage(AppLanguage.FRENCH) })
                Chip("English", l == AppLanguage.ENGLISH, { onLanguage(AppLanguage.ENGLISH) })
            }
        }
        Spacer(Modifier.height(14.dp))
        MenuGroup {
            MenuItem(Icons.Rounded.Info, stringResource(R.string.more_about), onAbout)
            MenuItem(Icons.Rounded.PrivacyTip, stringResource(R.string.more_privacy), onPrivacy)
        }
        Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall, color = YT.TextMuted, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun MenuGroup(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(YT.Surface).padding(vertical = 6.dp)) { content() }
}

@Composable
private fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().pressable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, size = 40)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = YT.TextMuted)
    }
}

@Composable
fun ContactScreen(onBack: () -> Unit, viewModel: BusinessViewModel = hiltViewModel()) {
    val b by viewModel.business.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    val wa = stringResource(R.string.wa_hello)
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.contact_title), onBack)
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { MapPreview(Modifier.fillMaxWidth().aspectRatio(1.6f).clip(RoundedCornerShape(24.dp))) }
            item { GradientButton(stringResource(R.string.contact_directions), { Launcher.directions(context, b) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Place) }
            item { ContactRow(Icons.Rounded.Chat, stringResource(R.string.quick_whatsapp), "⁦${b.whatsappDisplay()}⁩", YT.Success) { Launcher.whatsapp(context, b, wa) } }
            item { ContactRow(Icons.Rounded.Call, stringResource(R.string.quick_call), "⁦${b.phoneDisplay()}⁩", YT.Cyan) { Launcher.call(context, b.phone) } }
            if (b.email.isNotBlank()) item { ContactRow(Icons.Rounded.Email, stringResource(R.string.contact_email), b.email, YT.Blue) { Launcher.web(context, "mailto:${b.email}") } }
            item { ContactRow(Icons.Rounded.Place, stringResource(R.string.contact_address), b.address(l), YT.Violet) { Launcher.directions(context, b) } }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.Schedule, YT.Warning)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.contact_hours), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        OpenStatus(b, Modifier)
                    }
                    Spacer(Modifier.height(8.dp))
                    val hours = BusinessHours.parse(b.openingHours)
                    val today = Calendar.getInstance(BusinessHours.ALGIERS).get(Calendar.DAY_OF_WEEK)
                    listOf(Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY).forEach { d ->
                        val h = hours[d]
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(dayName(d, l), style = MaterialTheme.typography.bodyMedium, color = if (d == today) YT.Cyan else YT.White, modifier = Modifier.weight(1f))
                            Text(
                                h?.let { "⁦${BusinessHours.hhmm(it.first)} – ${BusinessHours.hhmm(it.second)}⁩" } ?: stringResource(R.string.contact_closed),
                                style = MaterialTheme.typography.bodyMedium, color = if (h == null) YT.Danger else YT.TextMuted,
                            )
                        }
                    }
                }
            }
            val socials = listOf("Facebook" to b.facebookUrl, "Instagram" to b.instagramUrl, "TikTok" to b.tiktokUrl, "Web" to b.websiteUrl).filter { it.second.isNotBlank() }
            if (socials.isNotEmpty()) {
                item { Text(stringResource(R.string.contact_social), style = MaterialTheme.typography.titleMedium) }
                items(socials) { (name, url) -> ContactRow(Icons.Rounded.Link, name, url, YT.Blue) { Launcher.web(context, url) } }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, title: String, value: String, tint: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(YT.Surface).pressable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = YT.TextMuted)
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit, viewModel: BusinessViewModel = hiltViewModel()) {
    val b by viewModel.business.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(stringResource(R.string.more_about), onBack)
        Column(Modifier.padding(20.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(24.dp)) { BrandLockup(Modifier.fillMaxWidth(), onDark = false) }
            Spacer(Modifier.height(16.dp))
            Text(b.tagline(l), style = MaterialTheme.typography.titleMedium, color = YT.Cyan)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.about_body), style = MaterialTheme.typography.bodyLarge, color = YT.TextMuted)
            Spacer(Modifier.height(16.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                KeyValueRow(stringResource(R.string.contact_address), b.address(l))
                KeyValueRow(stringResource(R.string.quick_call), "⁦${b.phoneDisplay()}⁩")
            }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(stringResource(R.string.more_privacy), onBack)
        GlassCard(Modifier.padding(20.dp).fillMaxWidth()) { Text(stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyLarge) }
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val content: ContentRepository) : ViewModel() {
    val list: StateFlow<List<NotificationEntity>?> = content.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun markRead() = viewModelScope.launch { delay(1200); content.markAllRead() }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit, onRequest: (Long) -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val list by viewModel.list.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    LaunchedEffect(Unit) { viewModel.markRead() }
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.notifications_title), onBack)
        val items = list ?: return@Column
        if (items.isEmpty()) { EmptyState(Icons.Rounded.NotificationsNone, stringResource(R.string.notifications_empty), ""); return@Column }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { n ->
                val status = RequestStatus.entries.firstOrNull { it.name == n.body }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (n.isRead) YT.Surface else YT.SurfaceHigh)
                        .pressable { n.requestId?.let(onRequest) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconTile(Icons.Rounded.Notifications)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(n.title, style = MaterialTheme.typography.titleSmall)
                        Text(status?.let { stringResource(it.label) } ?: n.body, style = MaterialTheme.typography.bodyMedium, color = YT.TextMuted)
                        Text(formatDate(n.createdAt, l, withTime = true), style = MaterialTheme.typography.labelSmall, color = YT.TextMuted)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Portfolio
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class PortfolioViewModel @Inject constructor(content: ContentRepository) : ViewModel() {
    val projects: StateFlow<List<ProjectEntity>?> = content.projects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun PortfolioScreen(onBack: () -> Unit, onProject: (Long) -> Unit, viewModel: PortfolioViewModel = hiltViewModel()) {
    val list by viewModel.projects.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.portfolio_title), onBack)
        val items = list ?: return@Column
        if (items.isEmpty()) { EmptyState(Icons.Rounded.PhotoLibrary, stringResource(R.string.portfolio_empty_title), stringResource(R.string.portfolio_empty_body)); return@Column }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(items, key = { it.id }) { p ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YT.Surface).pressable { onProject(p.id) }) {
                    MediaImage(p.photos.lines().firstOrNull { it.isNotBlank() }, "camera", Modifier.fillMaxWidth().aspectRatio(1.7f))
                    Column(Modifier.padding(14.dp)) {
                        Text(p.title, style = MaterialTheme.typography.titleMedium)
                        if (p.generalLocation.isNotBlank()) Text(p.generalLocation, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class ProjectViewModel @Inject constructor(saved: SavedStateHandle, content: ContentRepository) : ViewModel() {
    val project: StateFlow<ProjectEntity?> = content.project(checkNotNull(saved.get<String>("id")).toLong()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun ProjectScreen(onBack: () -> Unit, viewModel: ProjectViewModel = hiltViewModel()) {
    val p by viewModel.project.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(p?.title.orEmpty(), onBack)
        val pr = p ?: return@Column
        val photos = pr.photos.lines().filter { it.isNotBlank() }
        if (photos.isNotEmpty()) {
            val pager = rememberPagerState { photos.size }
            HorizontalPager(pager, Modifier.fillMaxWidth().aspectRatio(1.4f)) { i -> MediaImage(photos[i], "camera", Modifier.fillMaxSize()) }
        }
        Column(Modifier.padding(20.dp)) {
            Text(pr.title, style = MaterialTheme.typography.headlineSmall)
            pr.completedAt?.let { Text(formatDate(it, l), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted) }
            if (pr.description.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(pr.description, style = MaterialTheme.typography.bodyLarge, color = YT.TextMuted) }
            Spacer(Modifier.height(14.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                if (pr.generalLocation.isNotBlank()) KeyValueRow(stringResource(R.string.project_location), pr.generalLocation)
                if (pr.equipment.isNotBlank()) KeyValueRow(stringResource(R.string.project_equipment), pr.equipment)
            }
        }
    }
}

