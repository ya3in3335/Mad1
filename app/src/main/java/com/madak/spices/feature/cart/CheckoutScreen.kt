package com.madak.spices.feature.cart

import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.model.CartSummary
import com.madak.spices.data.model.CheckoutForm
import com.madak.spices.data.model.Wilaya
import com.madak.spices.data.model.Wilayas
import com.madak.spices.data.repository.CartRepository
import com.madak.spices.data.repository.OrderRepository
import com.madak.spices.data.repository.UserRepository
import com.madak.spices.designsystem.component.SummaryRow
import com.madak.spices.designsystem.component.bounceClick
import com.madak.spices.designsystem.theme.MadakColors
import com.madak.spices.ui.LocalAppLanguage
import com.madak.spices.ui.LocalSnackbar
import com.madak.spices.ui.MadakTopBar
import com.madak.spices.ui.OrderNotifier
import com.madak.spices.ui.price
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val form: CheckoutForm = CheckoutForm("", "", null, "", "", ""),
    val errors: Set<CheckoutForm.Field> = emptySet(),
    val submitting: Boolean = false,
)

sealed interface CheckoutEvent {
    data class Placed(val orderId: Long, val orderNumber: String) : CheckoutEvent
    data object Failed : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cart: CartRepository,
    private val orders: OrderRepository,
    private val users: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events: SharedFlow<CheckoutEvent> = _events

    val summary: StateFlow<CartSummary> = combine(cart.lines, _state) { lines, s -> CartSummary(lines, s.form.wilaya?.code) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CartSummary())

    init {
        viewModelScope.launch {
            val user = users.currentUser.first()
            val address = users.defaultAddress.first()
            _state.update {
                it.copy(
                    form = it.form.copy(
                        fullName = address?.fullName ?: user?.fullName.orEmpty(),
                        phone = address?.phone ?: user?.phone.orEmpty(),
                        wilaya = Wilayas.fromStorage(address?.wilaya),
                        commune = address?.commune.orEmpty(),
                        address = address?.street.orEmpty(),
                    )
                )
            }
        }
    }

    fun update(transform: (CheckoutForm) -> CheckoutForm) = _state.update { s ->
        val form = transform(s.form)
        // Re-validate only fields that were already flagged, so errors clear as the user types.
        s.copy(form = form, errors = if (s.errors.isEmpty()) s.errors else form.validate())
    }

    fun submit() {
        val current = _state.value
        val errors = current.form.validate()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            val result = runCatching { orders.placeOrder(current.form, summary.value) }
            _state.update { it.copy(submitting = false) }
            result.onSuccess { _events.emit(CheckoutEvent.Placed(it.id, it.orderNumber)) }
                .onFailure { _events.emit(CheckoutEvent.Failed) }
        }
    }
}

