package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Factions
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.engine.Idle
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.engine.Stats
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.HeroCard
import com.herobrawl.game.ui.LineupSlotPlaceholder
import com.herobrawl.game.ui.OutlinedPillButton
import com.herobrawl.game.ui.ProgressBar
import com.herobrawl.game.ui.Tab
import com.herobrawl.game.ui.formatNum
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
    go: (Tab) -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val reward = Idle.compute(state, now)
    val lineupHeroes = state.lineup.slots.mapNotNull { id ->
        id?.let { state.heroes.firstOrNull { h -> h.instanceId == it } }
    }
    val aura = Factions.lineupAura(lineupHeroes.map { Stats.templateFor(it).faction })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2B1948), Color(0xFF6B61FF), Color(0xFFFF4F9D))
                        )
                    )
                    .border(1.dp, HBColors.Stroke, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("⚔️", fontSize = 64.sp)
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Welcome, ${state.playerName}",
                    color = HBColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Chapter ${state.campaign.chapter}, Stage ${state.campaign.stage} · Arena ${state.arena.rating}",
                    color = HBColors.TextDim,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradientButton(
                        text = "Continue",
                        onClick = { go(Tab.CAMPAIGN) },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedPillButton(
                        text = "Summon",
                        onClick = { go(Tab.SUMMON) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Idle rewards card
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("IDLE REWARDS", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Cap: ${state.idle.capHours}h  (+50% vs the genre's 8h)",
                    color = HBColors.TextMute,
                    fontSize = 11.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResourceTile("Gold", "+${formatNum(reward.gold)}", HBColors.Gold, Modifier.weight(1f))
                    ResourceTile("Spirit", "+${formatNum(reward.spirit)}", HBColors.Spirit, Modifier.weight(1f))
                    ResourceTile("Shards", "+${formatNum(reward.shards)}", HBColors.Shard, Modifier.weight(1f))
                }
                ProgressBar((reward.hours / state.idle.capHours).toFloat())
                Text(
                    if (reward.capped) "⚠ Idle cap reached — claim now!"
                    else "Accrued ${"%.2f".format(reward.hours)}h",
                    color = if (reward.capped) HBColors.Gold else HBColors.TextMute,
                    fontSize = 11.sp,
                )
                GradientButton(
                    text = "Claim Idle Rewards",
                    onClick = {
                        update { s ->
                            val claimed = Idle.claim(s, System.currentTimeMillis())
                            Quests.bump(claimed, "idle-claim")
                        }
                        notify("Claimed idle rewards.", "reward")
                    },
                )
            }
        }

        // Lineup preview
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LINEUP", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${aura.label} · +${(aura.attack * 100).toInt()}% ATK, +${(aura.health * 100).toInt()}% HP",
                    color = HBColors.TextMute,
                    fontSize = 11.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.lineup.slots.forEachIndexed { idx, id ->
                        val h = id?.let { state.heroes.firstOrNull { he -> he.instanceId == it } }
                        if (h != null) {
                            HeroCard(h, compact = true, modifier = Modifier.weight(1f).aspectRatio(0.75f))
                        } else {
                            LineupSlotPlaceholder(slotIndex = idx, onClick = { go(Tab.ROSTER) },
                                modifier = Modifier.weight(1f).aspectRatio(0.75f))
                        }
                    }
                }
                OutlinedPillButton(text = "Edit Lineup →", onClick = { go(Tab.ROSTER) })
            }
        }

        // Why HeroBrawl
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("WHY HEROBRAWL", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                BulletLine("🎯 Transparent pity — guaranteed legendary every 60 pulls")
                BulletLine("♻️ Echo stacks — duplicates never wasted")
                BulletLine("⏳ 12h idle cap (+50%)")
                BulletLine("🌟 No VIP paywall — every hero reachable F2P")
                BulletLine("⚡ Clear faction wheel")
                BulletLine("💠 Equipment Stones + Skill Leveling + Events")
            }
        }

        // Recent messages
        if (state.messages.isNotEmpty()) {
            Card {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("RECENT", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    state.messages.take(6).forEach {
                        val color = when (it.kind) {
                            "reward" -> HBColors.Gold
                            "success" -> HBColors.Shard
                            "warn" -> Color(0xFFFF9A9A)
                            else -> HBColors.TextDim
                        }
                        Text(it.text, color = color, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label.uppercase(), color = HBColors.TextMute, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BulletLine(text: String) {
    Text(text, color = HBColors.TextDim, fontSize = 13.sp)
}
