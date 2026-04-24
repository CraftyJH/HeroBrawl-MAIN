package com.herobrawl.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.data.Cosmetics
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.AvatarKind
import com.herobrawl.game.model.GameState

@Composable
fun PlayerAvatarPreview(
    state: GameState,
    size: Dp = 64.dp,
) {
    val frame = Cosmetics.framesById[state.cosmetics.frame] ?: Cosmetics.frames.first()
    val (emoji, grad) = when (state.cosmetics.avatar.kind) {
        AvatarKind.HERO_PORTRAIT -> {
            val t = Heroes.byId[state.cosmetics.avatar.value]
            if (t != null) t.emoji to (t.portraitGradient[0] to t.portraitGradient[1])
            else "⚔️" to (0xFF2B1948L to 0xFFFF4F9DL)
        }
        AvatarKind.ICON -> {
            val icon = Cosmetics.iconsById[state.cosmetics.avatar.value]
                ?: Cosmetics.iconsById.getValue("default")
            icon.emoji to (icon.gradient.first to icon.gradient.second)
        }
    }
    Box(
        modifier = Modifier.size(size + 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size + 10.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(frame.stroke), Color(frame.accent)))
                )
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(grad.first), Color(grad.second)))
                )
                .border(2.dp, Color.Black.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = (size.value * 0.6f).sp)
        }
    }
}
