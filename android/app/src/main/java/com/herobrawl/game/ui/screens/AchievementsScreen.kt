package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.Achievements
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton

@Composable
fun AchievementsScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    val claimed = state.achievements.claimed
    val unlocked = state.achievements.unlocked

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Achievements", color = HBColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(
            "${claimed.size}/${Achievements.all.size} claimed · ${unlocked.size} unlocked",
            color = HBColors.TextDim, fontSize = 12.sp,
        )

        Achievements.all.forEach { a ->
            val isUnlocked = a.id in unlocked
            val isClaimed = a.id in claimed
            Card {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            (if (isClaimed) "✓ " else if (isUnlocked) "🏆 " else "🔒 ") + a.name,
                            color = if (isUnlocked) HBColors.Text else HBColors.TextMute,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(a.description, color = HBColors.TextDim, fontSize = 12.sp)
                        val rewards = buildString {
                            if (a.rewardGems > 0) append("💎 ${a.rewardGems}  ")
                            if (a.rewardScrolls > 0) append("📜 ${a.rewardScrolls}  ")
                            if (a.rewardFragments > 0) append("💠 ${a.rewardFragments}  ")
                        }
                        Text(rewards, color = HBColors.TextDim, fontSize = 11.sp)
                    }
                    if (Achievements.canClaim(state, a.id)) {
                        GradientButton(
                            text = "Claim",
                            onClick = {
                                update { Achievements.claim(it, a.id) }
                                notify("Achievement claimed: ${a.name}", "reward")
                            },
                            isGold = true,
                            modifier = Modifier.weight(0.4f),
                        )
                    } else {
                        OutlinedPillButton(
                            text = if (isClaimed) "Claimed" else if (isUnlocked) "Ready" else "Locked",
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(0.4f),
                        )
                    }
                }
            }
        }
    }
}
