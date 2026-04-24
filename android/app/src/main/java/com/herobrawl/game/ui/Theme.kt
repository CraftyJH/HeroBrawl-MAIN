package com.herobrawl.game.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object HBColors {
    val Bg0 = Color(0xFF0A0B14)
    val Bg1 = Color(0xFF12131F)
    val Bg2 = Color(0xFF1B1D2D)
    val Bg3 = Color(0xFF252842)
    val Stroke = Color(0xFF2A2D45)
    val StrokeBright = Color(0xFF3A3F60)
    val Text = Color(0xFFE9ECFF)
    val TextDim = Color(0xFFA8AED1)
    val TextMute = Color(0xFF6D7498)
    val Gold = Color(0xFFFFD966)
    val Gems = Color(0xFF5EE3FF)
    val Spirit = Color(0xFFC4B4FF)
    val Orb = Color(0xFFFFB86B)
    val Scroll = Color(0xFFFF9AD1)
    val Shard = Color(0xFF7DF5C5)
    val Fragment = Color(0xFFFF7AC4)
    val Dust = Color(0xFFD5FF66)
    val BrandPink = Color(0xFFFF4F9D)
    val BrandIndigo = Color(0xFF6B61FF)
    val BrandGold = Color(0xFFFFD04A)

    val RarityEpic = Color(0xFFC4B4FF)
    val RarityLegendary = Color(0xFFFFD04A)
}

private val DarkScheme = darkColorScheme(
    background = HBColors.Bg0,
    surface = HBColors.Bg1,
    onBackground = HBColors.Text,
    onSurface = HBColors.Text,
    primary = HBColors.BrandPink,
    onPrimary = Color.White,
    secondary = HBColors.BrandIndigo,
    onSecondary = Color.White,
    tertiary = HBColors.BrandGold,
)

@Composable
fun HeroBrawlTheme(content: @Composable () -> Unit) {
    val scheme = DarkScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(
            titleLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 24.sp, color = HBColors.Text),
            titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HBColors.Text),
            bodyLarge = TextStyle(fontSize = 15.sp, color = HBColors.Text),
            bodyMedium = TextStyle(fontSize = 13.sp, color = HBColors.TextDim),
            labelSmall = TextStyle(fontSize = 11.sp, color = HBColors.TextMute, fontWeight = FontWeight.SemiBold),
        ),
        content = content,
    )
    // Suppress isSystemInDarkTheme warning — we're dark-only by design.
    @Suppress("UNUSED_VARIABLE")
    val dark = isSystemInDarkTheme()
}
