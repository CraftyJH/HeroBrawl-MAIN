package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.StoneKind

object Progression {
    // --- XP / Level ---
    fun giveXp(state: GameState, instanceId: String, xp: Int): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        val cap = Stats.levelCap(hero.stars)
        var xpLeft = xp
        var level = hero.level
        var cur = hero.xp
        while (xpLeft > 0 && level < cap) {
            val need = Stats.xpToNext(level) - cur
            if (xpLeft >= need) {
                level += 1; xpLeft -= need; cur = 0
            } else {
                cur += xpLeft; xpLeft = 0
            }
        }
        return state.copy(heroes = state.heroes.map {
            if (it.instanceId == instanceId) it.copy(level = level, xp = cur) else it
        })
    }

    fun canLevelUp(state: GameState, hero: HeroInstance): Boolean {
        if (hero.level >= Stats.levelCap(hero.stars)) return false
        return state.currency.gold >= Stats.goldToLevel(hero.level)
    }

    fun levelUp(state: GameState, instanceId: String): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        if (!canLevelUp(state, hero)) return state
        val cost = Stats.goldToLevel(hero.level)
        return state.copy(
            currency = state.currency.copy(gold = state.currency.gold - cost),
            heroes = state.heroes.map {
                if (it.instanceId == instanceId) it.copy(level = it.level + 1, xp = 0) else it
            }
        )
    }

    // --- Ascension ---
    data class AscCost(val shards: Long, val spirit: Long, val gold: Long)

    fun ascensionCost(stars: Int): AscCost = when (stars) {
        5, 6, 7, 8, 9 -> AscCost(80L + (stars - 5) * 40, 1_200L + (stars - 5) * 600, 200_000L + (stars - 5) * 100_000)
        4 -> AscCost(30, 400, 50_000)
        3 -> AscCost(15, 200, 20_000)
        else -> AscCost(8, 100, 8_000)
    }

    fun canAscend(state: GameState, hero: HeroInstance): Boolean {
        if (hero.stars >= 10) return false
        if (hero.stars >= 5 && hero.echoStacks < 2) return false
        val c = ascensionCost(hero.stars)
        return state.currency.shards >= c.shards &&
            state.currency.spirit >= c.spirit &&
            state.currency.gold >= c.gold
    }

    fun ascend(state: GameState, instanceId: String): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        if (!canAscend(state, hero)) return state
        val c = ascensionCost(hero.stars)
        return state.copy(
            currency = state.currency.copy(
                shards = state.currency.shards - c.shards,
                spirit = state.currency.spirit - c.spirit,
                gold = state.currency.gold - c.gold,
            ),
            heroes = state.heroes.map {
                if (it.instanceId == instanceId) {
                    val newStars = minOf(10, it.stars + 1)
                    val newEcho = if (it.stars >= 5) it.echoStacks - 2 else it.echoStacks
                    it.copy(stars = newStars, echoStacks = newEcho)
                } else it
            }
        )
    }

    // --- Gear ---
    data class GearCost(val gold: Long, val shards: Long)

    fun gearCost(tier: Int) = GearCost(3_000L + tier * 4_500L, 5L + tier * 6L)

    fun canUpgradeGear(state: GameState, hero: HeroInstance): Boolean {
        if (hero.gearTier >= 6) return false
        val c = gearCost(hero.gearTier)
        return state.currency.gold >= c.gold && state.currency.shards >= c.shards
    }

    fun upgradeGear(state: GameState, instanceId: String): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        if (!canUpgradeGear(state, hero)) return state
        val c = gearCost(hero.gearTier)
        return state.copy(
            currency = state.currency.copy(
                gold = state.currency.gold - c.gold,
                shards = state.currency.shards - c.shards,
            ),
            heroes = state.heroes.map {
                if (it.instanceId == instanceId) it.copy(gearTier = it.gearTier + 1) else it
            }
        )
    }

    // --- Skill leveling (new iteration!) ---
    fun skillLevel(hero: HeroInstance, skillId: String): Int = hero.skillLevels[skillId] ?: 1
    fun skillCap(hero: HeroInstance): Int = minOf(10, hero.stars + 3)
    fun skillUpCost(level: Int): Long = 20L + level.toLong() * 15

    fun canUpgradeSkill(state: GameState, hero: HeroInstance, skillId: String): Boolean {
        val cur = skillLevel(hero, skillId)
        if (cur >= skillCap(hero)) return false
        return state.currency.dust >= skillUpCost(cur)
    }

    fun upgradeSkill(state: GameState, instanceId: String, skillId: String): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        if (!canUpgradeSkill(state, hero, skillId)) return state
        val cur = skillLevel(hero, skillId)
        val cost = skillUpCost(cur)
        return state.copy(
            currency = state.currency.copy(dust = state.currency.dust - cost),
            heroes = state.heroes.map {
                if (it.instanceId == instanceId) {
                    it.copy(skillLevels = it.skillLevels + (skillId to cur + 1))
                } else it
            }
        )
    }

    // --- Equipment Stones (new iteration!) ---
    // 3 stone slots per hero.
    const val STONE_SLOTS = 3
    fun stoneUnlockCost(slot: Int): Long = 50L + slot * 100L

    fun canEquipStone(state: GameState, hero: HeroInstance, slot: Int): Boolean {
        if (slot < 0 || slot >= STONE_SLOTS) return false
        return state.currency.stoneFragments >= stoneUnlockCost(slot)
    }

    fun equipStone(state: GameState, instanceId: String, slot: Int, kind: StoneKind): GameState {
        val hero = state.heroes.firstOrNull { it.instanceId == instanceId } ?: return state
        val already = hero.equippedStones.containsKey(slot.toString())
        if (!already && !canEquipStone(state, hero, slot)) return state
        val newStones = hero.equippedStones + (slot.toString() to kind)
        val cost = if (already) 0L else stoneUnlockCost(slot)
        return state.copy(
            currency = state.currency.copy(stoneFragments = state.currency.stoneFragments - cost),
            heroes = state.heroes.map {
                if (it.instanceId == instanceId) it.copy(equippedStones = newStones) else it
            }
        )
    }
}
