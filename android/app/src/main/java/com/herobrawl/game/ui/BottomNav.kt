package com.herobrawl.game.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNav(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0F1D), Color(0xFF0A0B14))
                )
            )
            .border(1.dp, HBColors.Stroke)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (tab in BottomTab.values()) {
                NavItem(
                    tab = tab,
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                    centerFeature = tab == BottomTab.SUMMON,
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    centerFeature: Boolean,
) {
    val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "nav-scale")
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (centerFeature) {
            // Summon button protrudes slightly
            Box(
                modifier = Modifier
                    .size((54 * scale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFD04A), Color(0xFFFF4F9D), Color(0xFF6B61FF))
                        )
                    )
                    .border(
                        2.dp,
                        if (selected) Color.White else HBColors.StrokeBright,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.emoji, fontSize = 26.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .size((40 * scale).dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Brush.linearGradient(
                            listOf(Color(0xFF6B61FF), Color(0xFFFF4F9D))
                        )
                        else Brush.linearGradient(
                            listOf(Color(0xFF252842), Color(0xFF1B1D2D))
                        )
                    )
                    .border(
                        1.dp,
                        if (selected) Color.Transparent else HBColors.Stroke,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.emoji, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            tab.label,
            color = if (selected) HBColors.Text else HBColors.TextMute,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
        )
    }
}
