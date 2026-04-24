package com.herobrawl.game.data

import com.herobrawl.game.model.ClassId
import com.herobrawl.game.model.HeroClass
import com.herobrawl.game.model.HeroStats

object Classes {
    val all: Map<ClassId, HeroClass> = mapOf(
        ClassId.GUARDIAN to HeroClass(
            ClassId.GUARDIAN, "Guardian", "Tank",
            "Frontline wall. Taunts enemies and reduces incoming damage.",
            HeroStats(attack = 120, health = 2400, armor = 180, speed = 85, willpower = 60)
        ),
        ClassId.BERSERKER to HeroClass(
            ClassId.BERSERKER, "Berserker", "Bruiser",
            "Frontline DPS. Gains attack as HP drops.",
            HeroStats(attack = 210, health = 1800, armor = 110, speed = 95, willpower = 55)
        ),
        ClassId.MAGE to HeroClass(
            ClassId.MAGE, "Mage", "AoE Caster",
            "Backline caster with arena-wide spells and crowd control.",
            HeroStats(attack = 260, health = 1300, armor = 70, speed = 100, willpower = 40)
        ),
        ClassId.ASSASSIN to HeroClass(
            ClassId.ASSASSIN, "Assassin", "Burst",
            "Leaps to enemy backline to delete supports and squishies.",
            HeroStats(attack = 280, health = 1250, armor = 60, speed = 130, willpower = 50)
        ),
        ClassId.RANGER to HeroClass(
            ClassId.RANGER, "Ranger", "Sustain DPS",
            "Backline ranged dps with armor-piercing shots.",
            HeroStats(attack = 230, health = 1400, armor = 80, speed = 110, willpower = 50)
        ),
        ClassId.CLERIC to HeroClass(
            ClassId.CLERIC, "Cleric", "Support",
            "Heals, shields, and cleanses allies with sacred light.",
            HeroStats(attack = 140, health = 1500, armor = 90, speed = 105, willpower = 75)
        ),
    )
}
