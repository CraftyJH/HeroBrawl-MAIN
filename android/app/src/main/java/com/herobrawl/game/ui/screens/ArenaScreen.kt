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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.engine.BattleResult
import com.herobrawl.game.engine.BattleUnit
import com.herobrawl.game.engine.Combat
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton
import kotlin.random.Random

private const val DAILY_TICKETS = 10

@Composable
fun ArenaScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var opponents by remember { mutableStateOf(seedOpponents(state.arena.rating)) }
    var battle by remember { mutableStateOf<Triple<List<BattleUnit>, List<BattleUnit>, BattleResult>?>(null) }
    var pendingOpponentId by remember { mutableStateOf<String?>(null) }

    val ticketsLeft = (DAILY_TICKETS - state.arena.ticketsUsed).coerceAtLeast(0)
    val allyCount = state.lineup.slots.count { it != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Arena", color = HBColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
        val total = state.arena.wins + state.arena.losses
        val winRate = if (total == 0) "—" else "${(state.arena.wins * 100.0 / total).toInt()}%"
        Text(
            "Rating ${state.arena.rating} · Win rate $winRate · $ticketsLeft/$DAILY_TICKETS tickets today",
            color = HBColors.TextDim,
            fontSize = 12.sp,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedPillButton(
                text = "Refresh · 💎 10",
                onClick = {
                    if (state.currency.gems < 10) notify("Need 10 gems to refresh.", "warn")
                    else {
                        update { s -> s.copy(currency = s.currency.copy(gems = s.currency.gems - 10)) }
                        opponents = seedOpponents(state.arena.rating)
                    }
                }
            )
        }

        opponents.forEach { op ->
            Card {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(op.name, color = HBColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text(
                                "Rating ${op.rating}",
                                color = HBColors.TextDim,
                                fontSize = 11.sp,
                            )
                        }
                        DifficultyBadge(op.rating - state.arena.rating)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        op.heroes.forEach { inst -> MiniHero(inst) }
                    }
                    GradientButton(
                        text = "Fight!",
                        onClick = {
                            if (ticketsLeft <= 0) { notify("No arena tickets left today.", "warn"); return@GradientButton }
                            if (allyCount == 0) { notify("Set up a lineup first.", "warn"); return@GradientButton }
                            pendingOpponentId = op.id
                            val allies = Combat.buildAllyUnits(state)
                            val enemies = op.heroes.mapIndexed { slot, h ->
                                Combat.unitFromInstance(h, BattleUnit.Side.ENEMY, slot, 0.0, 0.0, 0.0)
                            }
                            val r = Combat.simulate(allies, enemies)
                            battle = Triple(allies, enemies, r)
                        },
                        enabled = ticketsLeft > 0 && allyCount > 0,
                    )
                    Text(
                        "Win: +12 💎, +2 🔮 · Lose: +4 💎",
                        color = HBColors.TextMute,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }

    val b = battle
    if (b != null) {
        BattlePlayback(
            allies = b.first,
            enemies = b.second,
            result = b.third,
            onClose = {
                val result = b.third
                val op = opponents.firstOrNull { it.id == pendingOpponentId }
                val delta = if (result.winner == BattleUnit.Side.ALLY) {
                    maxOf(6, (20 + ((op?.rating ?: state.arena.rating) - state.arena.rating) / 8))
                } else {
                    -maxOf(4, 15 - ((op?.rating ?: state.arena.rating) - state.arena.rating) / 10)
                }
                update { s ->
                    val next = s.copy(
                        arena = s.arena.copy(
                            rating = (s.arena.rating + delta).coerceAtLeast(300),
                            wins = s.arena.wins + if (result.winner == BattleUnit.Side.ALLY) 1 else 0,
                            losses = s.arena.losses + if (result.winner == BattleUnit.Side.ALLY) 0 else 1,
                            ticketsUsed = s.arena.ticketsUsed + 1,
                        ),
                        currency = s.currency.copy(
                            gems = s.currency.gems + if (result.winner == BattleUnit.Side.ALLY) 12 else 4,
                            prophetOrbs = s.currency.prophetOrbs + if (result.winner == BattleUnit.Side.ALLY) 2 else 0,
                        ),
                    )
                    val bumped = Quests.bump(next, "arena-2")
                    com.herobrawl.game.engine.PlayerProgression.givePlayerXp(bumped, 20)
                }
                notify(
                    if (result.winner == BattleUnit.Side.ALLY) "Arena win · rating +$delta"
                    else "Arena loss · rating ${delta}",
                    if (result.winner == BattleUnit.Side.ALLY) "success" else "warn"
                )
                battle = null
                opponents = seedOpponents(state.arena.rating + delta)
                pendingOpponentId = null
            }
        )
    }
}

@Composable
private fun DifficultyBadge(delta: Int) {
    val (text, color) = when {
        delta > 50 -> "HARD" to Color(0xFFFF4F9D)
        delta < -50 -> "EASY" to HBColors.Shard
        else -> "EVEN" to HBColors.TextDim
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MiniHero(h: HeroInstance) {
    val t = Heroes.byId[h.templateId] ?: return
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(t.portraitGradient[0]), Color(t.portraitGradient[1]))
                )
            )
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) { Text(t.emoji, fontSize = 20.sp) }
}

private data class Opponent(
    val id: String,
    val name: String,
    val rating: Int,
    val heroes: List<HeroInstance>,
)

private fun seedOpponents(rating: Int): List<Opponent> {
    val namePool = listOf(
        "Crown of Daggers", "Ironhand Guild", "Silver Tempest", "Ember Covenant",
        "Nightglass Circle", "Wildsong Pack", "Moonbreaker Clan", "Veilwatcher Host"
    )
    val rnd = Random(rating)
    return List(5) { i ->
        val target = (rating + rnd.nextInt(-60, 240) + i * 10).coerceAtLeast(600)
        val heroes = List(5) { s ->
            val r = when {
                rnd.nextDouble() < 0.25 -> 5
                rnd.nextDouble() < 0.55 -> 4
                else -> 3
            }
            val pool = Heroes.byRarity[r] ?: Heroes.all.filter { it.baseRarity == r }
            val t = pool[rnd.nextInt(pool.size)]
            HeroInstance(
                instanceId = "op-$i-$s",
                templateId = t.id,
                level = (10 + target / 40 + rnd.nextInt(0, 5)).coerceAtLeast(1),
                stars = r,
                gearTier = (target / 350).coerceIn(0, 6),
            )
        }
        Opponent(
            id = "op-$i",
            name = namePool[(i + rating / 100) % namePool.size],
            rating = target,
            heroes = heroes,
        )
    }.sortedBy { it.rating }
}
