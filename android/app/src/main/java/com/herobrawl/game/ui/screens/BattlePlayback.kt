package com.herobrawl.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herobrawl.game.engine.BattleLog
import com.herobrawl.game.engine.BattleResult
import com.herobrawl.game.engine.BattleUnit
import com.herobrawl.game.ui.EnergyBar
import com.herobrawl.game.ui.GradientButton
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.HpBar
import com.herobrawl.game.ui.OutlinedPillButton
import kotlinx.coroutines.delay

@Composable
fun BattlePlayback(
    allies: List<BattleUnit>,
    enemies: List<BattleUnit>,
    result: BattleResult,
    onClose: () -> Unit,
) {
    val hp = remember { mutableStateMapOf<String, Int>() }
    val max = remember { mutableStateMapOf<String, Int>() }
    var idx by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (u in allies + enemies) {
            hp[u.uid] = u.hp
            max[u.uid] = u.maxHp
        }
    }

    LaunchedEffect(idx) {
        if (idx >= result.log.size) { done = true; return@LaunchedEffect }
        val entry = result.log[idx]
        delay(180L)
        when (entry.kind) {
            BattleLog.Kind.ATTACK -> entry.target?.let { hp[it] = ((hp[it] ?: 0) - entry.amount).coerceAtLeast(0) }
            BattleLog.Kind.HEAL -> entry.target?.let {
                val m = max[it] ?: 1
                hp[it] = ((hp[it] ?: 0) + entry.amount).coerceAtMost(m)
            }
            BattleLog.Kind.STATUS -> entry.actor?.let {
                if (entry.amount > 0) hp[it] = ((hp[it] ?: 0) - entry.amount).coerceAtLeast(0)
            }
            else -> Unit
        }
        idx++
    }

    val skip: () -> Unit = {
        for (u in allies + enemies) { hp[u.uid] = u.hp; max[u.uid] = u.maxHp }
        for (l in result.log) {
            when (l.kind) {
                BattleLog.Kind.ATTACK -> l.target?.let { hp[it] = ((hp[it] ?: 0) - l.amount).coerceAtLeast(0) }
                BattleLog.Kind.HEAL -> l.target?.let {
                    val m = max[it] ?: 1
                    hp[it] = ((hp[it] ?: 0) + l.amount).coerceAtMost(m)
                }
                BattleLog.Kind.STATUS -> l.actor?.let {
                    if (l.amount > 0) hp[it] = ((hp[it] ?: 0) - l.amount).coerceAtLeast(0)
                }
                else -> Unit
            }
        }
        idx = result.log.size
        done = true
    }

    AlertDialog(
        onDismissRequest = {},
        containerColor = HBColors.Bg0,
        titleContentColor = HBColors.Text,
        textContentColor = HBColors.TextDim,
        title = {
            Text(
                if (done) (if (result.winner == BattleUnit.Side.ALLY) "Victory!" else "Defeat") else "Battle",
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Battleground
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F1020), Color(0xFF090A16))
                            )
                        )
                        .border(1.dp, HBColors.Stroke, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SideField(units = allies, hp = hp, max = max, modifier = Modifier.weight(1f))
                    SideField(units = enemies, hp = hp, max = max, modifier = Modifier.weight(1f))
                }
                // Log
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, HBColors.Stroke, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    result.log.take(idx).forEach { l ->
                        val color = when (l.kind) {
                            BattleLog.Kind.SKILL -> HBColors.BrandGold
                            BattleLog.Kind.HEAL -> HBColors.Shard
                            BattleLog.Kind.DEATH -> Color(0xFFFF7A7A)
                            BattleLog.Kind.VICTORY -> HBColors.Shard
                            BattleLog.Kind.DEFEAT -> Color(0xFFFF7A7A)
                            else -> HBColors.TextDim
                        }
                        Text("· ${l.text}", color = color, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (done) {
                GradientButton(text = "Continue", onClick = onClose, modifier = Modifier.fillMaxWidth(0.4f))
            } else {
                OutlinedPillButton(text = "Skip ⏩", onClick = skip, modifier = Modifier.fillMaxWidth(0.4f))
            }
        },
    )
}

@Composable
private fun SideField(
    units: List<BattleUnit>,
    hp: Map<String, Int>,
    max: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Show 5 slots, grid-like 2x3
        val bySlot = units.associateBy { it.slot }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                UnitCell(bySlot[0], hp, max, Modifier.weight(1f))
                UnitCell(bySlot[1], hp, max, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                UnitCell(bySlot[2], hp, max, Modifier.weight(1f))
                UnitCell(bySlot[3], hp, max, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                UnitCell(bySlot[4], hp, max, Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UnitCell(
    unit: BattleUnit?,
    hp: Map<String, Int>,
    max: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    if (unit == null) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }
    val currentHp = hp[unit.uid] ?: unit.hp
    val currentMax = max[unit.uid] ?: unit.maxHp
    val dead = currentHp <= 0
    val color by animateColorAsState(
        targetValue = if (dead) Color.Gray.copy(alpha = 0.4f) else Color.White,
        label = "unit-tint",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(unit.gradientStart), Color(unit.gradientEnd))
                )
            )
            .border(1.dp, Color(unit.signatureColor).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(unit.name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(unit.classId.name.take(3), color = color.copy(alpha = 0.7f), fontSize = 9.sp)
        }
        Text(unit.emoji, fontSize = 28.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        HpBar(currentHp, currentMax)
        Text(
            "$currentHp / $currentMax",
            color = color.copy(alpha = 0.7f),
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.End),
        )
    }
}
