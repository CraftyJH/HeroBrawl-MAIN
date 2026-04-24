package com.herobrawl.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Factions
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.engine.Stats
import com.herobrawl.game.model.HeroInstance

@Composable
fun HeroCard(
    hero: HeroInstance,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val template = Heroes.byId[hero.templateId] ?: return
    val faction = Factions.all.getValue(template.faction)
    val cls = Classes.all.getValue(template.heroClass)
    val gradient = Brush.linearGradient(
        colors = listOf(Color(template.portraitGradient[0]), Color(template.portraitGradient[1]))
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HBColors.BrandPink else HBColors.Stroke,
                shape = RoundedCornerShape(16.dp)
            )
            .background(HBColors.Bg2)
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
                .background(gradient)
        ) {
            // Rarity tag (top right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, HBColors.RarityLegendary.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("Lv ${hero.level}", color = HBColors.RarityLegendary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            // Faction/Class badges (top left)
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Badge(text = faction.name, bg = Color(faction.color), textColor = Color.Black)
                Badge(text = cls.name, bg = Color.Black.copy(alpha = 0.5f), textColor = Color.White)
            }
            // Emoji (centered)
            Text(
                text = template.emoji,
                fontSize = 56.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            // Stars (bottom center)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, HBColors.RarityLegendary.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "★".repeat(hero.stars),
                    color = HBColors.RarityLegendary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (!compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, color = HBColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(
                        template.title.uppercase(),
                        color = HBColors.TextMute,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                Text("⚡ ${Stats.power(hero)}", color = HBColors.TextDim, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Badge(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun HpBar(current: Int, max: Int, modifier: Modifier = Modifier) {
    val pct = if (max <= 0) 0f else current.toFloat() / max
    val low = pct < 0.33f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(pct.coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        if (low) listOf(Color(0xFFFF4F4F), Color(0xFFFF9A4A))
                        else listOf(Color(0xFF2EE37F), Color(0xFF5EE3FF))
                    )
                )
        )
    }
}

@Composable
fun EnergyBar(energy: Int, modifier: Modifier = Modifier) {
    val pct = (energy / 100f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(pct)
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFD04A), Color(0xFFFF4F9D))
                    )
                )
        )
    }
}

@Composable
fun LineupSlotPlaceholder(
    slotIndex: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            if (slotIndex < 3) "🛡️ Front" else "🏹 Back",
            color = HBColors.TextMute,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
        Text("Slot ${slotIndex + 1}", color = HBColors.TextMute, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
    }
}
