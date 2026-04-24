package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.herobrawl.game.data.Vip
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.HBColors

@Composable
fun VipScreen(state: GameState) {
    val cur = state.vip.level
    val (xpInLevel, xpNeeded) = Vip.xpToNextLevel(state.currency.vipXp)
    val pct = if (xpNeeded <= 0) 1f else (xpInLevel.toFloat() / xpNeeded).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFD04A), Color(0xFFFF7AA3), Color(0xFF6B61FF))
                    )
                )
                .padding(16.dp),
        ) {
            Column {
                Text("👑  VIP $cur", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("${Vip.tierFor(cur).perks.joinToString(" · ")}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                    )
                }
                Text(
                    "VIP XP: ${xpInLevel}/${xpNeeded}  •  Total: ${state.currency.vipXp}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                )
            }
        }
        Text("Earn VIP XP by buying any pack or gems. No heroes are VIP-locked.", color = HBColors.TextDim, fontSize = 11.sp)

        Vip.tiers.forEach { t ->
            val reached = cur >= t.level
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (reached) HBColors.Bg3 else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (reached) HBColors.BrandGold else HBColors.Stroke,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (reached) HBColors.BrandGold else HBColors.Bg3)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "VIP ${t.level}",
                            color = if (reached) Color(0xFF2B1800) else HBColors.TextDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        "XP ${t.xpRequired}",
                        color = HBColors.TextMute,
                        fontSize = 10.sp,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    if (t.dailyGemsBonus > 0) {
                        Text("+${t.dailyGemsBonus}💎/day", color = HBColors.Gems, fontSize = 10.sp)
                    }
                    if (t.idleCapBonusHours > 0) {
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("+${t.idleCapBonusHours}h idle", color = HBColors.Shard, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
                t.perks.forEach { perk ->
                    Text(
                        "• $perk",
                        color = if (reached) HBColors.Text else HBColors.TextMute,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
