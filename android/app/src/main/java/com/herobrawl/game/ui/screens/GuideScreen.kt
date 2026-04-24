package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Factions
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.Card
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.OutlinedPillButton

@Composable
fun GuideScreen(state: GameState, reset: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("HeroBrawl Guide", color = HBColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Black)

        Card {
            Column {
                Text("CORE LOOP", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("1. Summon heroes with scrolls or prophet orbs.", color = HBColors.TextDim)
                Text("2. Assign 5 to your lineup (front 1–3, back 4–5).", color = HBColors.TextDim)
                Text("3. Clear campaign stages for gold, spirit, and XP.", color = HBColors.TextDim)
                Text("4. Claim idle every few hours (12h cap).", color = HBColors.TextDim)
                Text("5. Level + ascend; fight arena; progress events.", color = HBColors.TextDim)
            }
        }

        Card {
            Column {
                Text("WHY IT'S BETTER", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("• Transparent pity, live counter.", color = HBColors.TextDim)
                Text("• Echo stacks — no sacrifice gacha.", color = HBColors.TextDim)
                Text("• 12h idle cap (+50%).", color = HBColors.TextDim)
                Text("• No VIP paywall.", color = HBColors.TextDim)
                Text("• Seasonal events that never wipe your roster.", color = HBColors.TextDim)
                Text("• Clear faction wheel + Dawnfall Pact aura.", color = HBColors.TextDim)
                Text("• Equipment Stones & Skill Leveling in v0.2.", color = HBColors.TextDim)
            }
        }

        Card {
            Column {
                Text("FACTIONS", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Factions.all.values.forEach { f ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${f.name}: ${f.lore}", color = HBColors.TextDim, fontSize = 12.sp)
                    }
                    Text(
                        "Strong vs: ${f.strongVs.joinToString { Factions.all[it]!!.name }}",
                        color = HBColors.TextMute, fontSize = 11.sp,
                    )
                }
            }
        }

        Card {
            Column {
                Text("CLASSES", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Classes.all.values.forEach { c ->
                    Text("${c.name} · ${c.role}", color = HBColors.Text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(c.description, color = HBColors.TextDim, fontSize = 12.sp)
                }
            }
        }

        Card {
            Column {
                Text("SAVE", color = HBColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Your save lives in device-local DataStore. Started ${java.time.Instant.ofEpochMilli(state.createdAt)}.",
                    color = HBColors.TextMute, fontSize = 11.sp,
                )
                OutlinedPillButton(text = "Reset Save", onClick = { confirm = true })
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = HBColors.Bg1,
            title = { Text("Reset your save?", color = HBColors.Text, fontWeight = FontWeight.Black) },
            text = { Text("This can't be undone.", color = HBColors.TextDim) },
            confirmButton = {
                TextButton(onClick = { reset(); confirm = false }) {
                    Text("Reset", color = Color(0xFFFF9A9A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) {
                    Text("Cancel", color = HBColors.TextDim)
                }
            },
        )
    }
}