@Composable
fun CheckoutScreen(onBack: () -> Unit, onPlaced: (Long) -> Unit, viewModel: CheckoutViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val haptic = LocalHapticFeedback.current
    var showWilayas by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { e ->
            when (e) {
                is CheckoutEvent.Placed -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    OrderNotifier.notify(context, e.orderId.toInt(), context.getString(R.string.confirm_title), context.getString(R.string.confirm_body))
                    onPlaced(e.orderId)
                }
                CheckoutEvent.Failed -> snackbar.showSnackbar(context.getString(R.string.error_generic))
            }
        }
    }

    val f = state.form
    val err = state.errors
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().imePadding()) {
        MadakTopBar(stringResource(R.string.checkout_title), onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            SectionTitle(stringResource(R.string.checkout_contact))
            Field(f.fullName, { v -> viewModel.update { it.copy(fullName = v) } }, R.string.field_full_name, CheckoutForm.Field.NAME in err, R.string.error_name)
            Field(f.phone, { v -> viewModel.update { it.copy(phone = v) } }, R.string.field_phone, CheckoutForm.Field.PHONE in err, R.string.error_phone, KeyboardType.Phone)

            SectionTitle(stringResource(R.string.checkout_address_section))
            Box(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                OutlinedTextField(
                    value = f.wilaya?.label(language).orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_wilaya)) },
                    trailingIcon = { Icon(Icons.Rounded.ExpandMore, null) },
                    isError = CheckoutForm.Field.WILAYA in err,
                    supportingText = if (CheckoutForm.Field.WILAYA in err) { { Text(stringResource(R.string.error_wilaya)) } } else null,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(Modifier.matchParentSize().clip(RoundedCornerShape(18.dp)).bounceClick { focusManager.clearFocus(); showWilayas = true })
            }
            Field(f.commune, { v -> viewModel.update { it.copy(commune = v) } }, R.string.field_commune, CheckoutForm.Field.COMMUNE in err, R.string.error_commune)
            Field(f.address, { v -> viewModel.update { it.copy(address = v) } }, R.string.field_address, CheckoutForm.Field.ADDRESS in err, R.string.error_address)
            OutlinedTextField(
                value = f.notes, onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                label = { Text(stringResource(R.string.field_notes)) },
                placeholder = { Text(stringResource(R.string.field_notes_hint)) },
                shape = RoundedCornerShape(18.dp), minLines = 2,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )

            SectionTitle(stringResource(R.string.checkout_payment))
            OptionCard(Icons.Rounded.Payments, stringResource(R.string.checkout_cod), stringResource(R.string.checkout_cod_desc))
            SectionTitle(stringResource(R.string.checkout_delivery))
            OptionCard(Icons.Rounded.Home, stringResource(R.string.checkout_home_delivery), stringResource(R.string.checkout_home_delivery_desc))

            SectionTitle(stringResource(R.string.checkout_summary))
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp)) {
                    summary.lines.forEach { l ->
                        SummaryRow("${l.quantity} × ${l.product.name(language)} (${stringResource(R.string.weight_grams, l.variant.weightGrams)})", price(l.lineTotal))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryRow(stringResource(R.string.cart_subtotal), price(summary.subtotal))
                    SummaryRow(
                        stringResource(R.string.cart_delivery_fee),
                        if (summary.deliveryFee == 0) stringResource(R.string.cart_free) else price(summary.deliveryFee),
                    )
                    SummaryRow(stringResource(R.string.cart_total), price(summary.total), emphasize = true)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        Surface(shadowElevation = 16.dp, color = MaterialTheme.colorScheme.surface) {
            Button(
                onClick = viewModel::submit,
                enabled = !state.submitting && !summary.isEmpty,
                shape = RoundedCornerShape(50),
                modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(20.dp).height(56.dp),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.checkout_placing))
                } else {
                    Text(stringResource(R.string.action_place_order) + " • " + price(summary.total))
                }
            }
        }
    }

    WilayaPicker(
        visible = showWilayas,
        onPick = { w -> viewModel.update { it.copy(wilaya = w) }; showWilayas = false },
        onDismiss = { showWilayas = false },
    )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: Int,
    isError: Boolean,
    error: Int,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        isError = isError,
        supportingText = if (isError) { { Text(stringResource(error)) } } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

@Composable
private fun OptionCard(icon: ImageVector, title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.CheckCircle, null, tint = MadakColors.Success)
        }
    }
}

/** In-screen bottom sheet (no separate window): slides up over a scrim, Back or scrim tap closes it. */
@Composable
private fun WilayaPicker(visible: Boolean, onPick: (Wilaya) -> Unit, onDismiss: () -> Unit) {
    val language = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        Wilayas.all.filter { query.isBlank() || it.nameAr.contains(query) || it.nameFr.contains(query, ignoreCase = true) || it.code.toString() == query.trim() }
    }
    BackHandler(enabled = visible, onBack = onDismiss)
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            )
        }
        AnimatedVisibility(
            visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Surface(shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 24.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(20.dp)) {
                    Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 4.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.field_wilaya), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(query, { query = it }, singleLine = true, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(filtered, key = { it.code }) { w ->
                            Text(
                                w.label(language),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).bounceClick { onPick(w) }.padding(12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
