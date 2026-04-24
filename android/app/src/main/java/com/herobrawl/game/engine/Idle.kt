package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.IdleRates
import kotlin.math.roundToLong

data class IdleReward(
    val gold: Long,
    val spirit: Long,
    val shards: Long,
    val hours: Double,
    val capped: Boolean,
)

object Idle {
    fun compute(state: GameState, now: Long): IdleReward {
        val elapsedMs = (now - state.idle.tickedAt).coerceAtLeast(0L)
        val capMs = state.idle.capHours * 3600L * 1000L
        val effective = minOf(elapsedMs, capMs)
        val hours = effective / 3_600_000.0
        return IdleReward(
            gold = (state.idle.ratePerHour.gold * hours).roundToLong(),
            spirit = (state.idle.ratePerHour.spirit * hours).roundToLong(),
            shards = (state.idle.ratePerHour.shards * hours).roundToLong(),
            hours = hours,
            capped = elapsedMs >= capMs,
        )
    }

    fun claim(state: GameState, now: Long): GameState {
        val r = compute(state, now)
        return state.copy(
            idle = state.idle.copy(tickedAt = now),
            currency = state.currency.copy(
                gold = state.currency.gold + r.gold,
                spirit = state.currency.spirit + r.spirit,
                shards = state.currency.shards + r.shards,
            )
        )
    }

    fun upgradedRates(chapter: Int, stage: Int): IdleRates {
        val progress = (chapter - 1) * 10 + (stage - 1)
        val mul = 1.0 + progress * 0.08
        return IdleRates(
            gold = (1200L * mul).roundToLong(),
            spirit = (60L * mul).roundToLong(),
            shards = (4L * mul).roundToLong(),
        )
    }
}
