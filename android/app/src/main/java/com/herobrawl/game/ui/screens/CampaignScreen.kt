package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.herobrawl.game.engine.Combat
import com.herobrawl.game.engine.Events
import com.herobrawl.game.engine.Idle
import com.herobrawl.game.engine.Progression
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton

private val CHAPTERS = 10
private val STAGES = 10

@Composable
fun CampaignScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var battle by remember { mutableStateOf<BattleData?>(null) }
    var chapterView by remember { mutableStateOf(state.campaign.chapter) }
    val allyCount = state.lineup.slots.count { it != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Campaign", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(
                "${state.campaign.chapter}-${state.campaign.stage}",
                color = HBColors.Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
            )
        }

        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Chapter $chapterView",
                        color = HBColors.Text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedPillButton(
                            text = "‹",
                            onClick = { if (chapterView > 1) chapterView-- },
                            enabled = chapterView > 1,
                        )
                        OutlinedPillButton(
                            text = "›",
                            onClick = { if (chapterView < CHAPTERS) chapterView++ },
                            enabled = chapterView < CHAPTERS,
                        )
                    }
                }
                Text(chapterBlurb(chapterView), color = HBColors.TextDim, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                // Grid of stages, 5 wide
                val stages = (1..STAGES).toList()
                for (row in 0 until STAGES / 5) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 5) {
                            val stage = stages[row * 5 + col]
                            StageButton(
                                state = state,
                                chapter = chapterView,
                                stage = stage,
                                modifier = Modifier.weight(1f),
                                onStart = {
                                    if (allyCount == 0) {
                                        notify("Set up a lineup first.", "warn")
                                    } else {
                                        val allies = Combat.buildAllyUnits(state)
                                        val enemies = Combat.buildEnemyUnits(chapterView, stage)
                                        val result = Combat.simulate(allies, enemies)
                                        battle = BattleData(allies, enemies, result, chapterView, stage)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

    }

    val b = battle
    if (b != null) {
        BattlePlayback(
            allies = b.allies,
            enemies = b.enemies,
            result = b.result,
            onClose = {
                if (b.result.winner == com.herobrawl.game.engine.BattleUnit.Side.ALLY) {
                    val firstClear =
                        state.campaign.chapter < b.chapter ||
                        (state.campaign.chapter == b.chapter && state.campaign.stage <= b.stage)
                    val rewards = Combat.campaignRewards(b.chapter, b.stage, firstClear)

                    update { s ->
                        var next = s.copy(
                            currency = s.currency.copy(
                                gold = s.currency.gold + rewards.gold,
                                spirit = s.currency.spirit + rewards.spirit,
                                gems = s.currency.gems + rewards.firstClearGems,
                                stoneFragments = s.currency.stoneFragments + 2, // small stone drop per stage
                                dust = s.currency.dust + 2,
                            )
                        )
                        if (firstClear) {
                            var nextChapter = b.chapter
                            var nextStage = b.stage + 1
                            if (nextStage > STAGES) { nextChapter += 1; nextStage = 1 }
                            if (nextChapter > CHAPTERS) { nextChapter = CHAPTERS; nextStage = STAGES }
                            next = next.copy(
                                campaign = next.campaign.copy(chapter = nextChapter, stage = nextStage),
                                idle = next.idle.copy(ratePerHour = Idle.upgradedRates(nextChapter, nextStage)),
                            )
                        }
                        for (slot in next.lineup.slots) {
                            if (slot != null) next = Progression.giveXp(next, slot, rewards.xp)
                        }
                        next = Quests.bump(next, "campaign-3")
                        next = com.herobrawl.game.engine.PlayerProgression.givePlayerXp(next, 50 + b.chapter * 5)
                        Events.gainTokens(next, 5L)
                    }
                    notify(
                        "Stage cleared! +${rewards.gold} gold, +${rewards.spirit} spirit" +
                            if (firstClear) ", +${rewards.firstClearGems} gems" else "",
                        "reward"
                    )
                } else {
                    notify("Defeat. Try leveling your heroes.", "warn")
                }
                battle = null
            }
        )
    }
}

private data class BattleData(
    val allies: List<com.herobrawl.game.engine.BattleUnit>,
    val enemies: List<com.herobrawl.game.engine.BattleUnit>,
    val result: com.herobrawl.game.engine.BattleResult,
    val chapter: Int,
    val stage: Int,
)

@Composable
private fun StageButton(
    state: GameState,
    chapter: Int,
    stage: Int,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
) {
    val progress = (state.campaign.chapter - 1) * STAGES + state.campaign.stage
    val here = (chapter - 1) * STAGES + stage
    val done = here < progress
    val current = here == progress
    val locked = here > progress

    val bg = when {
        current -> Brush.horizontalGradient(listOf(HBColors.BrandPink, HBColors.BrandIndigo))
        done -> Brush.horizontalGradient(listOf(Color(0x332EE37F), Color(0x335EE3FF)))
        else -> Brush.horizontalGradient(listOf(HBColors.Bg2, HBColors.Bg2))
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                1.dp,
                if (current) Color.Transparent
                else if (done) Color(0x665EE3FF) else HBColors.Stroke,
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !locked) { onStart() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            (if (done) "✓ " else "") + "$chapter-$stage",
            color = if (locked) HBColors.TextMute else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
    }
}

private fun chapterBlurb(c: Int): String = when (c) {
    1 -> "The Fallow Marches — rogue sellswords burn border hamlets."
    2 -> "Ashkeep Ridge — the Horde wakes an old fire."
    3 -> "The Witchwood — roots older than kings."
    4 -> "Glass Spire — Arcane towers flicker. Something drinks the light."
    5 -> "Dawnmarch — Radiance opens its gates, and the Abyss answers."
    6 -> "The Shardlands — broken geometry, broken gods."
    7 -> "Voidreach — stars falling, bargains forming."
    8 -> "The Hollow Throne — a queen of shadows wakes."
    9 -> "Ember & Ash — the final shore before the last light."
    10 -> "Endgame — HeroBrawl's last trial. Beat it. We dare you."
    else -> "Unknown lands."
}
