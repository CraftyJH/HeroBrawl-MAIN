package com.herobrawl.game.data

import com.herobrawl.game.model.ClassId
import com.herobrawl.game.model.Skill
import com.herobrawl.game.model.SkillTarget
import com.herobrawl.game.model.StatusEffect
import com.herobrawl.game.model.StatusKind

object Skills {
    val classActive: Map<ClassId, Skill> = mapOf(
        ClassId.GUARDIAN to Skill(
            "gd_active", "Bulwark Strike",
            "Slam the nearest enemy for 180% ATK and gain a shield equal to 25% max HP.",
            active = true, energyCost = 100, power = 1.8, target = SkillTarget.ENEMY,
            status = StatusEffect(StatusKind.SHIELD, 2, 0.25)
        ),
        ClassId.BERSERKER to Skill(
            "bz_active", "Crimson Rend",
            "Cleave the frontline for 220% ATK to up to 3 enemies.",
            active = true, energyCost = 100, power = 2.2, aoe = true, target = SkillTarget.ALL_ENEMIES
        ),
        ClassId.MAGE to Skill(
            "mg_active", "Arcane Cascade",
            "Blast all enemies for 150% ATK and apply Burn (2 turns).",
            active = true, energyCost = 100, power = 1.5, aoe = true, target = SkillTarget.ALL_ENEMIES,
            status = StatusEffect(StatusKind.BURN, 2, 0.15)
        ),
        ClassId.ASSASSIN to Skill(
            "as_active", "Shadow Pounce",
            "Teleport and strike the enemy with the lowest HP for 320% ATK.",
            active = true, energyCost = 100, power = 3.2, target = SkillTarget.ENEMY
        ),
        ClassId.RANGER to Skill(
            "rg_active", "Piercing Volley",
            "Fire 3 arrows that each hit for 120% ATK, ignoring 40% armor.",
            active = true, energyCost = 100, power = 2.6, target = SkillTarget.ENEMY
        ),
        ClassId.CLERIC to Skill(
            "cl_active", "Dawnbloom",
            "Heal the team for 180% ATK and grant Regen for 2 turns.",
            active = true, energyCost = 100, power = 1.8, aoe = true, target = SkillTarget.ALL_ALLIES,
            status = StatusEffect(StatusKind.REGEN, 2, 0.10)
        ),
    )

    val classPassive: Map<ClassId, Skill> = mapOf(
        ClassId.GUARDIAN to Skill("gd_pass", "Iron Resolve", "Takes 15% less damage from melee attacks.", false),
        ClassId.BERSERKER to Skill("bz_pass", "Bloodrage", "+1% ATK for every 1% missing HP.", false),
        ClassId.MAGE to Skill("mg_pass", "Manaflow", "Gains +10 energy at start of each turn.", false),
        ClassId.ASSASSIN to Skill("as_pass", "Killsight", "Crit chance doubled vs targets under 50% HP.", false),
        ClassId.RANGER to Skill("rg_pass", "Hawk's Eye", "Ignores 30% armor on basic attacks.", false),
        ClassId.CLERIC to Skill("cl_pass", "Radiant Aegis", "Allies under 30% HP take 25% reduced damage.", false),
    )

    fun defaultFor(classId: ClassId): List<Skill> =
        listOf(classPassive.getValue(classId), classActive.getValue(classId))
}
