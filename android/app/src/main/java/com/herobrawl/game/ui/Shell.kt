package com.herobrawl.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.screens.AchievementsScreen
import com.herobrawl.game.ui.screens.ArenaScreen
import com.herobrawl.game.ui.screens.CampaignScreen
import com.herobrawl.game.ui.screens.EventsScreen
import com.herobrawl.game.ui.screens.FirstRunGift
import com.herobrawl.game.ui.screens.GuideScreen
import com.herobrawl.game.ui.screens.HomeScreen
import com.herobrawl.game.ui.screens.QuestsScreen
import com.herobrawl.game.ui.screens.RosterScreen
import com.herobrawl.game.ui.screens.SummonScreen

enum class Tab(val label: String, val icon: String) {
    HOME("Home", "🏰"),
    CAMPAIGN("Campaign", "🗺️"),
    SUMMON("Summon", "✨"),
    ROSTER("Heroes", "🛡️"),
    ARENA("Arena", "⚔️"),
    EVENTS("Events", "🎉"),
    QUESTS("Quests", "📜"),
    ACHIEVEMENTS("Medals", "🏆"),
    GUIDE("Guide", "📖"),
}

@Composable
fun HeroBrawlShell(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
    reset: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }

    Scaffold(
        containerColor = HBColors.Bg0,
        topBar = { TopBar(state) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HBColors.Bg0)
        ) {
            TabRow(tab = tab, onChange = { tab = it })
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    Tab.HOME -> HomeScreen(state, update, notify, go = { tab = it })
                    Tab.CAMPAIGN -> CampaignScreen(state, update, notify)
                    Tab.SUMMON -> SummonScreen(state, update, notify)
                    Tab.ROSTER -> RosterScreen(state, update, notify)
                    Tab.ARENA -> ArenaScreen(state, update, notify)
                    Tab.EVENTS -> EventsScreen(state, update, notify)
                    Tab.QUESTS -> QuestsScreen(state, update, notify)
                    Tab.ACHIEVEMENTS -> AchievementsScreen(state, update, notify)
                    Tab.GUIDE -> GuideScreen(state, reset = reset)
                }
            }
        }
        FirstRunGift(state, update, notify)
    }
}

@Composable
private fun TopBar(state: GameState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE616182C), Color(0xA60A0B14))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⚔️", fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "HeroBrawl",
                color = HBColors.BrandGold,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text("BETA", color = HBColors.TextMute, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(HBColors.Bg1)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            item { Pill("🪙", formatNum(state.currency.gold), HBColors.Gold) }
            item { Pill("💎", formatNum(state.currency.gems), HBColors.Gems) }
            item { Pill("📜", "${state.currency.heroicScrolls}", HBColors.Scroll) }
            item { Pill("🔮", "${state.currency.prophetOrbs}", HBColors.Orb) }
            item { Pill("✨", formatNum(state.currency.spirit), HBColors.Spirit) }
            item { Pill("🔹", formatNum(state.currency.shards), HBColors.Shard) }
            item { Pill("💠", formatNum(state.currency.stoneFragments), HBColors.Fragment) }
            item { Pill("🧪", formatNum(state.currency.dust), HBColors.Dust) }
        }
    }
}

@Composable
private fun TabRow(tab: Tab, onChange: (Tab) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0F1D)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(Tab.values().toList()) { t ->
            val active = tab == t
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .then(
                        if (active)
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(HBColors.BrandPink, HBColors.BrandIndigo)
                                )
                            )
                        else Modifier
                            .border(1.dp, HBColors.Stroke, RoundedCornerShape(999.dp))
                    )
                    .clickable { onChange(t) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(t.icon, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    t.label,
                    color = if (active) Color.White else HBColors.TextDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

fun formatNum(n: Long): String = when {
    n >= 1_000_000_000 -> "%.2fB".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.2fM".format(n / 1_000_000.0)
    n >= 10_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}
