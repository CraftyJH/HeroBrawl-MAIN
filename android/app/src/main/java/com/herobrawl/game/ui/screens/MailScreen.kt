package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.MailEngine
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton

@Composable
fun MailScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Mailbox", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedPillButton(text = "Trash read", onClick = { update { MailEngine.deleteClaimed(it) } })
                GradientButton(
                    text = "Claim All",
                    onClick = {
                        update { MailEngine.claimAll(it) }
                        notify("Claimed all mail.", "reward")
                    },
                    enabled = MailEngine.unclaimedCount(state) > 0,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    isGold = true,
                )
            }
        }

        if (state.mail.messages.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("📭", fontSize = 72.sp)
                Text("No mail.", color = HBColors.TextDim, fontSize = 14.sp)
                Text("Monthly cards, event rewards, and gifts land here.", color = HBColors.TextMute, fontSize = 11.sp)
            }
            return@Column
        }

        state.mail.messages.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (m.claimed) HBColors.Bg2.copy(alpha = 0.6f) else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (m.claimed) HBColors.Stroke else HBColors.Gold.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (m.claimed) "📭" else "📬", fontSize = 32.sp)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(Modifier.weight(1f)) {
                    Text(m.subject, color = HBColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("from ${m.sender}", color = HBColors.TextMute, fontSize = 10.sp)
                    Text(m.body, color = HBColors.TextDim, fontSize = 11.sp, maxLines = 3)
                    if (m.rewards.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            m.rewards.forEach {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    val icon = when (it.kind) {
                                        "gems" -> "💎"; "gold" -> "🪙"; "heroicScrolls" -> "📜"
                                        "prophetOrbs" -> "🔮"; "stoneFragments" -> "💠"; "dust" -> "🧪"
                                        else -> "🎁"
                                    }
                                    Text("$icon ${it.amount}", color = HBColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.padding(horizontal = 4.dp))
                if (!m.claimed && m.rewards.isNotEmpty()) {
                    GradientButton(
                        text = "Claim",
                        onClick = { update { MailEngine.claim(it, m.id) } },
                        modifier = Modifier.padding(4.dp),
                    )
                } else {
                    OutlinedPillButton(text = "✓", onClick = { update { MailEngine.delete(it, m.id) } })
                }
            }
        }
    }
}
