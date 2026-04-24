package com.herobrawl.game.engine

import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.HeroStats
import com.herobrawl.game.model.HeroTemplate
import com.herobrawl.game.model.StoneKind
import kotlin.math.pow
import kotlin.math.roundToInt

object Stats {
    fun levelMultiplier(level: Int): Double =
        1.0 + (level - 1) * 0.06 + (level - 1).toDouble().pow(1.45) * 0.004

    fun starMultiplier(stars: Int): Double = 1.0 + (stars - 1) * 0.18
    fun gearMultiplier(tier: Int): Double = 1.0 + tier * 0.09
    fun echoMultiplier(stacks: Int): Double = 1.0 + minOf(stacks, 10) * 0.03

    fun templateFor(inst: HeroInstance): HeroTemplate = Heroes.byId.getValue(inst.templateId)

    data class StoneBonus(
        val attack: Double = 0.0,
        val health: Double = 0.0,
        val speed: Double = 0.0,
        val willpower: Double = 0.0,
        val crit: Double = 0.0,
        val energy: Double = 0.0,
    )

    fun stoneBonus(inst: HeroInstance): StoneBonus {
        val b = StoneBonus()
        var atk = 0.0; var hp = 0.0; var spd = 0.0; var will = 0.0; var crit = 0.0; var energy = 0.0
        inst.equippedStones.values.forEach {
            when (it) {
                StoneKind.ATTACK -> atk += 0.06
                StoneKind.HEALTH -> hp += 0.08
                StoneKind.SPEED -> spd += 0.06
                StoneKind.ENERGY -> energy += 0.10
                StoneKind.CRIT -> crit += 0.08
            }
        }
        return b.copy(attack = atk, health = hp, speed = spd, willpower = will, crit = crit, energy = energy)
    }

    fun compute(inst: HeroInstance): HeroStats {
        val t = templateFor(inst)
        val base = Classes.all.getValue(t.heroClass).baseStats
        val rarityMul = 0.85 + t.baseRarity * 0.1
        val mul = levelMultiplier(inst.level) *
            starMultiplier(inst.stars) *
            gearMultiplier(inst.gearTier) *
            echoMultiplier(inst.echoStacks) *
            rarityMul
        val bonus = stoneBonus(inst)
        return HeroStats(
            attack = (base.attack * mul * (1.0 + bonus.attack)).roundToInt(),
            health = (base.health * mul * (1.0 + bonus.health)).roundToInt(),
            armor = (base.armor * mul * 0.85).roundToInt(),
            speed = (base.speed * (1.0 + bonus.speed)).roundToInt(),
            willpower = (base.willpower * (1.0 + bonus.willpower)).roundToInt(),
        )
    }

    fun power(inst: HeroInstance): Int {
        val s = compute(inst)
        return (s.attack * 4.0 + s.health * 0.5 + s.armor * 2.0 + s.speed * 5.0).roundToInt()
    }

    fun xpToNext(level: Int): Int = (60 + level.toDouble().pow(1.9) * 14).roundToInt()
    fun goldToLevel(level: Int): Int = (40 + level.toDouble().pow(1.8) * 9).roundToInt()

    fun levelCap(stars: Int): Int = 30 + stars * 18
}
