package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.engine.Events
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton
import com.herobrawl.game.ui.ProgressBar

@Composable
fun EventsScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    val event = Events.active(state)
    val featured = Heroes.byId[event.featuredHeroId]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Events", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)

        Card {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (featured != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(featured.portraitGradient[0]),
                                            Color(featured.portraitGradient[1])
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Text(featured.emoji, fontSize = 64.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(event.name, color = HBColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(event.tagline, color = HBColors.TextDim, fontSize = 12.sp)
                        if (featured != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Featured: ${featured.name}, ${featured.title}",
                                color = HBColors.BrandGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Event Tokens: ${state.events.tokensEarned}",
                        color = HBColors.BrandGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    val maxTokens = event.milestones.lastOrNull()?.tokensRequired ?: 1
                    ProgressBar((state.events.tokensEarned.toFloat() / maxTokens).coerceIn(0f, 1f))
                }

                event.milestones.forEach { m ->
                    val claimed = m.id in state.events.milestonesClaimed
                    val canClaim = Events.canClaim(state, m.id)
                    val rewards = buildString {
                        if (m.rewardGems > 0) append("💎 ${m.rewardGems}  ")
                        if (m.rewardScrolls > 0) append("📜 ${m.rewardScrolls}  ")
                        if (m.rewardOrbs > 0) append("🔮 ${m.rewardOrbs}  ")
                        if (m.rewardFragments > 0) append("💠 ${m.rewardFragments}  ")
                        if (m.rewardHeroShardId != null) append("🌟 ${Heroes.byId[m.rewardHeroShardId]?.name ?: "Hero"}  ")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HBColors.Bg2)
                            .border(1.dp, if (canClaim) HBColors.BrandGold else HBColors.Stroke, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Milestone · ${m.tokensRequired} tokens",
                                color = HBColors.Text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                            Text(rewards, color = HBColors.TextDim, fontSize = 11.sp)
                        }
                        if (canClaim) {
                            GradientButton(
                                text = "Claim",
                                onClick = {
                                    update { Events.claim(it, m.id) }
                                    notify("Event milestone claimed!", "reward")
                                },
                                isGold = true,
                                modifier = Modifier.weight(0.45f),
                            )
                        } else {
                            OutlinedPillButton(
                                text = if (claimed) "✓ Claimed" else "Locked",
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.weight(0.45f),
                            )
                        }
                    }
                }
            }
        }

    }
}
