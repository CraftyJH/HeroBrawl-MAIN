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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.herobrawl.game.data.Cosmetics
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.AvatarId
import com.herobrawl.game.model.AvatarKind
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.PlayerAvatarPreview

@Composable
fun AvatarPickerScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Profile", color = HBColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)

        // Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerAvatarPreview(state, size = 96.dp)
            Spacer(Modifier.size(12.dp))
            Column {
                Text(state.playerName, color = HBColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Lv ${state.playerLevel}  •  VIP ${state.vip.level}", color = HBColors.TextDim, fontSize = 11.sp)
                Text("${state.heroes.size} heroes  •  ${state.arena.rating} arena", color = HBColors.TextMute, fontSize = 11.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TabBtn("🖼️  Avatar", tab == 0, Modifier.weight(1f)) { tab = 0 }
            TabBtn("🔷 Frame", tab == 1, Modifier.weight(1f)) { tab = 1 }
            TabBtn("🛡️ Hero Portrait", tab == 2, Modifier.weight(1f)) { tab = 2 }
        }

        when (tab) {
            0 -> AvatarGrid(state, update)
            1 -> FrameGrid(state, update)
            2 -> HeroPortraitGrid(state, update)
        }
    }
}

@Composable
private fun TabBtn(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) HBColors.Bg3 else HBColors.Bg2)
            .border(
                1.dp,
                if (active) HBColors.BrandPink else HBColors.Stroke,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (active) HBColors.Text else HBColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AvatarGrid(state: GameState, update: ((GameState) -> GameState) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(90.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((Cosmetics.defaultIcons.size + 2) / 3) * 120).dp),
    ) {
        items(Cosmetics.defaultIcons, key = { it.id }) { icon ->
            val unlocked = Cosmetics.isIconUnlocked(icon.id, state.cosmetics.unlockedAvatars, state.vip.level, state.arena.rating, state.heroes)
            val current = state.cosmetics.avatar.kind == AvatarKind.ICON && state.cosmetics.avatar.value == icon.id
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (current) HBColors.Bg3 else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (current) HBColors.BrandPink else HBColors.Stroke,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = unlocked) {
                        if (unlocked) update { s ->
                            s.copy(cosmetics = s.cosmetics.copy(avatar = AvatarId.icon(icon.id)))
                        }
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(icon.gradient.first), Color(icon.gradient.second)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (unlocked) icon.emoji else "🔒", fontSize = 26.sp)
                }
                Text(icon.name, color = HBColors.Text, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(icon.unlockHint, color = HBColors.TextMute, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FrameGrid(state: GameState, update: ((GameState) -> GameState) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((Cosmetics.frames.size + 2) / 3) * 130).dp),
    ) {
        items(Cosmetics.frames, key = { it.id }) { frame ->
            val unlocked = Cosmetics.isFrameUnlocked(frame.id, state.cosmetics.unlockedFrames, state.vip.level, state.arena.rating, state.heroes, state.campaign.chapter)
            val current = state.cosmetics.frame == frame.id
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (current) HBColors.Bg3 else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (current) HBColors.BrandPink else HBColors.Stroke,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = unlocked) {
                        if (unlocked) update { s -> s.copy(cosmetics = s.cosmetics.copy(frame = frame.id)) }
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(frame.stroke), Color(frame.accent)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(HBColors.Bg2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (unlocked) "⚔️" else "🔒", fontSize = 22.sp)
                    }
                }
                Text(frame.name, color = HBColors.Text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(frame.unlockHint, color = HBColors.TextMute, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HeroPortraitGrid(state: GameState, update: ((GameState) -> GameState) -> Unit) {
    val heroes = state.heroes.distinctBy { it.templateId }
    if (heroes.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Summon heroes to use their portraits as avatars.", color = HBColors.TextDim)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((heroes.size + 3) / 4) * 100).dp),
    ) {
        items(heroes, key = { it.instanceId }) { inst ->
            val t = Heroes.byId[inst.templateId] ?: return@items
            val current = state.cosmetics.avatar.kind == AvatarKind.HERO_PORTRAIT && state.cosmetics.avatar.value == t.id
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (current) HBColors.Bg3 else HBColors.Bg2)
                    .border(
                        1.dp,
                        if (current) HBColors.BrandPink else HBColors.Stroke,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        update { s -> s.copy(cosmetics = s.cosmetics.copy(avatar = AvatarId.hero(t.id))) }
                    }
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(t.portraitGradient[0]), Color(t.portraitGradient[1]))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(t.emoji, fontSize = 24.sp)
                }
                Text(t.name, color = HBColors.Text, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
