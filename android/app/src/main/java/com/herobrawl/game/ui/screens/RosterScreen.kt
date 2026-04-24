package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Factions
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.engine.Progression
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.engine.Stats
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.StoneKind
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.ChipRow
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.HeroCard
import com.herobrawl.game.ui.LineupSlotPlaceholder
import com.herobrawl.game.ui.OutlinedPillButton
import com.herobrawl.game.ui.formatNum

@Composable
fun RosterScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf<Int?>(null) }
    var factionFilter by remember { mutableStateOf("All factions") }
    var classFilter by remember { mutableStateOf("All classes") }
    var sort by remember { mutableStateOf("Power") }

    val filtered = state.heroes.filter { h ->
        val t = Heroes.byId[h.templateId] ?: return@filter false
        (factionFilter == "All factions" || t.faction.name.equals(factionFilter, true) ||
            Factions.all[t.faction]?.name == factionFilter) &&
            (classFilter == "All classes" || Classes.all[t.heroClass]?.name == classFilter)
    }.let { list ->
        when (sort) {
            "Power" -> list.sortedByDescending { Stats.power(it) }
            "Level" -> list.sortedByDescending { it.level }
            "Stars" -> list.sortedByDescending { it.stars }
            else -> list.sortedBy { Heroes.byId[it.templateId]?.name ?: "" }
        }
    }

    val selectedHero = selectedId?.let { id -> state.heroes.firstOrNull { it.instanceId == id } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Heroes", color = HBColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("${state.heroes.size} collected. Tap a hero to inspect; tap a slot to assign.",
            color = HBColors.TextDim, fontSize = 12.sp)

        // Lineup editor
        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BATTLE LINEUP", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    state.lineup.slots.forEachIndexed { idx, id ->
                        val h = id?.let { state.heroes.firstOrNull { he -> he.instanceId == it } }
                        if (h != null) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(0.75f).clickable { picking = idx }) {
                                HeroCard(h, compact = true)
                            }
                        } else {
                            LineupSlotPlaceholder(idx, onClick = { picking = idx },
                                modifier = Modifier.weight(1f).aspectRatio(0.75f))
                        }
                    }
                }
            }
        }

        // Filters
        val factionOptions = listOf("All factions") + Factions.all.values.map { it.name }
        val classOptions = listOf("All classes") + Classes.all.values.map { it.name }
        ChipRow(factionOptions, factionFilter, { factionFilter = it })
        ChipRow(classOptions, classFilter, { classFilter = it })
        ChipRow(listOf("Power", "Level", "Stars", "Name"), sort, { sort = it })

        // Grid
        if (filtered.isEmpty()) {
            Card { Text("No heroes match this filter. Summon to recruit more!", color = HBColors.TextDim) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height((((filtered.size + 1) / 2) * 230).dp),
            ) {
                items(filtered, key = { it.instanceId }) { h ->
                    HeroCard(h, onClick = { selectedId = h.instanceId })
                }
            }
        }
    }

    if (picking != null) {
        val slotIndex = picking!!
        AlertDialog(
            onDismissRequest = { picking = null },
            containerColor = HBColors.Bg1,
            titleContentColor = HBColors.Text,
            textContentColor = HBColors.TextDim,
            title = { Text("Assign slot ${slotIndex + 1}", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    TextButton(onClick = {
                        update { s ->
                            val slots = s.lineup.slots.toMutableList()
                            slots[slotIndex] = null
                            s.copy(lineup = s.lineup.copy(slots = slots))
                        }
                        picking = null
                    }) { Text("Clear slot", color = HBColors.TextDim) }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(380.dp),
                    ) {
                        items(state.heroes, key = { it.instanceId }) { h ->
                            HeroCard(h, onClick = {
                                update { s ->
                                    val slots = s.lineup.slots.toMutableList()
                                    for (i in slots.indices) if (slots[i] == h.instanceId) slots[i] = null
                                    slots[slotIndex] = h.instanceId
                                    s.copy(lineup = s.lineup.copy(slots = slots))
                                }
                                picking = null
                            })
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { picking = null }) { Text("Close", color = HBColors.BrandPink) }
            }
        )
    }

    if (selectedHero != null) {
        HeroDetailDialog(
            state = state,
            hero = selectedHero,
            onDismiss = { selectedId = null },
            onAction = { action ->
                update { s ->
                    when (action) {
                        HeroAction.LEVEL -> {
                            val n = Progression.levelUp(s, selectedHero.instanceId)
                            Quests.bump(n, "level-hero")
                        }
                        HeroAction.ASCEND -> Progression.ascend(s, selectedHero.instanceId)
                        HeroAction.GEAR -> Progression.upgradeGear(s, selectedHero.instanceId)
                        is HeroAction.UpgradeSkill -> Progression.upgradeSkill(s, selectedHero.instanceId, action.skillId)
                        is HeroAction.EquipStone -> {
                            val n = Progression.equipStone(s, selectedHero.instanceId, action.slot, action.kind)
                            Quests.bump(n, "stone-equip")
                        }
                    }
                }
            },
        )
    }
}

