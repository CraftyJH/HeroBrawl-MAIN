package com.herobrawl.game.engine

import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.FactionId
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.HeroTemplate
import java.util.UUID
import kotlin.random.Random

/**
 * HeroBrawl transparent-pity gacha:
 *  - 3★ 65%, 4★ 28%, 5★ 7% (base)
 *  - Soft pity at 30 pulls: 5★ rate ramps by +2.5% per pull
 *  - Hard pity at 60 pulls: guaranteed 5★
 *  - Every 10th pull guaranteed 4★+
 *  - Duplicates convert to echo stacks (never wasted)
 */
object Gacha {
    data class PullResult(
        val template: HeroTemplate,
        val rarity: Int,
        val isDuplicate: Boolean,
    )

    data class PullSummary(val results: List<PullResult>, val state: GameState)

    private fun rollRarity(pity: Int, sinceEpic: Int, rnd: Random): Int {
        if (pity >= 59) return 5
        val guaranteedEpic = (sinceEpic + 1) % 10 == 0
        var fiveRate = 0.07
        if (pity >= 30) fiveRate = minOf(1.0, 0.07 + (pity - 29) * 0.025)
        val fourRate = 0.28
        val roll = rnd.nextDouble()
        if (roll < fiveRate) return 5
        if (guaranteedEpic) return 4
        if (roll < fiveRate + fourRate) return 4
        return 3
    }

    private fun pickTemplate(rarity: Int, factionFilter: FactionId?, rnd: Random): HeroTemplate {
        val pool = Heroes.byRarity[rarity] ?: Heroes.all.filter { it.baseRarity == rarity }
        val filtered = if (factionFilter != null) pool.filter { it.faction == factionFilter } else pool
        val finalPool = if (filtered.isEmpty()) pool else filtered
        return finalPool[rnd.nextInt(finalPool.size)]
    }

    fun pull(
        state: GameState,
        count: Int,
        factionFilter: FactionId? = null,
        rnd: Random = Random.Default,
    ): PullSummary {
        val results = mutableListOf<PullResult>()
        var pity = state.gacha.pityCount
        var sinceEpic = state.gacha.sinceEpic
        val heroes = state.heroes.toMutableList()

        repeat(count) {
            val rarity = rollRarity(pity, sinceEpic, rnd)
            val template = pickTemplate(rarity, factionFilter, rnd)
            val idx = heroes.indexOfFirst { it.templateId == template.id }
            if (idx >= 0) {
                heroes[idx] = heroes[idx].copy(echoStacks = heroes[idx].echoStacks + 1)
                results += PullResult(template, rarity, isDuplicate = true)
            } else {
                heroes += HeroInstance(
                    instanceId = UUID.randomUUID().toString(),
                    templateId = template.id,
                    level = 1,
                    stars = template.baseRarity,
                )
                results += PullResult(template, rarity, isDuplicate = false)
            }
            pity = if (rarity >= 5) 0 else pity + 1
            sinceEpic = if (rarity >= 4) 0 else sinceEpic + 1
        }

        return PullSummary(
            results = results,
            state = state.copy(
                heroes = heroes,
                gacha = state.gacha.copy(
                    heroicPulls = state.gacha.heroicPulls + count,
                    pityCount = pity,
                    sinceEpic = sinceEpic,
                ),
            ),
        )
    }
}
