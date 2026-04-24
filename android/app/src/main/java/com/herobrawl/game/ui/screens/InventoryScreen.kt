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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Factions
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.data.Items
import com.herobrawl.game.engine.Inventory
import com.herobrawl.game.engine.Progression
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.engine.Stats
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.StoneKind
import com.herobrawl.game.ui.ChipRow
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.HeroCard
import com.herobrawl.game.ui.LineupSlotPlaceholder
import com.herobrawl.game.ui.OutlinedPillButton

@Composable
fun InventoryScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("🛡️ Heroes", "📦 Items", "💎 Resources")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Big icon tabs
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { idx, label ->
                val active = idx == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) HBColors.Bg3 else HBColors.Bg2)
                        .border(
                            1.dp,
                            if (active) HBColors.BrandPink else HBColors.Stroke,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { tab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (active) HBColors.Text else HBColors.TextDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> HeroesTab(state, update, notify)
            1 -> ItemsTab(state, update, notify)
            else -> ResourcesTab(state)
        }
    }
}

@Composable
private fun HeroesTab(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf<Int?>(null) }
    var factionFilter by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf("Power") }

    val filtered = state.heroes.filter { h ->
        val t = Heroes.byId[h.templateId] ?: return@filter false
        (factionFilter == "All" || Factions.all[t.faction]?.name == factionFilter)
    }.let {
        when (sort) {
            "Power" -> it.sortedByDescending { h -> Stats.power(h) }
            "Level" -> it.sortedByDescending { h -> h.level }
            "Stars" -> it.sortedByDescending { h -> h.stars }
            else -> it.sortedBy { Heroes.byId[it.templateId]?.name ?: "" }
        }
    }

    // Lineup strip
    Text("Lineup", color = HBColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        state.lineup.slots.forEachIndexed { idx, id ->
            val h = id?.let { state.heroes.firstOrNull { he -> he.instanceId == it } }
            if (h != null) {
                Box(modifier = Modifier.weight(1f).aspectRatio(0.75f).clickable { picking = idx }) {
                    HeroCard(h, compact = true)
                }
            } else {
                LineupSlotPlaceholder(idx, { picking = idx }, modifier = Modifier.weight(1f).aspectRatio(0.75f))
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // Filters — compact
    val factionNames = listOf("All") + Factions.all.values.map { it.name }
    ChipRow(factionNames, factionFilter, { factionFilter = it })
    Spacer(Modifier.height(4.dp))
    ChipRow(listOf("Power", "Level", "Stars", "Name"), sort, { sort = it })

    Spacer(Modifier.height(10.dp))

    if (filtered.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🔍", fontSize = 48.sp)
            Text("No heroes match.", color = HBColors.TextDim)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
            modifier = Modifier.height((((filtered.size + 2) / 3) * 180).dp),
        ) {
            items(filtered, key = { it.instanceId }) { h ->
                HeroCard(h, onClick = { selectedId = h.instanceId })
            }
        }
    }

    if (picking != null) {
        val slotIndex = picking!!
        AlertDialog(
            onDismissRequest = { picking = null },
            containerColor = HBColors.Bg1,
            title = { Text("Slot ${slotIndex + 1}", color = HBColors.Text) },
            text = {
                Column {
                    TextButton(onClick = {
                        update { s ->
                            val slots = s.lineup.slots.toMutableList(); slots[slotIndex] = null
                            s.copy(lineup = s.lineup.copy(slots = slots))
                        }
                        picking = null
                    }) { Text("Clear slot", color = HBColors.TextDim) }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 110.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(400.dp),
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

    val selectedHero = selectedId?.let { id -> state.heroes.firstOrNull { it.instanceId == id } }
    if (selectedHero != null) {
        HeroDetailSheet(
            state = state,
            hero = selectedHero,
            onDismiss = { selectedId = null },
            onLevel = { update { Quests.bump(Progression.levelUp(it, selectedHero.instanceId), "level-hero") } },
            onAscend = { update { Progression.ascend(it, selectedHero.instanceId) } },
            onGear = { update { Progression.upgradeGear(it, selectedHero.instanceId) } },
            onUpgradeSkill = { skillId ->
                update { Progression.upgradeSkill(it, selectedHero.instanceId, skillId) }
            },
            onEquipStone = { slot, kind ->
                update { Quests.bump(Progression.equipStone(it, selectedHero.instanceId, slot, kind), "stone-equip") }
            },
        )
    }
}

@Composable
private fun ItemsTab(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    val owned = Items.all
        .mapNotNull { def ->
            val ownedCount = state.inventory.items[def.id] ?: 0
            if (ownedCount > 0) def to ownedCount else null
        }

    if (owned.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("📦", fontSize = 48.sp)
            Text("No items yet.", color = HBColors.TextDim)
            Text("Win battles, buy packs, or open chests.", color = HBColors.TextMute, fontSize = 11.sp)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((owned.size + 1) / 2) * 170).dp),
    ) {
        items(owned, key = { it.first.id }) { (def, count) ->
            ItemTile(def = def, count = count, onUse = {
                if (def.usable) {
                    update { Inventory.use(it, def.id) }
                    notify("Used ${def.name}.", "success")
                } else {
                    notify("Hold onto this — used automatically.", "info")
                }
            })
        }
    }
}

@Composable
private fun ItemTile(
    def: com.herobrawl.game.data.ItemDef,
    count: Int,
    onUse: () -> Unit,
) {
    val rarity = when (def.rarity) {
        5 -> HBColors.RarityLegendary
        4 -> HBColors.RarityEpic
        3 -> HBColors.Gems
        else -> HBColors.TextDim
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HBColors.Bg2)
            .border(1.dp, rarity.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Text(def.emoji, fontSize = 44.sp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 5.dp),
            ) {
                Text("x$count", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(def.name, color = rarity, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
            def.description,
            color = HBColors.TextDim,
            fontSize = 9.sp,
            maxLines = 2,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedPillButton(
            text = if (def.usable) "Use" else "Info",
            onClick = onUse,
        )
    }
}

@Composable
private fun ResourcesTab(state: GameState) {
    val c = state.currency
    val rows = listOf(
        Triple("🪙", "Gold", c.gold),
        Triple("💎", "Gems", c.gems),
        Triple("📜", "Heroic Scrolls", c.heroicScrolls.toLong()),
        Triple("📃", "Basic Scrolls", c.basicScrolls.toLong()),
        Triple("🔮", "Prophet Orbs", c.prophetOrbs),
        Triple("✨", "Spirit", c.spirit),
        Triple("🔹", "Shards", c.shards),
        Triple("💠", "Stone Fragments", c.stoneFragments),
        Triple("🧪", "Skill Dust", c.dust),
        Triple("🎟️", "Event Tokens", c.eventTokens),
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((rows.size + 1) / 2) * 90).dp),
    ) {
        items(rows, key = { it.second }) { (emoji, label, amount) ->
            ResourceTile(emoji, label, amount)
        }
    }
}

@Composable
private fun ResourceTile(emoji: String, label: String, amount: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HBColors.Bg2)
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.padding(horizontal = 4.dp))
        Column {
            Text(label, color = HBColors.TextDim, fontSize = 10.sp)
            Text(
                com.herobrawl.game.ui.formatNum(amount),
                color = HBColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

/**
 * Full hero detail sheet (slightly leaner than the old RosterScreen version).
 */
@Composable
private fun HeroDetailSheet(
    state: GameState,
    hero: HeroInstance,
    onDismiss: () -> Unit,
    onLevel: () -> Unit,
    onAscend: () -> Unit,
    onGear: () -> Unit,
    onUpgradeSkill: (String) -> Unit,
    onEquipStone: (Int, StoneKind) -> Unit,
) {
    val t = Heroes.byId.getValue(hero.templateId)
    val faction = Factions.all.getValue(t.faction)
    val cls = Classes.all.getValue(t.heroClass)
    val stats = Stats.compute(hero)
    val asc = Progression.ascensionCost(hero.stars)
    val gc = Progression.gearCost(hero.gearTier)
    val lvCost = Stats.goldToLevel(hero.level).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HBColors.Bg1,
        title = { Text("${t.name}", color = HBColors.Text, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Portrait row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color(t.portraitGradient[0]), Color(t.portraitGradient[1]))
                                )
                            )
                            .padding(18.dp)
                    ) { Text(t.emoji, fontSize = 52.sp) }
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${faction.name} · ${cls.name}", color = HBColors.TextDim, fontSize = 11.sp)
                        Text("★".repeat(hero.stars), color = HBColors.RarityLegendary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("⚡ ${Stats.power(hero)}", color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Lv ${hero.level}  •  Echo ${hero.echoStacks}  •  Gear T${hero.gearTier}", color = HBColors.TextMute, fontSize = 11.sp)
                    }
                }
                // Actions row (icon + number)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradientButton(
                        text = "Lv+  🪙${com.herobrawl.game.ui.formatNum(lvCost)}",
                        onClick = onLevel,
                        enabled = Progression.canLevelUp(state, hero),
                        modifier = Modifier.weight(1f),
                    )
                    GradientButton(
                        text = "★+  🔹${asc.shards}",
                        onClick = onAscend,
                        enabled = Progression.canAscend(state, hero),
                        isGold = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedPillButton(
                        text = "Gear  🪙${com.herobrawl.game.ui.formatNum(gc.gold)}",
                        onClick = onGear,
                        enabled = Progression.canUpgradeGear(state, hero),
                        modifier = Modifier.weight(1f),
                    )
                }
                // Stats (compact, icon-coded)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StatCell("⚔️", stats.attack)
                    StatCell("❤️", stats.health)
                    StatCell("🛡️", stats.armor)
                    StatCell("⚡", stats.speed)
                }
                // Skills
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    t.skills.forEach { sk ->
                        val lv = Progression.skillLevel(hero, sk.id)
                        val cap = Progression.skillCap(hero)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HBColors.Bg2)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(if (sk.active) "⚡" else "✨", fontSize = 18.sp)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${sk.name}  Lv $lv/$cap", color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(sk.description, color = HBColors.TextDim, fontSize = 10.sp, maxLines = 2)
                            }
                            OutlinedPillButton(
                                text = "🧪${Progression.skillUpCost(lv)}",
                                onClick = { onUpgradeSkill(sk.id) },
                                enabled = Progression.canUpgradeSkill(state, hero, sk.id),
                            )
                        }
                    }
                }
                // Stones — compact grid
                Text("Equipment Stones", color = HBColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                for (slot in 0 until Progression.STONE_SLOTS) {
                    val equipped = hero.equippedStones[slot.toString()]
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("#${slot + 1}", color = HBColors.TextDim, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                        for (kind in StoneKind.values()) {
                            val on = equipped == kind
                            OutlinedPillButton(
                                text = if (on) "✓${kind.name.take(3)}" else stoneEmoji(kind),
                                onClick = { onEquipStone(slot, kind) },
                                enabled = on || Progression.canEquipStone(state, hero, slot),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = HBColors.BrandPink) }
        },
    )
}

private fun stoneEmoji(k: StoneKind) = when (k) {
    StoneKind.ATTACK -> "⚔️"
    StoneKind.HEALTH -> "❤️"
    StoneKind.SPEED -> "💨"
    StoneKind.ENERGY -> "⚡"
    StoneKind.CRIT -> "💥"
}

@Composable
private fun StatCell(icon: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HBColors.Bg2)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 16.sp)
        Text(com.herobrawl.game.ui.formatNum(value.toLong()), color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
