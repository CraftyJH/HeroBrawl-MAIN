package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.Gacha
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors

@Composable
fun FirstRunGift(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    if (state.firstRunClaimed) return
    // If the player somehow already has heroes (e.g. migration), mark the first
    // run as satisfied on the next frame — never mutate state during composition.
    if (state.heroes.isNotEmpty()) {
        LaunchedEffect(Unit) {
            update { it.copy(firstRunClaimed = true) }
        }
        return
    }

    AlertDialog(
        onDismissRequest = {},
        containerColor = HBColors.Bg1,
        title = { Text("Welcome to HeroBrawl!", color = HBColors.Text, fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text(
                    "Your starter pack — 3 heroes (including a guaranteed 5★), scrolls, gems, and event tokens.",
                    color = HBColors.TextDim, fontSize = 13.sp,
                )
                Row(Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tile("3 HEROES", "🛡️ Guaranteed 5★", Modifier.weight(1f))
                    Tile("+15 📜", "", HBColors.Scroll, Modifier.weight(1f))
                    Tile("+500 💎", "", HBColors.Gems, Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            GradientButton(
                text = "Claim & Play",
                onClick = {
                    update { s ->
                        val seeded = s.copy(
                            gacha = s.gacha.copy(heroicPulls = 59, pityCount = 59, sinceEpic = 9)
                        )
                        val p1 = Gacha.pull(seeded, 1)
                        val p2 = Gacha.pull(p1.state, 1)
                        val p3 = Gacha.pull(p2.state, 1)
                        var final = p3.state.copy(
                            currency = p3.state.currency.copy(
                                heroicScrolls = p3.state.currency.heroicScrolls + 15,
                                gems = p3.state.currency.gems + 500,
                                prophetOrbs = p3.state.currency.prophetOrbs + 20,
                                stoneFragments = p3.state.currency.stoneFragments + 200,
                                dust = p3.state.currency.dust + 100,
                            ),
                            lineup = p3.state.lineup.copy(
                                slots = listOf(
                                    p3.state.heroes.getOrNull(0)?.instanceId,
                                    p3.state.heroes.getOrNull(1)?.instanceId,
                                    p3.state.heroes.getOrNull(2)?.instanceId,
                                    null, null,
                                )
                            ),
                            gacha = p3.state.gacha.copy(pityCount = 0, sinceEpic = 0),
                            firstRunClaimed = true,
                        )
                        // Seed a welcome gift box + chest in inventory so the Bag tab has items.
                        final = com.herobrawl.game.engine.Inventory.add(final, "gift_box", 1)
                        final = com.herobrawl.game.engine.Inventory.add(final, "chest_rare", 1)
                        final = com.herobrawl.game.engine.Inventory.add(final, "xp_small", 3)
                        // Welcome mail
                        final = com.herobrawl.game.engine.MailEngine.send(
                            final,
                            com.herobrawl.game.model.MailMessage(
                                id = java.util.UUID.randomUUID().toString(),
                                sentAt = System.currentTimeMillis(),
                                sender = "HeroBrawl Team",
                                subject = "Welcome, Champion!",
                                body = "Thanks for playing HeroBrawl. Here's a little something to get you started.",
                                rewards = listOf(
                                    com.herobrawl.game.model.MailReward("gems", 100),
                                    com.herobrawl.game.model.MailReward("heroicScrolls", 2),
                                ),
                            )
                        )
                        notify("Starter pack opened!", "reward")
                        final
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        },
    )
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, color = HBColors.TextMute, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Tile(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (value.isNotEmpty()) Text(value, color = HBColors.TextDim, fontSize = 11.sp)
        }
    }
}
