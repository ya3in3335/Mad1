package com.madak.spices.feature.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.BuildConfig
import com.madak.spices.R
import com.madak.spices.data.local.entity.NotificationEntity
import com.madak.spices.data.local.entity.UserEntity
import com.madak.spices.data.model.AppLanguage
import com.madak.spices.data.model.CheckoutForm
import com.madak.spices.data.model.StoreInfo
import com.madak.spices.data.repository.NotificationRepository
import com.madak.spices.data.repository.UserRepository
import com.madak.spices.data.settings.SettingsRepository
import com.madak.spices.designsystem.component.EmptyState
import com.madak.spices.designsystem.component.LogoPart
import com.madak.spices.designsystem.component.MadakChip
import com.madak.spices.designsystem.component.MadakLogo
import com.madak.spices.designsystem.component.MadakWordmark
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.Launcher
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.MadakTopBar
import com.madak.spices.ui.formatDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val settings: SettingsRepository,
    notifications: NotificationRepository,
) : ViewModel() {
    val user: StateFlow<UserEntity?> = users.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val splashSound: StateFlow<Boolean> = settings.splashSoundEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val unread: StateFlow<Int> = notifications.unreadCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setSplashSound(enabled: Boolean) = viewModelScope.launch { settings.setSplashSound(enabled) }
    suspend fun save(name: String, phone: String, email: String) = users.saveProfile(name, phone, email)
}

@Composable
fun ProfileScreen(
    onOrders: () -> Unit,
    onFavorites: () -> Unit,
    onNotifications: () -> Unit,
    onAbout: () -> Unit,
    onStore: () -> Unit,
    onContact: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val sound by viewModel.splashSound.collectAsStateWithLifecycle()
    val unread by viewModel.unread.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    var editing by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(Brush.verticalGradient(listOf(MadakColors.Ink, MadakColors.Charcoal)))
                .statusBarsPadding().padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MadakColors.Magenta, MadakColors.Gold))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(user?.fullName?.firstOrNull()?.toString() ?: "م", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user?.fullName ?: stringResource(R.string.profile_guest), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    user?.phone?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MadakColors.Beige.copy(alpha = 0.8f)) }
                }
                IconButton(onClick = { editing = true }) { Icon(Icons.Rounded.Edit, stringResource(R.string.profile_edit), tint = MadakColors.Gold) }
            }
        }

        MenuSection(stringResource(R.string.profile_section_shop)) {
            MenuItem(Icons.AutoMirrored.Rounded.ReceiptLong, stringResource(R.string.profile_orders), onClick = onOrders)
            MenuItem(Icons.Rounded.Favorite, stringResource(R.string.profile_favorites), onClick = onFavorites)
            MenuItem(Icons.Rounded.Notifications, stringResource(R.string.profile_notifications), badge = unread.takeIf { it > 0 }?.toString(), onClick = onNotifications)
        }
        MenuSection(stringResource(R.string.profile_section_app)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                MenuIcon(Icons.Rounded.Language)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.profile_language), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                MadakChip(stringResource(R.string.language_arabic), language == AppLanguage.ARABIC, { onLanguageChange(AppLanguage.ARABIC) })
                Spacer(Modifier.width(6.dp))
                MadakChip(stringResource(R.string.language_french), language == AppLanguage.FRENCH, { onLanguageChange(AppLanguage.FRENCH) })
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                MenuIcon(Icons.Rounded.VolumeUp)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.profile_splash_sound), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(sound, viewModel::setSplashSound, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
            }
        }
        MenuSection(stringResource(R.string.profile_section_madak)) {
            MenuItem(Icons.Rounded.Info, stringResource(R.string.profile_about), onClick = onAbout)
            MenuItem(Icons.Rounded.Storefront, stringResource(R.string.profile_store), onClick = onStore)
            MenuItem(Icons.Rounded.SupportAgent, stringResource(R.string.profile_contact), onClick = onContact)
        }
        Text(
            stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp),
        )
    }

    if (editing) EditProfileDialog(user, onDismiss = { editing = false }, onSave = viewModel::save)
}

