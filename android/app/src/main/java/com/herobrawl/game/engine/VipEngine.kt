package com.herobrawl.game.engine

import com.herobrawl.game.data.Vip
import com.herobrawl.game.model.GameState

object VipEngine {
    /** Add VIP XP (from real-money packs). Automatically levels up. */
    fun addVipXp(state: GameState, xp: Long): GameState {
        val newXp = state.currency.vipXp + xp
        val newLevel = Vip.levelForXp(newXp)
        return state.copy(
            currency = state.currency.copy(vipXp = newXp),
            vip = state.vip.copy(level = newLevel),
        )
    }

    /** VIP perk: idle rate multiplier. */
    fun idleMultiplier(state: GameState): Double {
        val t = Vip.tierFor(state.vip.level)
        return 1.0 + (t.level * 0.05) // ~5%/level — gated by specific perks in tiers
    }

    /** Idle cap bonus from VIP. */
    fun idleCapBonusHours(state: GameState): Int = Vip.tierFor(state.vip.level).idleCapBonusHours

    /** Monthly card status. */
    fun monthlyCardActive(state: GameState, now: Long): Boolean =
        state.vip.monthlyCardExpiresAt > now

    fun grantMonthlyCard(state: GameState, now: Long): GameState {
        val thirtyDays = 30L * 24 * 3600 * 1000
        return state.copy(
            vip = state.vip.copy(monthlyCardExpiresAt = maxOf(state.vip.monthlyCardExpiresAt, now) + thirtyDays)
        )
    }
}
