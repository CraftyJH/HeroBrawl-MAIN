package com.herobrawl.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton
import com.herobrawl.game.ui.ProgressBar

@Composable
fun QuestsScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    val canCompletion = Quests.canClaimCompletion(state)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Daily Quests", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)

        Card {
            Column {
                Text("ALL-CLEAR BONUS", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Complete every daily quest for +${Quests.completionGems} 💎 and +${Quests.completionScrolls} 📜.",
                    color = HBColors.TextMute, fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                GradientButton(
                    text = if (state.quests.completionClaimed) "Already claimed"
                    else if (canCompletion) "Claim bonus"
                    else "Complete all quests first",
                    onClick = {
                        update { Quests.claimCompletion(it) }
                        notify("All quests complete! Bonus claimed.", "reward")
                    },
                    enabled = canCompletion,
                    isGold = true,
                )
            }
        }

        Quests.dailies.forEach { q ->
            val progress = state.quests.progress[q.id] ?: 0
            val claimed = state.quests.claimed[q.id] == true
            val canClaim = Quests.canClaim(state, q.id)

            Card {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(q.name, color = HBColors.Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(q.description, color = HBColors.TextDim, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        ProgressBar((progress.toFloat() / q.target).coerceIn(0f, 1f))
                        Text("$progress / ${q.target}", color = HBColors.TextMute, fontSize = 10.sp)
                        Spacer(Modifier.height(4.dp))
                        val rewards = buildString {
                            if (q.rewardGems > 0) append("💎 ${q.rewardGems}  ")
                            if (q.rewardGold > 0) append("🪙 ${q.rewardGold}  ")
                            if (q.rewardScrolls > 0) append("📜 ${q.rewardScrolls}  ")
                            if (q.rewardDust > 0) append("🧪 ${q.rewardDust}  ")
                            if (q.rewardOrbs > 0) append("🔮 ${q.rewardOrbs}  ")
                            if (q.rewardFragments > 0) append("💠 ${q.rewardFragments}  ")
                        }
                        Text(rewards, color = HBColors.TextDim, fontSize = 11.sp)
                    }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    if (canClaim) {
                        GradientButton(text = "Claim", onClick = {
                            update { Quests.claim(it, q.id) }
                        }, modifier = Modifier.weight(0.5f))
                    } else {
                        OutlinedPillButton(
                            text = if (claimed) "Claimed" else "Incomplete",
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(0.5f),
                        )
                    }
                }
            }
        }
    }
}