private sealed class HeroAction {
    object LEVEL : HeroAction()
    object ASCEND : HeroAction()
    object GEAR : HeroAction()
    data class UpgradeSkill(val skillId: String) : HeroAction()
    data class EquipStone(val slot: Int, val kind: StoneKind) : HeroAction()
}

@Composable
private fun HeroDetailDialog(
    state: GameState,
    hero: HeroInstance,
    onDismiss: () -> Unit,
    onAction: (HeroAction) -> Unit,
) {
    val t = Heroes.byId.getValue(hero.templateId)
    val faction = Factions.all.getValue(t.faction)
    val cls = Classes.all.getValue(t.heroClass)
    val stats = Stats.compute(hero)
    val asc = Progression.ascensionCost(hero.stars)
    val gc = Progression.gearCost(hero.gearTier)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HBColors.Bg1,
        titleContentColor = HBColors.Text,
        textContentColor = HBColors.TextDim,
        title = { Text("${t.name}, ${t.title}", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(t.portraitGradient[0]), Color(t.portraitGradient[1]))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(t.emoji, fontSize = 56.sp)
                    }
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${faction.name} · ${cls.name} · ${"★".repeat(hero.stars)}",
                            color = HBColors.TextDim,
                            fontSize = 12.sp,
                        )
                        Text("\"${t.bio}\"", color = HBColors.TextDim, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                        Spacer(Modifier.height(6.dp))
                        StatRow("Level", hero.level.toString())
                        StatRow("Power", "⚡ ${Stats.power(hero)}")
                        StatRow("Echo Stacks", hero.echoStacks.toString())
                        StatRow("Gear", "◆".repeat(hero.gearTier) + "◇".repeat(6 - hero.gearTier))
                    }
                }
                // Stats
                Card {
                    Column {
                        StatRow("Attack", stats.attack.toString())
                        StatRow("Health", stats.health.toString())
                        StatRow("Armor", stats.armor.toString())
                        StatRow("Speed", stats.speed.toString())
                        StatRow("Willpower", stats.willpower.toString())
                    }
                }
                // Skills (with leveling)
                Card {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SKILLS", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        t.skills.forEach { skill ->
                            val lv = Progression.skillLevel(hero, skill.id)
                            val cap = Progression.skillCap(hero)
                            val cost = Progression.skillUpCost(lv)
                            Column {
                                Text(
                                    "${if (skill.active) "⚡" else "✨"} ${skill.name}  Lv $lv/$cap",
                                    color = HBColors.Text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                                Text(skill.description, color = HBColors.TextDim, fontSize = 11.sp)
                                OutlinedPillButton(
                                    text = "Upgrade · 🧪 $cost",
                                    onClick = { onAction(HeroAction.UpgradeSkill(skill.id)) },
                                    enabled = Progression.canUpgradeSkill(state, hero, skill.id),
                                )
                            }
                        }
                    }
                }
                // Stones (new iteration)
                Card {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("EQUIPMENT STONES (NEW)", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Fragments: 💠 ${formatNum(state.currency.stoneFragments)}", color = HBColors.Fragment, fontSize = 11.sp)
                        for (slot in 0 until Progression.STONE_SLOTS) {
                            val equipped = hero.equippedStones[slot.toString()]
                            val cost = Progression.stoneUnlockCost(slot)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("Slot ${slot + 1}:", color = HBColors.TextDim, modifier = Modifier.padding(vertical = 6.dp))
                                StoneKind.values().forEach { kind ->
                                    val on = equipped == kind
                                    OutlinedPillButton(
                                        text = if (on) "✓ ${kind.name.lowercase()}" else kind.name.lowercase(),
                                        onClick = { onAction(HeroAction.EquipStone(slot, kind)) },
                                        enabled = on || Progression.canEquipStone(state, hero, slot),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                            if (equipped == null) {
                                Text("Unlock cost: 💠 $cost fragments", color = HBColors.TextMute, fontSize = 10.sp)
                            }
                        }
                    }
                }
                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradientButton(
                        text = "Level · 🪙 ${formatNum(Stats.goldToLevel(hero.level).toLong())}",
                        onClick = { onAction(HeroAction.LEVEL) },
                        enabled = Progression.canLevelUp(state, hero),
                        modifier = Modifier.weight(1f),
                    )
                    GradientButton(
                        text = "Ascend · 🔹${asc.shards} ✨${asc.spirit}",
                        onClick = { onAction(HeroAction.ASCEND) },
                        enabled = Progression.canAscend(state, hero),
                        isGold = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedPillButton(
                    text = "Gear · 🪙${formatNum(gc.gold)} 🔹${gc.shards}",
                    onClick = { onAction(HeroAction.GEAR) },
                    enabled = Progression.canUpgradeGear(state, hero),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = HBColors.BrandPink) }
        },
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = HBColors.TextDim, fontSize = 12.sp)
        Text(value, color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
