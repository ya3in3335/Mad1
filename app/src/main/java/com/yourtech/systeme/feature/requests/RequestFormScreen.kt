package com.yourtech.systeme.feature.requests

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.yourtech.systeme.R
import com.yourtech.systeme.data.local.entity.ProductCategoryEntity
import com.yourtech.systeme.data.local.entity.ServiceCategoryEntity
import com.yourtech.systeme.data.media.ImageImporter
import com.yourtech.systeme.data.model.PropertyType
import com.yourtech.systeme.data.model.RequestForm
import com.yourtech.systeme.data.model.RequestType
import com.yourtech.systeme.data.model.Wilaya
import com.yourtech.systeme.data.model.Wilayas
import com.yourtech.systeme.data.model.name
import com.yourtech.systeme.data.repository.CatalogRepository
import com.yourtech.systeme.data.repository.ContentRepository
import com.yourtech.systeme.data.repository.RequestRepository
import com.yourtech.systeme.designsystem.component.Chip
import com.yourtech.systeme.designsystem.component.GlassCard
import com.yourtech.systeme.designsystem.component.GradientButton
import com.yourtech.systeme.designsystem.component.MediaImage
import com.yourtech.systeme.designsystem.component.pressable
import com.yourtech.systeme.designsystem.theme.YT
import com.yourtech.systeme.ui.LocalLanguage
import com.yourtech.systeme.ui.LocalSnackbar
import com.yourtech.systeme.ui.TopBar
import com.yourtech.systeme.ui.formatDate
import com.yourtech.systeme.ui.label
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormUi(
    val form: RequestForm,
    val errors: Set<RequestForm.Field> = emptySet(),
    val submitting: Boolean = false,
    val importing: Boolean = false,
)

