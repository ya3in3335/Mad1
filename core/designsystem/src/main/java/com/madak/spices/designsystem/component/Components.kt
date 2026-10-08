package com.madak.spices.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.madak.spices.designsystem.theme.MadakColors

/** Press-to-shrink feedback used on cards and tiles. */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "bounce")
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** Skeleton loading shimmer. */
fun Modifier.shimmer(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart), label = "shimmerX",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    this.clip(shape).background(
        Brush.linearGradient(
            colors = listOf(base.copy(alpha = 0.6f), base.copy(alpha = 0.25f), base.copy(alpha = 0.6f)),
            start = androidx.compose.ui.geometry.Offset(x * 600f, 0f),
            end = androidx.compose.ui.geometry.Offset(x * 600f + 400f, 400f),
        )
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)) {
    Box(modifier.shimmer(shape))
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 4.dp, height = 20.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val btn = if (compact) 30.dp else 40.dp
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(Icons.Rounded.Remove, btn, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onDecrease()
        }
        Text(
            quantity.toString(),
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(if (compact) 30.dp else 40.dp).animateContentSize(),
            textAlign = TextAlign.Center,
        )
        StepButton(Icons.Rounded.Add, btn, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onIncrease()
        }
    }
}

@Composable
private fun StepButton(icon: ImageVector, size: Dp, bg: Color, fg: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(CircleShape).background(bg).bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(size * 0.5f)) }
}

/** Selectable pill used for weight variants and filters. */
@Composable
fun MadakChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: String? = null,
) {
    val bg by androidx.compose.animation.animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface, label = "chipBg",
    )
    val fg by androidx.compose.animation.animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface, label = "chipFg",
    )
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(50))
            .bounceClick(enabled = enabled, onClick = onClick)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { Text(leading); Spacer(Modifier.width(6.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
fun RatingBadge(rating: Float, modifier: Modifier = Modifier, reviews: Int? = null) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Star, null, tint = MadakColors.Gold, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text(
            "%.1f".format(java.util.Locale.US, rating) + (reviews?.let { " ($it)" } ?: ""),
            style = MaterialTheme.typography.labelSmall, color = Color.White,
        )
    }
}

/** Cart badge that bumps every time the count changes. */
@Composable
fun AnimatedCountBadge(count: Int, content: @Composable () -> Unit) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(count) {
        if (count > 0) {
            scale.animateTo(1.45f, tween(120))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
        }
    }
    BadgedBox(badge = {
        if (count > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
            ) { Text(if (count > 99) "99+" else count.toString(), fontWeight = FontWeight.Bold) }
        }
    }) { content() }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val pulse = rememberInfiniteTransition(label = "empty")
        val s by pulse.animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "emptyScale")
        Box(
            Modifier.size(112.dp).graphicsLayer { scaleX = s; scaleY = s }.clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction, contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)) { Text(actionLabel) }
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50), modifier = modifier) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier, emphasize: Boolean = false, valueColor: Color = Color.Unspecified) {
    Row(modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
            color = if (valueColor != Color.Unspecified) valueColor else if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
