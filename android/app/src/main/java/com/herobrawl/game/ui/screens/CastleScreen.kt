package com.herobrawl.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Buildings
import com.herobrawl.game.engine.Events
import com.herobrawl.game.engine.Idle
import com.herobrawl.game.engine.Quests
import com.herobrawl.game.engine.VipEngine
import com.herobrawl.game.model.BuildingId
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.Route
import com.herobrawl.game.ui.formatNum
import kotlinx.coroutines.delay

/**
 * Visually-driven home scene. A parallax-ish castle courtyard painted in the
 * HeroBrawl palette with icon-only building tiles arranged like Idle Heroes
 * / TapTap Heroes. No big walls of text — everything is tap-to-explore.
 */
@Composable
fun CastleScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
    go: (Route) -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val reward = Idle.compute(state, now)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0B14), Color(0xFF1B1D2D), Color(0xFF2B1948))
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Hero banner: parallax sky with floating castle
        CastleHero(reward = reward, cap = state.idle.capHours)

        // Idle claim row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .offset(y = (-24).dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            IdleClaimPanel(
                gold = reward.gold,
                spirit = reward.spirit,
                shards = reward.shards,
                capped = reward.capped,
                fraction = (reward.hours / state.idle.capHours).toFloat().coerceIn(0f, 1f),
                onClaim = {
                    update { s ->
                        val next = Idle.claim(s, System.currentTimeMillis())
                        Quests.bump(next, "idle-claim")
                    }
                    notify("Claimed idle rewards.", "reward")
                },
                vipBoost = (VipEngine.idleMultiplier(state) - 1) * 100,
            )
        }

        Spacer(Modifier.height(4.dp))

        // Building grid — 3 columns, icon + mini label
        val buildings = Buildings.all.filter { it.id != BuildingId.CASTLE }
        val eventTokens = state.events.tokensEarned
        val unreadAchievements = com.herobrawl.game.engine.Achievements.all
            .count { it.id in state.achievements.unlocked && it.id !in state.achievements.claimed }

        Text(
            "Explore the Keep",
            color = HBColors.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        // Row 1
        BuildingGridRow(
            entries = buildings.slice(0..2),
            badges = mapOf(
                BuildingId.CAMPAIGN_GATE to "${state.campaign.chapter}-${state.campaign.stage}",
                BuildingId.ARENA to state.arena.rating.toString(),
                BuildingId.SUMMONING_CIRCLE to "${state.currency.heroicScrolls}📜",
            ),
            go = go,
        )
        BuildingGridRow(
            entries = buildings.slice(3..5),
            badges = mapOf(
                BuildingId.MARKET to if (state.vip.monthlyCardExpiresAt > now) "🗓️" else null,
                BuildingId.MAILBOX to com.herobrawl.game.engine.MailEngine.unclaimedCount(state).takeIf { it > 0 }?.toString(),
            ).filterValues { it != null }.mapValues { it.value!! },
            go = go,
        )
        BuildingGridRow(
            entries = buildings.slice(6..8),
            badges = mapOf(
                BuildingId.EVENT_PAVILION to if (eventTokens > 0) formatNum(eventTokens) else null,
            ).filterValues { it != null }.mapValues { it.value!! },
            go = go,
        )

        Spacer(Modifier.height(10.dp))

        // Quick-access chips (mini icons: Quests / Achievements / VIP / Guide)
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Text("Quick Links", color = HBColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                QuickChip("📜", "Quests", modifier = Modifier.weight(1f)) { go(Route.Quests) }
                QuickChip("🏆", "Medals", modifier = Modifier.weight(1f), badge = unreadAchievements.takeIf { it > 0 }?.toString()) { go(Route.Achievements) }
                QuickChip("👑", "VIP", modifier = Modifier.weight(1f)) { go(Route.Vip) }
                QuickChip("📖", "Guide", modifier = Modifier.weight(1f)) { go(Route.Guide) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Recent activity (minimal, collapsible-feel)
        if (state.messages.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 14.dp)) {
                Text("News", color = HBColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                state.messages.take(3).forEach {
                    val color = when (it.kind) {
                        "reward" -> HBColors.Gold
                        "success" -> HBColors.Shard
                        "warn" -> Color(0xFFFF9A9A)
                        else -> HBColors.TextDim
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("•", color = color, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(it.text, color = color, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CastleHero(reward: com.herobrawl.game.engine.IdleReward, cap: Int) {
    val rt = rememberInfiniteTransition(label = "sky")
    val cloudOffset by rt.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud-offset"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B1948), Color(0xFF6B61FF), Color(0xFF1B1D2D))
                )
            )
    ) {
        // Clouds (three floating emoji at slightly different speeds)
        Cloud(cloudOffset, speedMul = 1.0f, yOffset = 24.dp, size = 36.sp)
        Cloud(cloudOffset, speedMul = 0.65f, yOffset = 60.dp, size = 26.sp, alpha = 0.7f)
        Cloud(cloudOffset, speedMul = 1.4f, yOffset = 40.dp, size = 44.sp, alpha = 0.5f)

        // Castle — scaled emoji silhouette
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🏰", fontSize = 96.sp)
        }

        // Ground line + banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "HEROBRAWL KEEP",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScopeCloud() {}

@Composable
private fun androidx.compose.foundation.layout.BoxWithConstraintsScope.Cloud(
    t: Float,
    speedMul: Float,
    yOffset: Dp,
    size: androidx.compose.ui.unit.TextUnit,
    alpha: Float = 0.8f,
) {
    val w = maxWidth
    val xDp = (-40).dp + (w + 80.dp) * ((t * speedMul) % 1f)
    Box(
        modifier = Modifier
            .offset(x = xDp, y = yOffset)
    ) {
        Text("☁️", fontSize = size, color = Color.White.copy(alpha = alpha))
    }
}

@Composable
private fun IdleClaimPanel(
    gold: Long,
    spirit: Long,
    shards: Long,
    capped: Boolean,
    fraction: Float,
    onClaim: () -> Unit,
    vipBoost: Double,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF16182C), Color(0xFF2B1948))
                )
            )
            .border(1.dp, if (capped) HBColors.Gold else HBColors.Stroke, RoundedCornerShape(14.dp))
            .clickable { onClaim() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left gift icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFD04A), Color(0xFFFF4F9D)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (capped) "🎁" else "💰", fontSize = 28.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Idle Rewards", color = HBColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CurrencyLine("🪙", formatNum(gold), HBColors.Gold)
                CurrencyLine("✨", formatNum(spirit), HBColors.Spirit)
                CurrencyLine("🔹", formatNum(shards), HBColors.Shard)
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(listOf(HBColors.BrandGold, HBColors.BrandPink))
                        )
                )
            }
            if (vipBoost > 0) {
                Text(
                    "VIP +${"%.0f".format(vipBoost)}% idle",
                    color = HBColors.Gems,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFF4F9D), Color(0xFF6B61FF)))
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (capped) "CLAIM!" else "CLAIM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CurrencyLine(icon: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 10.sp)
        Spacer(Modifier.width(2.dp))
        Text(value, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BuildingGridRow(
    entries: List<com.herobrawl.game.data.BuildingDef>,
    badges: Map<BuildingId, String>,
    go: (Route) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (b in entries) {
            BuildingTile(
                def = b,
                badge = badges[b.id],
                onClick = {
                    when (b.route) {
                        "home" -> go(Route.Castle)
                        "campaign" -> go(Route.Campaign)
                        "arena" -> go(Route.Arena)
                        "summon" -> go(Route.Summon)
                        "roster" -> go(Route.Inventory)
                        "shop" -> go(Route.Shop)
                        "mail" -> go(Route.Mail)
                        "events" -> go(Route.Events)
                        else -> go(Route.Castle)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BuildingTile(
    def: com.herobrawl.game.data.BuildingDef,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(def.accent).copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.45f),
                        )
                    )
                )
                .border(1.dp, Color(def.accent).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(def.emoji, fontSize = 38.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                def.tag,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFF4F5E))
                    .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    badge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun QuickChip(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(HBColors.Bg2)
                .border(1.dp, HBColors.Stroke, RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(label, color = HBColors.TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFF4F5E))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text(badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
