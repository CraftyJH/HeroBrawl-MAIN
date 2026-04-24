package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState

data class EventMilestone(
    val id: String,
    val tokensRequired: Long,
    val rewardGems: Long = 0,
    val rewardScrolls: Int = 0,
    val rewardOrbs: Long = 0,
    val rewardFragments: Long = 0,
    val rewardHeroShardId: String? = null,
)

data class EventDef(
    val id: String,
    val name: String,
    val tagline: String,
    val featuredHeroId: String,
    val milestones: List<EventMilestone>,
)

/**
 * The iteration's signature new system. Events run alongside the core loop, grant
 * event-only limited heroes, and reward existing activities with tokens (no grind duplication).
 */
object Events {
    val dawnbloomFestival = EventDef(
        id = "dawnbloom_festival",
        name = "Dawnbloom Festival",
        tagline = "Every fight blooms a token. Unlock Solara, the Sunsovereign.",
        featuredHeroId = "ev_solara",
        milestones = listOf(
            EventMilestone("m10",   10, rewardGems = 50, rewardScrolls = 1),
            EventMilestone("m50",   50, rewardGems = 100, rewardOrbs = 5),
            EventMilestone("m150", 150, rewardGems = 150, rewardScrolls = 3, rewardFragments = 40),
            EventMilestone("m400", 400, rewardGems = 300, rewardScrolls = 5, rewardFragments = 80),
            EventMilestone("m1000", 1000, rewardGems = 500, rewardScrolls = 10, rewardHeroShardId = "ev_solara"),
        ),
    )

    val all: Map<String, EventDef> = mapOf(dawnbloomFestival.id to dawnbloomFestival)

    fun active(state: GameState): EventDef =
        all[state.events.activeEventId] ?: dawnbloomFestival

    /** Every battle awards event tokens. */
    fun gainTokens(state: GameState, amount: Long): GameState =
        state.copy(events = state.events.copy(tokensEarned = state.events.tokensEarned + amount))

    fun canClaim(state: GameState, milestoneId: String): Boolean {
        val def = active(state)
        val m = def.milestones.firstOrNull { it.id == milestoneId } ?: return false
        if (milestoneId in state.events.milestonesClaimed) return false
        return state.events.tokensEarned >= m.tokensRequired
    }

    fun claim(state: GameState, milestoneId: String): GameState {
        if (!canClaim(state, milestoneId)) return state
        val def = active(state)
        val m = def.milestones.first { it.id == milestoneId }
        var heroes = state.heroes
        if (m.rewardHeroShardId != null) {
            // Grant or echo the featured limited hero.
            val existing = heroes.firstOrNull { it.templateId == m.rewardHeroShardId }
            heroes = if (existing != null) {
                heroes.map { if (it.instanceId == existing.instanceId) it.copy(echoStacks = it.echoStacks + 1) else it }
            } else {
                val t = com.herobrawl.game.data.Heroes.byId.getValue(m.rewardHeroShardId)
                heroes + com.herobrawl.game.model.HeroInstance(
                    instanceId = java.util.UUID.randomUUID().toString(),
                    templateId = m.rewardHeroShardId,
                    stars = t.baseRarity,
                )
            }
        }
        return state.copy(
            heroes = heroes,
            currency = state.currency.copy(
                gems = state.currency.gems + m.rewardGems,
                heroicScrolls = state.currency.heroicScrolls + m.rewardScrolls,
                prophetOrbs = state.currency.prophetOrbs + m.rewardOrbs,
                stoneFragments = state.currency.stoneFragments + m.rewardFragments,
            ),
            events = state.events.copy(
                milestonesClaimed = state.events.milestonesClaimed + milestoneId,
            ),
        )
    }
}
