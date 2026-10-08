package com.madak.spices.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.madak.spices.R
import com.madak.spices.data.model.Product
import com.madak.spices.designsystem.component.ProductImage
import com.madak.spices.designsystem.component.RatingBadge
import com.madak.spices.designsystem.component.bounceClick
import kotlinx.coroutines.delay

val LocalSnackbar = staticCompositionLocalOf { SnackbarHostState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MadakTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (isFavorite) 1.15f else 1f, spring(dampingRatio = Spring.DampingRatioHighBouncy), label = "fav")
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .bounceClick {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(R.string.favorites_title),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}

/** Round "+" button that morphs into a check mark for a moment after adding. */
@Composable
fun QuickAddButton(onAdd: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val haptic = LocalHapticFeedback.current
    var added by remember { mutableStateOf(false) }
    LaunchedEffect(added) { if (added) { delay(1200); added = false } }
    Box(
        modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
            .bounceClick(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                added = true
                onAdd()
            },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(added, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = "quickAdd") { done ->
            Icon(
                if (done) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = stringResource(R.string.action_add_to_cart),
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    Surface(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                ProductImage(product.entity.imageUrl, product.entity.colorArgb, product.id, Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)))
                FavoriteButton(product.isFavorite, onToggleFavorite, Modifier.align(Alignment.TopEnd).padding(10.dp))
                if (product.entity.reviewsCount > 0) RatingBadge(product.entity.rating, Modifier.align(Alignment.BottomStart).padding(10.dp))
            }
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(product.name(language), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.price_from, ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(price(product.startingPrice), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    QuickAddButton(onQuickAdd, enabled = product.inStock)
                }
            }
        }
    }
}
