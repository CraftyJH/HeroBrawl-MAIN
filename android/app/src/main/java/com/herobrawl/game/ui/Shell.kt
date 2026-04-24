package com.herobrawl.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.MailEngine
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.screens.AchievementsScreen
import com.herobrawl.game.ui.screens.ArenaScreen
import com.herobrawl.game.ui.screens.AvatarPickerScreen
import com.herobrawl.game.ui.screens.BattlesScreen
import com.herobrawl.game.ui.screens.CampaignScreen
import com.herobrawl.game.ui.screens.CastleScreen
import com.herobrawl.game.ui.screens.EventsScreen
import com.herobrawl.game.ui.screens.FirstRunGift
import com.herobrawl.game.ui.screens.GuideScreen
import com.herobrawl.game.ui.screens.InventoryScreen
import com.herobrawl.game.ui.screens.MailScreen
import com.herobrawl.game.ui.screens.QuestsScreen
import com.herobrawl.game.ui.screens.ShopScreen
import com.herobrawl.game.ui.screens.SummonScreen
import com.herobrawl.game.ui.screens.VipScreen

@Composable
fun HeroBrawlShell(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
    reset: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(BottomTab.CASTLE) }
    // Back-stack of pushed secondary routes (Mail/Profile/Campaign/etc.).
    var stack by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    fun push(route: Route) {
        stack = stack + routeKey(route)
    }
    fun pop(): Boolean {
        if (stack.isEmpty()) return false
        stack = stack.dropLast(1)
        return true
    }

    val unreadMail = MailEngine.unclaimedCount(state)

    // When tab changes, clear secondary stack so the user always sees the tab root.
    LaunchedEffect(tab) { stack = emptyList() }

    Scaffold(
        containerColor = HBColors.Bg0,
        topBar = {
            TopHud(
                state = state,
                onAvatar = { push(Route.AvatarPicker) },
                onGems = { tab = BottomTab.SHOP },
                onGold = { tab = BottomTab.SHOP },
                onMail = { push(Route.Mail) },
                unreadMail = unreadMail,
            )
        },
        bottomBar = {
            BottomNav(selected = tab, onSelect = {
                tab = it
                stack = emptyList()
            })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HBColors.Bg0),
        ) {
            val topRoute = stack.lastOrNull()
            if (topRoute != null) {
                SecondaryHeader(title = titleFor(topRoute), onBack = { pop() })
            }
            Box(modifier = Modifier.weight(1f)) {
                when (topRoute) {
                    null -> when (tab) {
                        BottomTab.CASTLE -> CastleScreen(state, update, notify, go = { route ->
                            when (route) {
                                Route.Summon -> tab = BottomTab.SUMMON
                                Route.Shop -> tab = BottomTab.SHOP
                                Route.Inventory -> tab = BottomTab.INVENTORY
                                Route.Castle -> tab = BottomTab.CASTLE
                                Route.Battles -> tab = BottomTab.BATTLES
                                else -> push(route)
                            }
                        })
                        BottomTab.INVENTORY -> InventoryScreen(state, update, notify)
                        BottomTab.SUMMON -> SummonScreen(state, update, notify)
                        BottomTab.SHOP -> ShopScreen(state, update, notify)
                        BottomTab.BATTLES -> BattlesScreen(state, go = { push(it) })
                    }
                    "mail" -> MailScreen(state, update, notify)
                    "avatar" -> AvatarPickerScreen(state, update, notify)
                    "campaign" -> CampaignScreen(state, update, notify)
                    "arena" -> ArenaScreen(state, update, notify)
                    "events" -> EventsScreen(state, update, notify)
                    "quests" -> QuestsScreen(state, update, notify)
                    "achievements" -> AchievementsScreen(state, update, notify)
                    "vip" -> VipScreen(state)
                    "guide" -> GuideScreen(state, reset = reset)
                }
            }
        }
        FirstRunGift(state, update, notify)
    }
}

@Composable
private fun SecondaryHeader(title: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HBColors.Bg1)
            .clickable { onBack() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(HBColors.Bg2),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", color = HBColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.size(8.dp))
        Text(title, color = HBColors.Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        Text("Back", color = HBColors.TextMute, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun routeKey(route: Route): String = when (route) {
    Route.Castle -> "castle"
    Route.Inventory -> "inventory"
    Route.Summon -> "summon"
    Route.Shop -> "shop"
    Route.Battles -> "battles"
    Route.Mail -> "mail"
    Route.AvatarPicker -> "avatar"
    Route.Campaign -> "campaign"
    Route.Arena -> "arena"
    Route.Events -> "events"
    Route.Quests -> "quests"
    Route.Achievements -> "achievements"
    Route.Vip -> "vip"
    Route.Guide -> "guide"
}

private fun titleFor(key: String): String = when (key) {
    "mail" -> "Mail"
    "avatar" -> "Profile"
    "campaign" -> "Campaign"
    "arena" -> "Arena"
    "events" -> "Events"
    "quests" -> "Quests"
    "achievements" -> "Medals"
    "vip" -> "VIP"
    "guide" -> "Guide"
    else -> key.replaceFirstChar { it.uppercase() }
}

// Helper for screens referencing formatted numbers
fun formatNum(n: Long): String = when {
    n >= 1_000_000_000 -> "%.2fB".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.2fM".format(n / 1_000_000.0)
    n >= 10_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}
