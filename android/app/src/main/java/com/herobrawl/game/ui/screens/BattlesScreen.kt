package com.herobrawl.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.Route

@Composable
fun BattlesScreen(state: GameState, go: (Route) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Battles",
            color = HBColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            HubTile(
                emoji = "🏯",
                title = "Campaign",
                subtitle = "Chapter ${state.campaign.chapter}-${state.campaign.stage}",
                modifier = Modifier.weight(1f),
                gradient = listOf(Color(0xFFFF4F9D), Color(0xFF6B61FF)),
                onClick = { go(Route.Campaign) },
            )
            HubTile(
                emoji = "⚔️",
                title = "Arena",
                subtitle = "Rating ${state.arena.rating}",
                modifier = Modifier.weight(1f),
                gradient = listOf(Color(0xFFB47DFF), Color(0xFF6B61FF)),
                onClick = { go(Route.Arena) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            HubTile(
                emoji = "🎉",
                title = "Events",
                subtitle = "${state.events.tokensEarned} tokens",
                modifier = Modifier.weight(1f),
                gradient = listOf(Color(0xFFFFD04A), Color(0xFFFF7AA3)),
                onClick = { go(Route.Events) },
            )
            HubTile(
                emoji = "📜",
                title = "Quests",
                subtitle = "Daily ${state.quests.progress.values.sum()} pts",
                modifier = Modifier.weight(1f),
                gradient = listOf(Color(0xFF5EE3FF), Color(0xFF2EE37F)),
                onClick = { go(Route.Quests) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            HubTile(
                emoji = "🏆",
                title = "Medals",
                subtitle = "${state.achievements.claimed.size}/${com.herobrawl.game.engine.Achievements.all.size}",
                modifier = Modifier.weight(1f),
                gradient = listOf(Color(0xFFFFB54A), Color(0xFFFF4F9D)),
                onClick = { go(Route.Achievements) },
            )
            // Placeholder: future Guild hub
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF252842), Color(0xFF1B1D2D)))
                    )
                    .border(1.dp, HBColors.Stroke, RoundedCornerShape(14.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Text("🛡️", fontSize = 32.sp)
                    Spacer(Modifier.size(4.dp))
                    Text("Guild", color = HBColors.TextMute, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("Coming soon", color = HBColors.TextMute, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun HubTile(
    emoji: String,
    title: String,
    subtitle: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(gradient.map { it.copy(alpha = 0.35f) }))
            .border(1.dp, gradient.first().copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Text(emoji, fontSize = 44.sp)
        Spacer(Modifier.size(4.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}
