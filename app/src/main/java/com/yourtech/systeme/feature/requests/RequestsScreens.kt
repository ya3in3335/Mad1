package com.yourtech.systeme.feature.requests

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.BusinessInfoEntity
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.RequestWithDetails
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.model.BusinessDefaults
import com.yourtech.systeme.data.model.RequestStatus
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.Wilayas
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.data.repository.CatalogRepository
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.data.repository.RequestRepository
import com.yourtech.systeme.designsystem.component.EmptyState
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.IconTile
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
import com.yourtech.systeme.ui.formatDate
import com.yourtech.systeme.ui.label
import com.yourtech.systeme.ui.money
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val RequestType.icon: ImageVector get() = when (this) {
    RequestType.INSTALLATION -> Icons.Rounded.Build
    RequestType.MAINTENANCE -> Icons.Rounded.SupportAgent
    RequestType.CONSULTATION -> Icons.Rounded.TipsAndUpdates
    RequestType.QUOTE -> Icons.Rounded.RequestQuote
}

@HiltViewModel
class RequestsViewModel @Inject constructor(repo: RequestRepository) : ViewModel() {
    val requests: StateFlow<List<RequestWithDetails>?> = repo.all.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun RequestsScreen(onRequest: (Long) -> Unit, onNew: (RequestType) -> Unit, viewModel: RequestsViewModel = hiltViewModel()) {
    val list by viewModel.requests.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.requests_title), style = MaterialTheme.typography.headlineSmall) }
        item { GradientButton(stringResource(R.string.request_quote), { onNew(RequestType.QUOTE) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.RequestQuote) }
        val l2 = list
        if (l2 != null && l2.isEmpty()) item { EmptyState(Icons.Rounded.RequestQuote, stringResource(R.string.requests_empty_title), stringResource(R.string.requests_empty_body)) }
        items(l2.orEmpty(), key = { it.request.id }) { r ->
            val req = r.request
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(YT.Surface).pressable { onRequest(req.id) }.padding(14.dp).animateItem(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(req.type.icon, req.status.color)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(req.type.label), style = MaterialTheme.typography.titleSmall)
                    Text(req.reference, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                    Text(formatDate(req.createdAt, l), style = MaterialTheme.typography.labelSmall, color = YT.TextMuted)
                }
                StatusPill(stringResource(req.status.label), req.status.color)
            }
        }
    }
}

@HiltViewModel
class RequestDetailViewModel @Inject constructor(saved: SavedStateHandle, private val repo: RequestRepository, catalog: CatalogRepository, content: ContentRepository) : ViewModel() {
    val id: Long = checkNotNull(saved.get<String>("id")).toLong()
    val request: StateFlow<RequestWithDetails?> = repo.request(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val business: StateFlow<BusinessInfoEntity> = content.business.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusinessDefaults.info)
    val services: StateFlow<List<ServiceCategoryEntity>> = catalog.services.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val equipment: StateFlow<List<ProductCategoryEntity>> = catalog.productCategories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun cancel() = viewModelScope.launch { repo.cancel(id) }
}

@Composable
private fun systemName(r: RequestWithDetails, services: List<ServiceCategoryEntity>, equipment: List<ProductCategoryEntity>): String? {
    val l = LocalLanguage.current
    val id = r.request.systemType ?: return null
    return services.firstOrNull { it.id == id }?.name(l) ?: equipment.firstOrNull { it.id == id }?.name(l)
}

