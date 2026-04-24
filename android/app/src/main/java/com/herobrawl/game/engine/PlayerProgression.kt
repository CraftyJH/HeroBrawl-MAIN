package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import kotlin.math.pow

object PlayerProgression {
    fun xpToNext(level: Int): Int = (200 + level.toDouble().pow(1.8) * 50).toInt()

    fun givePlayerXp(state: GameState, xp: Int): GameState {
        var level = state.playerLevel
        var cur = state.playerXp + xp
        var needed = xpToNext(level)
        while (cur >= needed && level < 200) {
            cur -= needed
            level++
            needed = xpToNext(level)
        }
        return state.copy(playerLevel = level, playerXp = cur)
    }
}
