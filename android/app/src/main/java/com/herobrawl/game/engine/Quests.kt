package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import java.time.LocalDate
import java.time.ZoneOffset

data class QuestDef(
    val id: String,
    val name: String,
    val description: String,
    val target: Int,
    val rewardGems: Long = 0,
    val rewardGold: Long = 0,
    val rewardScrolls: Int = 0,
    val rewardDust: Long = 0,
    val rewardOrbs: Long = 0,
    val rewardFragments: Long = 0,
)

object Quests {
    val dailies: List<QuestDef> = listOf(
        QuestDef("campaign-3", "Campaign Commander", "Complete 3 campaign stages.", 3, rewardGold = 5_000, rewardDust = 10),
        QuestDef("pulls-1", "Fateful Summons", "Perform 1 gacha pull.", 1, rewardGems = 40, rewardOrbs = 2),
        QuestDef("arena-2", "Arena Challenger", "Fight 2 arena matches.", 2, rewardGems = 30, rewardScrolls = 1),
        QuestDef("idle-claim", "Diligent Captain", "Claim your idle rewards.", 1, rewardGold = 2_000, rewardDust = 5),
        QuestDef("level-hero", "Trainer", "Level up any hero 5 times.", 5, rewardGold = 3_000, rewardGems = 20),
        QuestDef("stone-equip", "Stonebinder", "Equip any stone once.", 1, rewardFragments = 30, rewardGems = 15), // new
    )

    const val completionGems = 100L
    const val completionScrolls = 1

    fun today(now: Long): String =
        LocalDate.ofInstant(java.time.Instant.ofEpochMilli(now), ZoneOffset.UTC).toString()

    fun ensureRollover(state: GameState, now: Long): GameState {
        val d = today(now)
        if (state.quests.day == d && state.arena.ticketDay == d) return state
        return state.copy(
            quests = state.quests.copy(
                day = d, progress = emptyMap(), claimed = emptyMap(), completionClaimed = false,
            ),
            arena = state.arena.copy(ticketsUsed = 0, ticketDay = d),
        )
    }

    fun bump(state: GameState, id: String, by: Int = 1): GameState {
        val def = dailies.firstOrNull { it.id == id } ?: return state
        val cur = state.quests.progress[id] ?: 0
        val next = minOf(def.target, cur + by)
        return state.copy(
            quests = state.quests.copy(progress = state.quests.progress + (id to next))
        )
    }

    fun canClaim(state: GameState, id: String): Boolean {
        val def = dailies.firstOrNull { it.id == id } ?: return false
        if (state.quests.claimed[id] == true) return false
        return (state.quests.progress[id] ?: 0) >= def.target
    }

    fun claim(state: GameState, id: String): GameState {
        if (!canClaim(state, id)) return state
        val def = dailies.first { it.id == id }
        return state.copy(
            currency = state.currency.copy(
                gems = state.currency.gems + def.rewardGems,
                gold = state.currency.gold + def.rewardGold,
                heroicScrolls = state.currency.heroicScrolls + def.rewardScrolls,
                dust = state.currency.dust + def.rewardDust,
                prophetOrbs = state.currency.prophetOrbs + def.rewardOrbs,
                stoneFragments = state.currency.stoneFragments + def.rewardFragments,
            ),
            quests = state.quests.copy(claimed = state.quests.claimed + (id to true)),
        )
    }

    fun allCompleted(state: GameState): Boolean =
        dailies.all { (state.quests.progress[it.id] ?: 0) >= it.target }

    fun canClaimCompletion(state: GameState): Boolean =
        allCompleted(state) && !state.quests.completionClaimed

    fun claimCompletion(state: GameState): GameState {
        if (!canClaimCompletion(state)) return state
        return state.copy(
            currency = state.currency.copy(
                gems = state.currency.gems + completionGems,
                heroicScrolls = state.currency.heroicScrolls + completionScrolls,
            ),
            quests = state.quests.copy(completionClaimed = true),
        )
    }
}
