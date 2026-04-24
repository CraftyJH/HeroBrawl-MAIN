package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState

data class AchievementDef(
    val id: String,
    val name: String,
    val description: String,
    val rewardGems: Long,
    val rewardScrolls: Int = 0,
    val rewardFragments: Long = 0,
    val check: (GameState) -> Boolean,
)

object Achievements {
    val all: List<AchievementDef> = listOf(
        AchievementDef("first_pull", "First Summon", "Summon your first hero.",
            rewardGems = 50) { it.gacha.heroicPulls >= 1 },
        AchievementDef("ten_heroes", "Recruiter", "Collect 10 unique heroes.",
            rewardGems = 100, rewardScrolls = 2) { it.heroes.size >= 10 },
        AchievementDef("thirty_heroes", "Roster Rich", "Collect 30 unique heroes.",
            rewardGems = 300, rewardScrolls = 5) { it.heroes.size >= 30 },
        AchievementDef("chapter_3", "Frontier Commander", "Clear Chapter 3.",
            rewardGems = 150) { it.campaign.chapter > 3 || (it.campaign.chapter == 3 && it.campaign.stage > 10) },
        AchievementDef("chapter_5", "Warborn", "Clear Chapter 5.",
            rewardGems = 300, rewardFragments = 100) { it.campaign.chapter > 5 },
        AchievementDef("arena_1500", "Arena Gladiator", "Reach 1500 arena rating.",
            rewardGems = 200) { it.arena.rating >= 1500 },
        AchievementDef("legendary", "A Legend is Born", "Own a 5★ hero.",
            rewardGems = 100) { it.heroes.any { h -> h.stars >= 5 } },
        AchievementDef("mono_faction", "Bonded", "Field 5 heroes from one faction in your lineup.",
            rewardGems = 80, rewardFragments = 60) { st ->
            val f = st.lineup.slots.mapNotNull { id -> id?.let { st.heroes.firstOrNull { h -> h.instanceId == it } } }
                .map { Stats.templateFor(it).faction }
            f.size == 5 && f.toSet().size == 1
        },
        AchievementDef("skill_level_5", "Skillmaster", "Upgrade any skill to level 5.",
            rewardGems = 80) { st -> st.heroes.any { h -> h.skillLevels.values.any { it >= 5 } } },
        AchievementDef("all_stones", "Stonebound", "Equip 3 stones on one hero.",
            rewardGems = 120, rewardFragments = 80) { st -> st.heroes.any { h -> h.equippedStones.size >= 3 } },
    )

    /** Re-evaluate achievement unlocks. Claiming is a separate action. */
    fun evaluate(state: GameState): GameState {
        val unlockedNow = state.achievements.unlocked.toMutableSet()
        all.forEach { if (it.check(state)) unlockedNow += it.id }
        return if (unlockedNow == state.achievements.unlocked) state
        else state.copy(achievements = state.achievements.copy(unlocked = unlockedNow))
    }

    fun canClaim(state: GameState, id: String): Boolean =
        id in state.achievements.unlocked && id !in state.achievements.claimed

    fun claim(state: GameState, id: String): GameState {
        if (!canClaim(state, id)) return state
        val def = all.firstOrNull { it.id == id } ?: return state
        return state.copy(
            currency = state.currency.copy(
                gems = state.currency.gems + def.rewardGems,
                heroicScrolls = state.currency.heroicScrolls + def.rewardScrolls,
                stoneFragments = state.currency.stoneFragments + def.rewardFragments,
            ),
            achievements = state.achievements.copy(
                claimed = state.achievements.claimed + id,
            ),
        )
    }
}
