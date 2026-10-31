package com.padukarafi.rapify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padukarafi.rapify.ui.navigation.Screen
import com.padukarafi.rapify.ui.theme.RapifyAppTheme
import com.padukarafi.rapify.ui.theme.Typography

/**
 * Bottom bar berbentuk pil kaca yang melayang. Indikator item aktif adalah kapsul kaca
 * yang bergeser mengikuti tab (digambar di fase draw, jadi tidak memicu recomposition).
 */
@Composable
fun GlassNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RapifyAppTheme.colors
    val shape = RoundedCornerShape(30.dp)
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val indicatorPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "nav_indicator"
    )

    val coverTint = LocalCoverTint.current
    val indicatorFill = if (colors.isDark) coverTint.copy(alpha = 0.26f) else Color.Black.copy(alpha = 0.07f)
    val indicatorRim = if (colors.isDark) Color.White.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .glass(shape, colors.isDark, GlassLevel.Floating, LocalCoverTint.current)
                .drawBehind {
                    val itemWidth = size.width / items.size
                    val inset = 6.dp.toPx()
                    val indicatorSize = Size(itemWidth - inset * 2, size.height - inset * 2)
                    val topLeft = Offset(indicatorPosition * itemWidth + inset, inset)
                    val radius = CornerRadius(indicatorSize.height / 2f)
                    drawRoundRect(indicatorFill, topLeft, indicatorSize, radius)
                    drawRoundRect(indicatorRim, topLeft, indicatorSize, radius, style = Stroke(0.75.dp.toPx()))
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, screen ->
                GlassNavItem(
                    screen = screen,
                    selected = index == selectedIndex,
                    onClick = { onNavigate(screen) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.GlassNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = RapifyAppTheme.colors
    val tint by animateColorAsState(
        targetValue = if (selected) colors.textPrimary else colors.textMuted,
        animationSpec = tween(180),
        label = "nav_item_tint"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(screen.titleRes),
            style = Typography.labelMedium.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1
        )
    }
}
