package com.herobrawl.game.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Card(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E2032), Color(0xFF121420))
                )
            )
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(16.dp))
            .padding(paddingValues)
            .animateContentSize()
    ) {
        content()
    }
}

@Composable
fun Pill(
    icon: String,
    text: String,
    color: Color = HBColors.Text,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(HBColors.Bg2)
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(text, color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    leading: (@Composable (() -> Unit))? = null,
    isGold: Boolean = false,
) {
    val bg = if (isGold)
        Brush.horizontalGradient(listOf(Color(0xFFFFB54A), Color(0xFFFF7E3D)))
    else
        Brush.horizontalGradient(listOf(HBColors.BrandPink, HBColors.BrandIndigo))
    val textColor = if (isGold) Color(0xFF2B1800) else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) bg else Brush.horizontalGradient(listOf(HBColors.Bg3, HBColors.Bg3)))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        leading?.invoke()
        if (leading != null) Spacer(Modifier.width(8.dp))
        Text(text, color = if (enabled) textColor else HBColors.TextMute, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun OutlinedPillButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, HBColors.StrokeBright, RoundedCornerShape(10.dp))
            .background(HBColors.Bg2)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 10.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, color = if (enabled) HBColors.Text else HBColors.TextMute, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(HBColors.BrandGold, HBColors.BrandPink)
                    )
                )
        )
    }
}

@Composable
fun ChipRow(
    options: List<String>,
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(options) { opt ->
            val active = opt == selected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) HBColors.Bg3 else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (active) HBColors.StrokeBright else HBColors.Stroke,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { onChange(opt) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    opt,
                    color = if (active) HBColors.Text else HBColors.TextDim,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text.uppercase(),
            color = HBColors.TextDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
    }
}