@HiltViewModel
class RequestFormViewModel @Inject constructor(
    saved: SavedStateHandle,
    catalog: CatalogRepository,
    private val content: ContentRepository,
    private val requests: RequestRepository,
    private val importer: ImageImporter,
) : ViewModel() {
    private val type = RequestType.entries.firstOrNull { it.name == saved.get<String>("type") } ?: RequestType.INSTALLATION
    private val _ui = MutableStateFlow(
        FormUi(RequestForm(type = type, systemType = saved.get<String>("system")?.ifBlank { null }, productId = saved.get<String>("product")?.ifBlank { null }))
    )
    val ui: StateFlow<FormUi> = _ui
    val services: StateFlow<List<ServiceCategoryEntity>> = catalog.services.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val equipment: StateFlow<List<ProductCategoryEntity>> = catalog.productCategories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _sent = MutableSharedFlow<Long>()
    val sent: SharedFlow<Long> = _sent
    private val _rejected = MutableSharedFlow<Unit>()
    val rejected: SharedFlow<Unit> = _rejected

    init {
        viewModelScope.launch {
            val p = content.profile.first() ?: return@launch
            update { it.copy(customerName = p.fullName, phone = p.phone, wilaya = Wilayas.fromStorage(p.wilaya), commune = p.commune, address = p.address) }
        }
    }

    fun update(f: (RequestForm) -> RequestForm) = _ui.update { s ->
        val form = f(s.form)
        s.copy(form = form, errors = if (s.errors.isEmpty()) s.errors else form.validate())
    }

    fun addPhotos(uris: List<android.net.Uri>) = viewModelScope.launch {
        _ui.update { it.copy(importing = true) }
        for (uri in uris) {
            if (_ui.value.form.photoPaths.size >= RequestForm.MAX_PHOTOS) break
            when (val r = importer.import(uri, "requests")) {
                is ImageImporter.Result.Ok -> update { it.copy(photoPaths = it.photoPaths + r.path) }
                else -> _rejected.emit(Unit)
            }
        }
        _ui.update { it.copy(importing = false) }
    }

    fun removePhoto(path: String) { importer.delete(path); update { it.copy(photoPaths = it.photoPaths - path) } }

    fun submit() {
        val s = _ui.value
        val errors = s.form.validate()
        if (errors.isNotEmpty()) { _ui.update { it.copy(errors = errors) }; return }
        viewModelScope.launch {
            _ui.update { it.copy(submitting = true) }
            val r = runCatching { requests.submit(s.form) }
            _ui.update { it.copy(submitting = false) }
            r.onSuccess { _sent.emit(it.id) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RequestFormScreen(onBack: () -> Unit, onSent: (Long) -> Unit, viewModel: RequestFormViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val equipment by viewModel.equipment.collectAsStateWithLifecycle()
    val l = LocalLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val focus = LocalFocusManager.current
    var wilayaSheet by remember { mutableStateOf(false) }
    var datePicker by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(RequestForm.MAX_PHOTOS)) { uris -> if (uris.isNotEmpty()) viewModel.addPhotos(uris) }
    LaunchedEffect(Unit) { viewModel.sent.collect(onSent) }
    LaunchedEffect(Unit) { viewModel.rejected.collect { snackbar.showSnackbar(context.getString(R.string.photo_rejected)) } }

    val f = ui.form
    val e = ui.errors
    val type = f.type
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar(stringResource(type.label), onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Section(stringResource(R.string.form_need))
                if (type == RequestType.MAINTENANCE) {
                    Label(stringResource(R.string.field_equipment), RequestForm.Field.SYSTEM in e)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        equipment.forEach { c -> Chip(c.name(l), f.systemType == c.id, { viewModel.update { it.copy(systemType = c.id) } }) }
                    }
                    Field(f.problemDescription, { v -> viewModel.update { it.copy(problemDescription = v) } }, R.string.field_problem, RequestForm.Field.PROBLEM in e, R.string.err_problem, minLines = 3)
                } else {
                    Label(stringResource(R.string.field_system), RequestForm.Field.SYSTEM in e)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        services.forEach { c -> Chip(c.name(l), f.systemType == c.id, { viewModel.update { it.copy(systemType = c.id) } }) }
                    }
                }
                if (type == RequestType.INSTALLATION || type == RequestType.QUOTE) {
                    Label(stringResource(R.string.field_property), RequestForm.Field.PROPERTY in e)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PropertyType.entries.forEach { pt -> Chip(stringResource(pt.label), f.propertyType == pt, { viewModel.update { it.copy(propertyType = pt) } }) }
                    }
                    Field(f.deviceCount, { v -> viewModel.update { it.copy(deviceCount = v.filter(Char::isDigit).take(3)) } }, R.string.field_devices, RequestForm.Field.DEVICES in e, R.string.err_devices, KeyboardType.Number)
                }

                Section(stringResource(R.string.form_contact))
                Field(f.customerName, { v -> viewModel.update { it.copy(customerName = v) } }, R.string.field_name, RequestForm.Field.NAME in e, R.string.err_name)
                Field(f.phone, { v -> viewModel.update { it.copy(phone = v) } }, R.string.field_phone, RequestForm.Field.PHONE in e, R.string.err_phone, KeyboardType.Phone)

                Section(stringResource(R.string.form_site))
                PickerField(f.wilaya?.label(l).orEmpty(), stringResource(R.string.field_wilaya), RequestForm.Field.WILAYA in e, stringResource(R.string.err_wilaya), Icons.Rounded.ExpandMore) {
                    focus.clearFocus(); wilayaSheet = true
                }
                Field(f.commune, { v -> viewModel.update { it.copy(commune = v) } }, R.string.field_commune, RequestForm.Field.COMMUNE in e, R.string.err_commune)
                Field(f.address, { v -> viewModel.update { it.copy(address = v) } }, R.string.field_address, RequestForm.Field.ADDRESS in e, R.string.err_address)
                PickerField(f.preferredDate?.let { formatDate(it, l) }.orEmpty(), stringResource(R.string.field_date), RequestForm.Field.DATE in e, stringResource(R.string.err_date), Icons.Rounded.CalendarMonth) {
                    focus.clearFocus(); datePicker = true
                }
                Field(f.notes, { v -> viewModel.update { it.copy(notes = v) } }, R.string.field_notes, false, R.string.err_generic, minLines = 2)

                Section(stringResource(R.string.field_photos))
                GlassCard(Modifier.fillMaxWidth()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        f.photoPaths.forEach { path ->
                            Box(Modifier.size(84.dp)) {
                                MediaImage(path, "camera", Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)))
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(YT.Navy.copy(alpha = 0.8f)).pressable { viewModel.removePhoto(path) },
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.Rounded.Close, null, modifier = Modifier.size(14.dp)) }
                            }
                        }
                        if (f.photoPaths.size < RequestForm.MAX_PHOTOS) {
                            Box(
                                Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, YT.Cyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .pressable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (ui.importing) CircularProgressIndicator(Modifier.size(24.dp), color = YT.Cyan, strokeWidth = 2.dp)
                                else Icon(Icons.Rounded.AddAPhoto, stringResource(R.string.add_photos), tint = YT.Cyan)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.photos_note), style = MaterialTheme.typography.bodySmall, color = YT.TextMuted)
                }
                Spacer(Modifier.height(24.dp))
            }
            Box(Modifier.navigationBarsPadding().padding(20.dp)) {
                GradientButton(
                    if (ui.submitting) stringResource(R.string.submitting) else stringResource(R.string.submit_request),
                    viewModel::submit, Modifier.fillMaxWidth(), enabled = !ui.submitting && !ui.importing,
                )
            }
        }
        WilayaSheet(wilayaSheet, onPick = { w -> viewModel.update { it.copy(wilaya = w) }; wilayaSheet = false }, onDismiss = { wilayaSheet = false })
    }

    if (datePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = f.preferredDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { datePicker = false },
            confirmButton = { TextButton(onClick = { viewModel.update { it.copy(preferredDate = state.selectedDateMillis) }; datePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { datePicker = false }) { Text(stringResource(R.string.action_back)) } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun Section(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = YT.Cyan, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

@Composable
private fun Label(text: String, error: Boolean) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = if (error) YT.Danger else YT.TextMuted, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = YT.Outline, focusedBorderColor = YT.Cyan, unfocusedContainerColor = YT.Surface, focusedContainerColor = YT.Surface,
    focusedLabelColor = YT.Cyan,
)

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: Int, isError: Boolean, error: Int, keyboard: KeyboardType = KeyboardType.Text, minLines: Int = 1) {
    OutlinedTextField(
        value, onChange, label = { Text(stringResource(label)) }, isError = isError,
        supportingText = if (isError) { { Text(stringResource(error)) } } else null,
        singleLine = minLines == 1, minLines = minLines, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
    )
}

@Composable
private fun PickerField(value: String, label: String, isError: Boolean, error: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        OutlinedTextField(
            value, {}, readOnly = true, label = { Text(label) }, isError = isError,
            supportingText = if (isError) { { Text(error) } } else null,
            trailingIcon = { Icon(icon, null) }, shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
        )
        // The read-only field is covered by a button that carries the label for accessibility services.
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(16.dp)).pressable(onClick = onClick).semantics { contentDescription = label; role = Role.Button })
    }
}

/** In-screen bottom sheet (no extra window) with search. */
@Composable
fun WilayaSheet(visible: Boolean, onPick: (Wilaya) -> Unit, onDismiss: () -> Unit) {
    val l = LocalLanguage.current
    var query by remember { mutableStateOf("") }
    val list = remember(query) { Wilayas.all.filter { query.isBlank() || it.nameAr.contains(query) || it.nameFr.contains(query, true) || it.code.toString() == query.trim() } }
    BackHandler(visible, onDismiss)
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable(remember { MutableInteractionSource() }, null, onClick = onDismiss))
        }
        AnimatedVisibility(visible, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(), modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(YT.Surface)
                    .navigationBarsPadding().imePadding().padding(20.dp),
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(40.dp, 4.dp).clip(RoundedCornerShape(50)).background(YT.Outline))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.field_wilaya), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(query, { query = it }, singleLine = true, placeholder = { Text(stringResource(R.string.wilaya_search)) }, shape = RoundedCornerShape(50), colors = fieldColors(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    items(list, key = { it.code }) { w ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).pressable { onPick(w) }.padding(12.dp)) {
                            Text(w.label(l), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
