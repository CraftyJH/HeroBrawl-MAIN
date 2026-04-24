package com.herobrawl.game.engine

import com.herobrawl.game.data.GemOffer
import com.herobrawl.game.data.GemPack
import com.herobrawl.game.data.PackDeal
import com.herobrawl.game.data.Shop
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.MailMessage
import com.herobrawl.game.model.MailReward
import java.util.UUID

object ShopEngine {
    fun buyGemPack(state: GameState, pack: GemPack, now: Long): GameState {
        val cur = state.currency
        val withGems = state.copy(
            currency = cur.copy(gems = cur.gems + pack.gems + pack.bonusGems),
            shop = state.shop.copy(
                purchases = state.shop.purchases + (pack.id to (state.shop.purchases[pack.id] ?: 0) + 1)
            ),
        )
        return VipEngine.addVipXp(withGems, pack.vipXp)
    }

    fun canBuyOffer(state: GameState, offer: GemOffer): Boolean {
        val bought = state.shop.purchases["${offer.id}:${state.shop.lastDaily}"] ?: 0
        if (bought >= offer.dailyLimit) return false
        return state.currency.gems >= offer.costGems
    }

    fun buyGemOffer(state: GameState, offer: GemOffer): GameState {
        if (!canBuyOffer(state, offer)) return state
        val today = state.shop.lastDaily.ifBlank {
            java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString()
        }
        val key = "${offer.id}:$today"
        var s = state.copy(
            currency = state.currency.copy(gems = state.currency.gems - offer.costGems),
            shop = state.shop.copy(
                purchases = state.shop.purchases + (key to (state.shop.purchases[key] ?: 0) + 1),
                lastDaily = today,
            ),
        )
        s = applyReward(s, offer.rewardKind, offer.rewardValue, offer.rewardAmount)
        return s
    }

    fun buyPackDeal(state: GameState, deal: PackDeal, now: Long): GameState {
        var s = state
        for (r in deal.rewards) {
            s = when {
                r.kind == "gems" -> s.copy(currency = s.currency.copy(gems = s.currency.gems + r.amount))
                r.kind == "heroicScrolls" -> s.copy(currency = s.currency.copy(heroicScrolls = s.currency.heroicScrolls + r.amount.toInt()))
                r.kind == "prophetOrbs" -> s.copy(currency = s.currency.copy(prophetOrbs = s.currency.prophetOrbs + r.amount))
                r.kind == "stoneFragments" -> s.copy(currency = s.currency.copy(stoneFragments = s.currency.stoneFragments + r.amount))
                r.kind == "dust" -> s.copy(currency = s.currency.copy(dust = s.currency.dust + r.amount))
                r.kind == "gold" -> s.copy(currency = s.currency.copy(gold = s.currency.gold + r.amount))
                r.kind == "eventTokens" -> Events.gainTokens(s, r.amount)
                r.kind == "mail_daily_gems" -> s // delivered via mail
                r.kind == "mail_chapter_gems" -> s
                r.kind.startsWith("item:") -> {
                    val kindName = r.kind.removePrefix("item:")
                    val id = com.herobrawl.game.data.Items.all
                        .firstOrNull { it.kind.name == kindName }?.id
                    if (id != null) Inventory.add(s, id, r.amount.toInt()) else s
                }
                else -> s
            }
        }
        s = s.copy(
            shop = s.shop.copy(
                purchases = s.shop.purchases + (deal.id to (s.shop.purchases[deal.id] ?: 0) + 1)
            ),
        )
        s = VipEngine.addVipXp(s, deal.vipXp)
        if (deal.isMonthlyCard) {
            s = VipEngine.grantMonthlyCard(s, now)
            s = MailEngine.send(
                s,
                MailMessage(
                    id = UUID.randomUUID().toString(),
                    sentAt = now,
                    sender = "Monthly Card",
                    subject = "Daily dispensed",
                    body = "First daily 💎 payout from your monthly card. More lands tomorrow.",
                    rewards = listOf(MailReward(kind = "gems", amount = 100)),
                )
            )
        }
        return s
    }

    private fun applyReward(state: GameState, kind: String, value: String, amount: Long): GameState = when (kind) {
        "currency" -> when (value) {
            "gold" -> state.copy(currency = state.currency.copy(gold = state.currency.gold + amount))
            "gems" -> state.copy(currency = state.currency.copy(gems = state.currency.gems + amount))
            "heroicScrolls" -> state.copy(currency = state.currency.copy(heroicScrolls = state.currency.heroicScrolls + amount.toInt()))
            "prophetOrbs" -> state.copy(currency = state.currency.copy(prophetOrbs = state.currency.prophetOrbs + amount))
            "stoneFragments" -> state.copy(currency = state.currency.copy(stoneFragments = state.currency.stoneFragments + amount))
            "dust" -> state.copy(currency = state.currency.copy(dust = state.currency.dust + amount))
            "arenaTicket" -> state.copy(arena = state.arena.copy(ticketsUsed = (state.arena.ticketsUsed - amount.toInt()).coerceAtLeast(0)))
            else -> state
        }
        "item" -> {
            val id = com.herobrawl.game.data.Items.all.firstOrNull { it.kind.name == value }?.id
            if (id != null) Inventory.add(state, id, amount.toInt()) else state
        }
        else -> state
    }
}