@Composable
private fun EditProfileDialog(user: UserEntity?, onDismiss: () -> Unit, onSave: suspend (String, String, String) -> Unit) {
    var name by remember { mutableStateOf(user?.fullName.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    val phoneValid = phone.isBlank() || CheckoutForm.isValidAlgerianPhone(phone)
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.field_full_name)) }, singleLine = true)
                OutlinedTextField(
                    phone, { phone = it }, label = { Text(stringResource(R.string.field_phone)) }, singleLine = true,
                    isError = !phoneValid, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = if (!phoneValid) { { Text(stringResource(R.string.error_phone)) } } else null,
                )
                OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.field_email)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            }
        },
        confirmButton = {
            Button(enabled = name.trim().length >= 3 && phoneValid, onClick = {
                scope.launch {
                    onSave(name, phone, email)
                    onDismiss()
                    snackbar.showSnackbar(context.getString(R.string.profile_saved))
                }
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_back)) } },
    )
}

@Composable
private fun MenuSection(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 8.dp))
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Column(Modifier.padding(vertical = 6.dp)) { content() }
    }
}

@Composable
private fun MenuIcon(icon: ImageVector) {
    Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun MenuItem(icon: ImageVector, title: String, badge: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().bounceClick(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        MenuIcon(icon)
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (badge != null) {
            Text(badge, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary).padding(horizontal = 8.dp, vertical = 2.dp))
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repo: NotificationRepository) : ViewModel() {
    val notifications: StateFlow<List<NotificationEntity>?> = repo.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun markAllRead() = viewModelScope.launch { repo.markAllRead() }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit, onOrder: (Long) -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val list by viewModel.notifications.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    LaunchedEffect(list?.size) { if (list?.any { !it.isRead } == true) { kotlinx.coroutines.delay(1500); viewModel.markAllRead() } }
    Column(Modifier.fillMaxSize()) {
        MadakTopBar(stringResource(R.string.notifications_title), onBack)
        val items = list ?: return@Column
        if (items.isEmpty()) {
            EmptyState(Icons.Rounded.NotificationsNone, stringResource(R.string.notifications_empty_title), stringResource(R.string.notifications_empty_body))
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { n ->
                val icon = when (n.type) {
                    NotificationEntity.TYPE_ORDER -> Icons.AutoMirrored.Rounded.ReceiptLong
                    NotificationEntity.TYPE_OFFER -> Icons.Rounded.LocalOffer
                    else -> Icons.Rounded.Notifications
                }
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (n.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.animateItem().bounceClick { n.orderId?.let(onOrder) },
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp)) {
                        MenuIcon(icon)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(language.pick(n.titleAr, n.titleFr), style = MaterialTheme.typography.titleSmall)
                            Text(language.pick(n.bodyAr, n.bodyFr), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatDateTime(n.createdAt, language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// About / Store / Contact
// ---------------------------------------------------------------------------------------------

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MadakTopBar(stringResource(R.string.about_title), onBack)
        Box(
            Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(MadakColors.Ink).padding(vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) { MadakLogo(Modifier.width(170.dp)) }
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text(stringResource(R.string.about_story_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.about_story), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("🌿", R.string.about_value_1, MadakColors.Herb),
                    Triple("⚙️", R.string.about_value_2, MadakColors.Turmeric),
                    Triple("🏺", R.string.about_value_3, MadakColors.Cinnamon),
                ).forEach { (emoji, label, color) ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.15f)).padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StoreLocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MadakTopBar(stringResource(R.string.store_title), onBack)
        // Stylised offline map (no API key or network required); the button opens the real map app.
        Box(Modifier.padding(20.dp).fillMaxWidth().height(230.dp).clip(RoundedCornerShape(28.dp)).background(MadakColors.Beige)) {
            Canvas(Modifier.fillMaxSize()) {
                val grid = MadakColors.Gold.copy(alpha = 0.35f)
                for (i in 1..8) {
                    drawLine(grid, Offset(size.width * i / 9f, 0f), Offset(size.width * i / 9f - 60f, size.height), strokeWidth = 2f)
                    drawLine(grid, Offset(0f, size.height * i / 9f), Offset(size.width, size.height * i / 9f + 30f), strokeWidth = 2f)
                }
                drawLine(Color.White, Offset(0f, size.height * 0.6f), Offset(size.width, size.height * 0.35f), strokeWidth = 18f)
                drawLine(Color.White, Offset(size.width * 0.3f, 0f), Offset(size.width * 0.55f, size.height), strokeWidth = 14f)
                drawCircle(MadakColors.Magenta.copy(alpha = 0.18f), radius = 70f, center = center)
                drawCircle(MadakColors.Magenta.copy(alpha = 0.5f), radius = 70f, center = center, style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
            }
            Box(Modifier.align(Alignment.Center).size(54.dp).clip(CircleShape).background(MadakColors.Ink), contentAlignment = Alignment.Center) {
                MadakLogo(Modifier.size(38.dp), part = LogoPart.CHEF)
            }
        }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(Icons.Rounded.Place, stringResource(R.string.store_address), language.pick(StoreInfo.ADDRESS_AR, StoreInfo.ADDRESS_FR))
            if (StoreInfo.hasOpeningHours) {
                InfoCard(Icons.Rounded.AccessTime, stringResource(R.string.store_hours), language.pick(StoreInfo.OPENING_HOURS_AR, StoreInfo.OPENING_HOURS_FR))
            }
            Button(onClick = { Launcher.maps(context) }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Icon(Icons.Rounded.Map, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_open_maps))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoCard(icon: ImageVector, title: String, body: String, onClick: (() -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.bounceClick(onClick = onClick) else Modifier)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            MenuIcon(icon)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(body, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val whatsappMessage = stringResource(R.string.contact_whatsapp_message)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MadakTopBar(stringResource(R.string.contact_title), onBack)
        Column(
            Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(MadakColors.Ink).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MadakWordmark(Modifier.height(60.dp))
            Spacer(Modifier.height(10.dp))
            Text(language.pick(StoreInfo.SLOGAN_AR, StoreInfo.SLOGAN_FR), color = MadakColors.Gold, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.contact_subtitle), color = MadakColors.Beige, style = MaterialTheme.typography.bodyMedium)
        }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Only channels with real, published details are shown.
            if (StoreInfo.hasWhatsapp) {
                ContactButton(Icons.Rounded.Chat, stringResource(R.string.action_whatsapp), "\u2066${StoreInfo.phonePretty}\u2069", Color(0xFF25D366)) { Launcher.whatsapp(context, whatsappMessage) }
            }
            if (StoreInfo.hasPhone) {
                ContactButton(Icons.Rounded.Call, stringResource(R.string.action_call), "\u2066${StoreInfo.phonePretty}\u2069", MadakColors.Info) { Launcher.call(context) }
            }
            if (StoreInfo.hasTiktok) {
                ContactButton(Icons.Rounded.MusicNote, stringResource(R.string.action_tiktok), "\u2066@${StoreInfo.TIKTOK_HANDLE}\u2069", MadakColors.Ink) { Launcher.tiktok(context) }
            }
            if (StoreInfo.hasInstagram) {
                ContactButton(Icons.Rounded.PhotoCamera, stringResource(R.string.action_instagram), "\u2066@${StoreInfo.INSTAGRAM_HANDLE}\u2069", MadakColors.Magenta) { Launcher.instagram(context) }
            }
            if (StoreInfo.hasEmail) {
                ContactButton(Icons.Rounded.Email, stringResource(R.string.contact_email), StoreInfo.EMAIL, MadakColors.Cinnamon) { Launcher.email(context) }
            }
            ContactButton(Icons.Rounded.Place, stringResource(R.string.profile_store), language.pick(StoreInfo.ADDRESS_AR, StoreInfo.ADDRESS_FR), MadakColors.Gold) { Launcher.maps(context) }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ContactButton(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().bounceClick(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
