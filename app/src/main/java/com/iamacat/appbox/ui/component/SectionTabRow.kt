package com.iamacat.appbox.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.ui.theme.GreenifyColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Compact, horizontally scrollable category tabs in the classic launcher style.
 * A single green underline slides and resizes to the selected tab.
 */
@Composable
fun SectionTabRow(
    sections: List<AppSection>,
    selectedSectionId: String?,
    onSectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val tabBounds = remember { mutableStateMapOf<String, IntRange>() }
    val indicatorX = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    var hasIndicator by remember { mutableStateOf(false) }
    val selectedBounds = selectedSectionId?.let { tabBounds[it] }

    LaunchedEffect(selectedSectionId, selectedBounds, scrollState.maxValue) {
        val range = selectedBounds ?: return@LaunchedEffect
        val tabWidth = range.tabWidth
        if (hasIndicator) {
            launch {
                indicatorX.animateTo(
                    targetValue = range.first.toFloat(),
                    animationSpec = tween(INDICATOR_ANIMATION_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                indicatorWidth.animateTo(
                    targetValue = tabWidth.toFloat(),
                    animationSpec = tween(INDICATOR_ANIMATION_MS, easing = FastOutSlowInEasing)
                )
            }
        } else {
            indicatorX.snapTo(range.first.toFloat())
            indicatorWidth.snapTo(tabWidth.toFloat())
            hasIndicator = true
        }

        val viewport = scrollState.viewportSize
        if (viewport > 0) {
            val visible = range.first >= scrollState.value &&
                range.last < scrollState.value + viewport
            if (!visible) {
                val target = (range.first - (viewport - tabWidth) / 2)
                    .coerceIn(0, scrollState.maxValue)
                scrollState.animateScrollTo(target)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .drawBehind {
                    if (indicatorWidth.value > 0f) {
                        val thickness = 2.dp.toPx()
                        drawRoundRect(
                            color = GreenifyColors.Primary,
                            topLeft = Offset(indicatorX.value, size.height - thickness),
                            size = Size(indicatorWidth.value, thickness),
                            cornerRadius = CornerRadius(thickness / 2f)
                        )
                    }
                }
        ) {
            sections.forEach { section ->
                val selected = section.id == selectedSectionId
                val contentColor by animateColorAsState(
                    targetValue = if (selected) GreenifyColors.Primary else Color(section.colorArgb),
                    animationSpec = tween(INDICATOR_ANIMATION_MS),
                    label = "tabContentColor"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onSectionSelected(section.id) }
                        )
                        .widthIn(min = 64.dp)
                        .onGloballyPositioned { coordinates ->
                            val start = coordinates.positionInParent().x.roundToInt()
                            tabBounds[section.id] = start until (start + coordinates.size.width)
                        }
                        .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = SectionIcons.iconFor(section.type),
                        contentDescription = null,
                        tint = Color(section.colorArgb),
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = section.name,
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

private const val INDICATOR_ANIMATION_MS = 220

private val IntRange.tabWidth: Int get() = last - first + 1
