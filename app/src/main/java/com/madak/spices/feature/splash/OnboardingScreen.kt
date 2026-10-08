package com.madak.spices.feature.splash

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madak.spices.R
import com.madak.spices.data.settings.SettingsRepository
import com.madak.spices.designsystem.component.LogoPart
import com.madak.spices.designsystem.component.MadakLogo
import com.madak.spices.designsystem.theme.MadakColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val settings: SettingsRepository) : ViewModel() {
    fun complete(then: () -> Unit) {
        viewModelScope.launch { settings.setOnboardingDone(); then() }
    }
}

private data class Page(@StringRes val title: Int, @StringRes val body: Int, val accent: Color)

@Composable
fun OnboardingScreen(onDone: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val pages = listOf(
        Page(R.string.onb_title_1, R.string.onb_body_1, MadakColors.Turmeric),
        Page(R.string.onb_title_2, R.string.onb_body_2, MadakColors.Magenta),
        Page(R.string.onb_title_3, R.string.onb_body_3, MadakColors.Leaf),
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val finish = { viewModel.complete(onDone) }

    Column(Modifier.fillMaxSize().background(MadakColors.Ink).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = finish) { Text(stringResource(R.string.action_skip), color = MadakColors.Beige) }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { index ->
            val page = pages[index]
            val offset = ((pager.currentPage - index) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp).graphicsLayer {
                    alpha = 1f - offset * 0.7f
                    val s = 1f - offset * 0.15f
                    scaleX = s; scaleY = s
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Brush.radialGradient(listOf(page.accent.copy(alpha = 0.45f), Color.Transparent)), radius = size.minDimension / 2f)
                        drawCircle(MadakColors.Gold.copy(alpha = 0.5f), radius = size.minDimension * 0.42f, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                        repeat(14) { i ->
                            val a = i / 14f * 6.283f
                            drawCircle(
                                MadakColors.spicePalette[i % MadakColors.spicePalette.size],
                                radius = 5f + (i % 3) * 3f,
                                center = Offset(center.x + kotlin.math.cos(a) * size.minDimension * 0.42f, center.y + kotlin.math.sin(a) * size.minDimension * 0.42f),
                            )
                        }
                    }
                    when (index) {
                        0 -> MadakLogo(Modifier.width(150.dp), part = LogoPart.CHEF)
                        1 -> Icon(Icons.Rounded.Restaurant, null, tint = Color.White, modifier = Modifier.size(110.dp))
                        else -> Icon(Icons.Rounded.LocalShipping, null, tint = Color.White, modifier = Modifier.size(110.dp))
                    }
                }
                Spacer(Modifier.height(40.dp))
                Text(stringResource(page.title), style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge, color = MadakColors.Beige.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pages.indices.forEach { i ->
                    val w by animateDpAsState(if (i == pager.currentPage) 26.dp else 8.dp, label = "dot")
                    Box(
                        Modifier.height(8.dp).width(w).clip(CircleShape)
                            .background(if (i == pager.currentPage) MadakColors.Magenta else Color.White.copy(alpha = 0.25f))
                    )
                }
            }
            val last = pager.currentPage == pages.lastIndex
            Button(
                onClick = { if (last) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                colors = ButtonDefaults.buttonColors(containerColor = MadakColors.Magenta),
                shape = RoundedCornerShape(50),
                modifier = Modifier.animateContentSize(),
            ) {
                Text(stringResource(if (last) R.string.action_start else R.string.action_next), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}
