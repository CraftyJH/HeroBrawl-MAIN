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
import com.herobrawl.game.data.Shop
import com.herobrawl.game.engine.ShopEngine
import com.herobrawl.game.engine.VipEngine
import com.herobrawl.game.model.GameState
import com.herobrawl.game.ui.HBColors
import com.herobrawl.game.ui.formatNum

@Composable
fun ShopScreen(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("🎁 Packs", "💎 Gems", "🛍️ Spend Gems")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        // Monthly-card banner (if active, prominent)
        val now = System.currentTimeMillis()
        val monthly = VipEngine.monthlyCardActive(state, now)
        if (monthly) {
            MonthlyCardBanner(state.vip.monthlyCardExpiresAt - now)
            Spacer(Modifier.height(10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { idx, label ->
                val active = idx == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) HBColors.Bg3 else HBColors.Bg2)
                        .border(
                            1.dp,
                            if (active) HBColors.BrandPink else HBColors.Stroke,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { tab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (active) HBColors.Text else HBColors.TextDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> PacksTab(state, update, notify)
            1 -> GemsTab(state, update, notify)
            else -> SpendGemsTab(state, update, notify)
        }
    }
}

@Composable
private fun MonthlyCardBanner(timeLeftMs: Long) {
    val days = timeLeftMs / (24 * 3600 * 1000)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFFB54A), Color(0xFFFF4F9D)))
            )
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🗓️", fontSize = 32.sp)
            Spacer(Modifier.size(8.dp))
            Column {
                Text("Monthly Card Active", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("$days days left · 💎 100/day delivered via Mail", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PacksTab(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Shop.packDeals.forEach { deal ->
            PackDealCard(deal = deal, onBuy = {
                update { ShopEngine.buyPackDeal(it, deal, System.currentTimeMillis()) }
                notify("Purchased ${deal.title}!", "reward")
            })
        }
    }
}

@Composable
private fun PackDealCard(deal: com.herobrawl.game.data.PackDeal, onBuy: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF16182C), Color(0xFF2B1948)))
            )
            .border(1.dp, HBColors.StrokeBright, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFD04A), Color(0xFFFF4F9D)))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(deal.banner, fontSize = 36.sp)
            }
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(deal.title, color = HBColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(deal.description, color = HBColors.TextDim, fontSize = 11.sp, maxLines = 2)
                Spacer(Modifier.size(4.dp))
                // Reward mini-chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    deal.rewards.take(3).forEach {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(it.label, color = HBColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                PriceButton(price = deal.price, onClick = onBuy)
            }
        }
    }
}

@Composable
private fun GemsTab(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
        modifier = Modifier.height((((Shop.gemPacks.size + 1) / 2) * 170).dp),
    ) {
        items(Shop.gemPacks, key = { it.id }) { pack ->
            GemPackCard(pack = pack, onBuy = {
                update { ShopEngine.buyGemPack(it, pack, System.currentTimeMillis()) }
                notify("Received ${pack.gems + pack.bonusGems} 💎!", "reward")
            })
        }
    }
}

@Composable
private fun GemPackCard(pack: com.herobrawl.game.data.GemPack, onBuy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF252842), Color(0xFF12131F))
                )
            )
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (pack.highlight != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFD04A))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(pack.highlight.uppercase(), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.size(4.dp))
        }
        Text(pack.emoji, fontSize = 42.sp)
        Text(pack.title, color = HBColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Row {
            Text("💎 ${pack.gems}", color = HBColors.Gems, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (pack.bonusGems > 0) {
                Spacer(Modifier.size(4.dp))
                Text(
                    "+${pack.bonusGems}",
                    color = HBColors.Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.size(6.dp))
        PriceButton(price = pack.price, onClick = onBuy)
    }
}

@Composable
private fun SpendGemsTab(
    state: GameState,
    update: ((GameState) -> GameState) -> Unit,
    notify: (String, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((Shop.gemOffers.size + 1) / 2) * 150).dp),
    ) {
        items(Shop.gemOffers, key = { it.id }) { offer ->
            GemOfferCard(state, offer, onBuy = {
                update { ShopEngine.buyGemOffer(it, offer) }
                notify("Purchased ${offer.title}.", "success")
            })
        }
    }
}

@Composable
private fun GemOfferCard(state: GameState, offer: com.herobrawl.game.data.GemOffer, onBuy: () -> Unit) {
    val enabled = ShopEngine.canBuyOffer(state, offer)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HBColors.Bg2)
            .border(1.dp, HBColors.Stroke, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(offer.emoji, fontSize = 36.sp)
        Text(offer.title, color = HBColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(offer.description, color = HBColors.TextDim, fontSize = 10.sp, maxLines = 1)
        Spacer(Modifier.size(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (enabled)
                        Brush.horizontalGradient(listOf(Color(0xFF5EE3FF), Color(0xFF6B61FF)))
                    else Brush.horizontalGradient(listOf(HBColors.Bg3, HBColors.Bg3))
                )
                .clickable(enabled = enabled) { onBuy() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                "💎 ${offer.costGems}",
                color = if (enabled) Color.White else HBColors.TextMute,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun PriceButton(price: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF2EE37F), Color(0xFF1FAE60)))
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(price, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}
