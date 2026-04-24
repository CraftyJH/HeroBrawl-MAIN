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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Factions
import com.herobrawl.game.engine.Gacha
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.model.FactionId
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.ChipRow
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton
import com.herobrawl.game.ui.ProgressBar

@Composable
fun SummonScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var showResults by remember { mutableStateOf<List<Gacha.PullResult>?>(null) }
    var factionPick by remember { mutableStateOf("Vanguard") }

    val pity = state.gacha.pityCount
    val toPity = 60 - pity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text("Summon", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("⭐ $pity/60", color = HBColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        ProgressBar((pity / 60f).coerceIn(0f, 1f))

        // Heroic banner
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x406B61FF), Color(0x40FF4F9D))
                            )
                        )
                        .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "HEROIC SUMMON",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Forge of Legends",
                            color = HBColors.Text,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "Ramp 30+ · guarantee 60.",
                            color = HBColors.TextDim,
                            fontSize = 11.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RateChip("7%", "5★", Modifier.weight(1f))
                            RateChip("28%", "4★", Modifier.weight(1f))
                            RateChip("65%", "3★", Modifier.weight(1f))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedPillButton(
                        text = "x1 · 📜 1",
                        onClick = { doPull(state, update, notify, 1, null) { r -> showResults = r } },
                        enabled = state.currency.heroicScrolls >= 1,
                        modifier = Modifier.weight(1f),
                    )
                    GradientButton(
                        text = "x10 · 📜 10",
                        onClick = { doPull(state, update, notify, 10, null) { r -> showResults = r } },
                        enabled = state.currency.heroicScrolls >= 10,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Prophet banner
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x40FFD04A), Color(0x40FF9AD1))
                            )
                        )
                        .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "PROPHET SUMMON",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Oracle's Tree",
                            color = HBColors.Text,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "Target one faction.",
                            color = HBColors.TextDim,
                            fontSize = 11.sp,
                        )
                    }
                }
                ChipRow(
                    options = Factions.all.values.map { it.name },
                    selected = factionPick,
                    onChange = { factionPick = it },
                )
                val fid = Factions.all.values.firstOrNull { it.name == factionPick }?.id ?: FactionId.VANGUARD
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedPillButton(
                        text = "x1 · 🔮 1",
                        onClick = { doPull(state, update, notify, 1, fid) { r -> showResults = r } },
                        enabled = state.currency.prophetOrbs >= 1,
                        modifier = Modifier.weight(1f),
                    )
                    GradientButton(
                        text = "x10 · 🔮 10",
                        onClick = { doPull(state, update, notify, 10, fid) { r -> showResults = r } },
                        enabled = state.currency.prophetOrbs >= 10,
                        isGold = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    val results = showResults
    if (results != null) {
        AlertDialog(
            onDismissRequest = { showResults = null },
            containerColor = HBColors.Bg1,
            titleContentColor = HBColors.Text,
            textContentColor = HBColors.TextDim,
            title = { Text("Summon Results", fontWeight = FontWeight.Black) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    results.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(r.template.portraitGradient[0]).copy(alpha = 0.45f),
                                            Color(r.template.portraitGradient[1]).copy(alpha = 0.25f),
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (r.rarity >= 5) HBColors.RarityLegendary
                                    else if (r.rarity >= 4) HBColors.RarityEpic
                                    else HBColors.Stroke,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(r.template.emoji, fontSize = 28.sp)
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.template.name, color = HBColors.Text, fontWeight = FontWeight.Bold)
                                Text(
                                    "${"★".repeat(r.rarity)} · ${r.template.title}" +
                                        if (r.isDuplicate) " · echo+" else " · NEW",
                                    color = HBColors.TextDim,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showResults = null }) {
                    Text("Awesome!", color = HBColors.BrandPink)
                }
            },
        )
    }
}

@Composable
private fun RateChip(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(value, color = HBColors.RarityLegendary, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = HBColors.TextDim, fontSize = 10.sp)
    }
}

private fun doPull(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
    count: Int,
    faction: FactionId?,
    onResults: (List<Gacha.PullResult>) -> Unit,
) {
    val needScrolls = faction == null
    if (needScrolls && state.currency.heroicScrolls < count) {
        notify("Not enough Heroic Scrolls.", "warn"); return
    }
    if (faction != null && state.currency.prophetOrbs < count) {
        notify("Not enough Prophet Orbs.", "warn"); return
    }

    update { s ->
        val summary = Gacha.pull(s, count, faction)
        val updated = if (needScrolls) {
            summary.state.copy(
                currency = summary.state.currency.copy(
                    heroicScrolls = summary.state.currency.heroicScrolls - count
                )
            )
        } else {
            summary.state.copy(
                currency = summary.state.currency.copy(
                    prophetOrbs = summary.state.currency.prophetOrbs - count
                )
            )
        }
        onResults(summary.results)
        val epics = summary.results.count { it.rarity >= 5 }
        if (epics > 0) notify("Summoned $epics legendary hero${if (epics > 1) "es" else ""}!", "reward")
        Quests.bump(updated, "pulls-1", count)
    }
}
