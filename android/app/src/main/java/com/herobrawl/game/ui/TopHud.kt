package com.herobrawl.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.herobrawl.game.data.Cosmetics
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.data.Vip
import com.herobrawl.game.engine.PlayerProgression
import com.herobrawl.game.model.AvatarKind
import com.herobrawl.game.model.GameState

@Composable
fun TopHud(
    state: GameState,
    onAvatar: () -> Unit,
    onGems: () -> Unit,
    onGold: () -> Unit,
    onMail: () -> Unit,
    unreadMail: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B1D2D), Color(0xFF12131F))
                )
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: avatar + frame + name + level/xp + VIP
        AvatarBadge(state = state, onClick = onAvatar)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.playerName,
                    color = HBColors.Text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(6.dp))
                LevelChip(level = state.playerLevel)
                Spacer(Modifier.width(6.dp))
                VipChip(level = state.vip.level)
            }
            Spacer(Modifier.height(4.dp))
            XpBar(
                xp = state.playerXp,
                needed = PlayerProgression.xpToNext(state.playerLevel)
            )
        }
        Spacer(Modifier.width(8.dp))
        // Mail + currencies
        MailButton(unread = unreadMail, onClick = onMail)
        Spacer(Modifier.width(6.dp))
        CurrencyBox(icon = "🪙", amount = state.currency.gold, tint = HBColors.Gold, onClick = onGold)
        Spacer(Modifier.width(6.dp))
        CurrencyBox(icon = "💎", amount = state.currency.gems, tint = HBColors.Gems, onClick = onGems)
    }
}

@Composable
private fun AvatarBadge(state: GameState, onClick: () -> Unit) {
    val frame = Cosmetics.framesById[state.cosmetics.frame] ?: Cosmetics.frames.first()
    val size = 48.dp

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
        modifier = Modifier.size(size + 8.dp).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        // outer frame
        Box(
            modifier = Modifier
                .size(size + 8.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(frame.stroke), Color(frame.accent)))
                ),
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
            Text(emoji, fontSize = 24.sp)
        }
    }
}

@Composable
private fun LevelChip(level: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFFD04A), Color(0xFFFF8A3D)))
            )
            .padding(horizontal = 8.dp, vertical = 1.dp)
    ) {
        Text("Lv $level", color = Color(0xFF2B1800), fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun VipChip(level: Int) {
    val color = when {
        level >= 10 -> Color(0xFFFFD04A)
        level >= 5 -> Color(0xFFB47DFF)
        level >= 1 -> Color(0xFF5EE3FF)
        else -> Color(0xFF6D7498)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, color, RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text("VIP $level", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun XpBar(xp: Int, needed: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((xp.toFloat() / needed.coerceAtLeast(1)).coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF5EE3FF), Color(0xFF7DF5C5)))
                )
        )
    }
}

@Composable
private fun CurrencyBox(icon: String, amount: Long, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(formatNum(amount), color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text("+", color = tint, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MailButton(unread: Int, onClick: () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, HBColors.Stroke, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Text("📬", fontSize = 18.sp)
        }
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4F5E))
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (unread > 9) "9+" else "$unread",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