@Composable
fun RequestDetailScreen(onBack: () -> Unit, viewModel: RequestDetailViewModel = hiltViewModel()) {
    val data by viewModel.request.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val equipment by viewModel.equipment.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.track_request), onBack)
        val r = data ?: return@Column
        val req = r.request
        val sys = systemName(r, services, equipment)
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GlassCard(Modifier.fillMaxWidth(), highlight = req.status.color.copy(alpha = 0.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(req.type.icon, req.status.color)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(req.type.label), style = MaterialTheme.typography.titleMedium)
                            Text(req.reference, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                        }
                        StatusPill(stringResource(req.status.label), req.status.color)
                    }
                }
            }
            item { Timeline(r) }
            if (req.quoteAmountDzd != null || req.scheduledAt != null || req.companyNote.isNotBlank()) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        req.quoteAmountDzd?.let { KeyValueRow(stringResource(R.string.request_quote_amount), money(it)) }
                        req.scheduledAt?.let { KeyValueRow(stringResource(R.string.request_scheduled_at), formatDate(it, l, withTime = true)) }
                        if (req.companyNote.isNotBlank()) KeyValueRow(stringResource(R.string.request_company_note), req.companyNote)
                    }
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.request_details), style = MaterialTheme.typography.titleMedium)
                    sys?.let { KeyValueRow(stringResource(R.string.field_system), it) }
                    req.propertyType?.let { KeyValueRow(stringResource(R.string.field_property), stringResource(it.label)) }
                    req.deviceCount?.let { KeyValueRow(stringResource(R.string.field_devices), it.toString()) }
                    if (req.problemDescription.isNotBlank()) KeyValueRow(stringResource(R.string.field_problem), req.problemDescription)
                    KeyValueRow(stringResource(R.string.field_name), req.customerName)
                    KeyValueRow(stringResource(R.string.field_phone), req.phone)
                    if (req.wilaya.isNotBlank()) KeyValueRow(stringResource(R.string.field_wilaya), Wilayas.fromStorage(req.wilaya)?.label(l) ?: req.wilaya)
                    req.preferredDate?.let { KeyValueRow(stringResource(R.string.field_date), formatDate(it, l)) }
                    if (req.notes.isNotBlank()) KeyValueRow(stringResource(R.string.field_notes), req.notes)
                }
            }
            if (r.photos.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(r.photos, key = { it.id }) { ph -> MediaImage(ph.path, "camera", Modifier.size(96.dp).clip(RoundedCornerShape(14.dp))) }
                    }
                }
            }
            item {
                val msg = requestMessage(context, req, sys, l)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton(stringResource(R.string.send_whatsapp), { Launcher.whatsapp(context, business, msg) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Chat, colors = listOf(Color(0xFF16A34A), Color(0xFF22C55E)))
                    if (r.photos.isNotEmpty()) OutlineButton(stringResource(R.string.field_photos), { sharePhotos(context, r.photos.map { it.path }, req.reference) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Photo)
                    if (req.status == RequestStatus.SUBMITTED) TextButton(onClick = viewModel::cancel, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Close, null, tint = YT.Danger); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.cancel_request), color = YT.Danger)
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Info, null, tint = YT.TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.request_local_note), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun Timeline(r: RequestWithDetails) {
    val l = LocalLanguage.current
    val status = r.request.status
    val steps = if (status == RequestStatus.CANCELLED) listOf(RequestStatus.SUBMITTED, RequestStatus.CANCELLED) else RequestStatus.timeline
    GlassCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.request_timeline), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        steps.forEachIndexed { i, step ->
            val event = r.events.lastOrNull { it.status == step }
            val reached = event != null || step.ordinal <= status.ordinal && status != RequestStatus.CANCELLED
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (reached) step.color else YT.SurfaceHigh),
                        contentAlignment = Alignment.Center,
                    ) { if (reached) Icon(if (step == RequestStatus.CANCELLED) Icons.Rounded.Close else Icons.Rounded.Check, null, tint = YT.Navy, modifier = Modifier.size(15.dp)) }
                    if (i < steps.lastIndex) Box(Modifier.width(2.dp).height(30.dp).background(if (reached) step.color.copy(alpha = 0.6f) else YT.SurfaceHigh))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(top = 2.dp)) {
                    Text(stringResource(step.label), style = MaterialTheme.typography.titleSmall, color = if (reached) YT.White else YT.TextMuted)
                    event?.let {
                        Text(formatDate(it.timestamp, l, withTime = true), style = MaterialTheme.typography.labelSmall, color = YT.TextMuted)
                        if (it.note.isNotBlank()) Text(it.note, style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestSentScreen(onTrack: (Long) -> Unit, onHome: () -> Unit, viewModel: RequestDetailViewModel = hiltViewModel()) {
    val data by viewModel.request.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val equipment by viewModel.equipment.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    val ring = remember { Animatable(0f) }
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { ring.animateTo(1f, tween(1000)) }
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) { drawCircle(YT.Cyan.copy(alpha = 1f - ring.value), radius = size.minDimension / 2f * (0.5f + 0.5f * ring.value), style = Stroke(3f)) }
            Box(Modifier.size(104.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }.clip(CircleShape).background(YT.Blue), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.sent_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        val r = data
        Text(stringResource(R.string.sent_body, r?.request?.reference ?: ""), style = MaterialTheme.typography.bodyLarge, color = YT.TextMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        if (r != null) {
            val sys = systemName(r, services, equipment)
            GradientButton(
                stringResource(R.string.send_whatsapp), { Launcher.whatsapp(context, business, requestMessage(context, r.request, sys, l)) },
                Modifier.fillMaxWidth(), icon = Icons.Rounded.Chat, colors = listOf(Color(0xFF16A34A), Color(0xFF22C55E)),
            )
            Spacer(Modifier.height(10.dp))
            OutlineButton(stringResource(R.string.track_request), { onTrack(r.request.id) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Event)
        }
        TextButton(onClick = onHome, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(R.string.go_home), color = YT.TextMuted) }
    }
}
