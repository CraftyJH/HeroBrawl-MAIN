package com.herobrawl.game.engine

import com.herobrawl.game.data.Classes
import com.herobrawl.game.data.Factions
import com.herobrawl.game.data.Skills
import com.herobrawl.game.model.ClassId
import com.herobrawl.game.model.FactionId
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.Skill
import com.herobrawl.game.model.SkillTarget
import com.herobrawl.game.model.StatusEffect
import com.herobrawl.game.model.StatusKind
import kotlin.math.roundToInt
import kotlin.random.Random

data class BattleUnit(
    val uid: String,
    val side: Side,
    val slot: Int,
    val name: String,
    val emoji: String,
    val faction: FactionId,
    val classId: ClassId,
    val stars: Int,
    val gradientStart: Long,
    val gradientEnd: Long,
    val signatureColor: Long,
    val skills: List<Skill>,
    var attack: Int,
    var maxHp: Int,
    var hp: Int,
    var armor: Int,
    var speed: Int,
    var willpower: Int,
    var energy: Int = 0,
    val statuses: MutableList<StatusEffect> = mutableListOf(),
) {
    enum class Side { ALLY, ENEMY }
}

data class BattleLog(
    val kind: Kind,
    val text: String,
    val actor: String? = null,
    val target: String? = null,
    val amount: Int = 0,
) {
    enum class Kind { INFO, ATTACK, SKILL, HEAL, STATUS, DEATH, VICTORY, DEFEAT }
}

data class BattleResult(
    val winner: BattleUnit.Side,
    val turns: Int,
    val log: List<BattleLog>,
    val allyStart: List<BattleUnit>,
    val enemyStart: List<BattleUnit>,
)

object Combat {
    fun unitFromInstance(
        inst: HeroInstance, side: BattleUnit.Side, slot: Int,
        auraAtk: Double, auraHp: Double, auraSpd: Double,
    ): BattleUnit {
        val t = Stats.templateFor(inst)
        val s = Stats.compute(inst)
        val maxHp = (s.health * (1.0 + auraHp)).roundToInt()
        val atk = (s.attack * (1.0 + auraAtk)).roundToInt()
        val spd = (s.speed * (1.0 + auraSpd)).roundToInt()
        return BattleUnit(
            uid = "${side.name}-$slot-${inst.instanceId}",
            side = side, slot = slot,
            name = t.name, emoji = t.emoji,
            faction = t.faction, classId = t.heroClass,
            stars = inst.stars,
            gradientStart = t.portraitGradient[0],
            gradientEnd = t.portraitGradient[1],
            signatureColor = t.signatureColor,
            skills = t.skills,
            attack = atk, maxHp = maxHp, hp = maxHp,
            armor = s.armor, speed = spd, willpower = s.willpower,
            energy = if (t.heroClass == ClassId.MAGE) 30 else 0,
        )
    }

    fun buildAllyUnits(state: GameState): List<BattleUnit> {
        val insts = state.lineup.slots.mapNotNull { id ->
            id?.let { state.heroes.firstOrNull { h -> h.instanceId == it } }
        }
        val aura = Factions.lineupAura(insts.map { Stats.templateFor(it).faction })
        return state.lineup.slots.mapIndexedNotNull { slot, id ->
            val inst = id?.let { state.heroes.firstOrNull { h -> h.instanceId == it } } ?: return@mapIndexedNotNull null
            unitFromInstance(inst, BattleUnit.Side.ALLY, slot, aura.attack, aura.health, aura.speed)
        }
    }

    fun buildEnemyUnits(chapter: Int, stage: Int, rnd: Random = Random.Default): List<BattleUnit> {
        val progress = (chapter - 1) * 10 + (stage - 1)
        val power = 1.0 + progress * 0.18
        val slotPicks = listOf(ClassId.GUARDIAN, ClassId.GUARDIAN, ClassId.BERSERKER, ClassId.MAGE, ClassId.RANGER)
        val names = listOf("Rogue Sellsword", "Cursed Knight", "Wildfire Witch",
            "Night Stalker", "Ironclad", "Lost Pilgrim")
        val emojiFor = mapOf(
            ClassId.GUARDIAN to "🗿", ClassId.BERSERKER to "💢", ClassId.MAGE to "🌀",
            ClassId.ASSASSIN to "🕸️", ClassId.RANGER to "🎯", ClassId.CLERIC to "🕯️",
        )
        val factions = FactionId.values()

        return (0 until 5).map { slot ->
            val cls = slotPicks[slot]
            val faction = factions[(slot * 31 + chapter * 17 + stage * 7 + rnd.nextInt(0, 1_000_000)) % factions.size]
            val base = Classes.all.getValue(cls).baseStats
            val stars = minOf(5, 2 + progress / 8)
            val mul = power * (0.85 + stars * 0.1)
            val maxHp = (base.health * mul).roundToInt()
            BattleUnit(
                uid = "enemy-$slot-$progress-${rnd.nextInt()}",
                side = BattleUnit.Side.ENEMY,
                slot = slot,
                name = "${names[slot % names.size]} ${progress + 1}",
                emoji = emojiFor.getValue(cls),
                faction = faction,
                classId = cls,
                stars = stars,
                gradientStart = 0xFF2A2A36,
                gradientEnd = 0xFF4A4A5A,
                signatureColor = 0xFF8A8A9A,
                skills = Skills.defaultFor(cls),
                attack = (base.attack * mul).roundToInt(),
                maxHp = maxHp,
                hp = maxHp,
                armor = (base.armor * mul * 0.8).roundToInt(),
                speed = base.speed,
                willpower = base.willpower,
            )
        }
    }

    fun simulate(
        allies: List<BattleUnit>, enemies: List<BattleUnit>,
        rnd: Random = Random.Default, maxTurns: Int = 40,
    ): BattleResult {
        val A = allies.map { it.copy(statuses = it.statuses.toMutableList(), skills = it.skills.toList()) }
        val E = enemies.map { it.copy(statuses = it.statuses.toMutableList(), skills = it.skills.toList()) }
        val log = mutableListOf<BattleLog>()
        log += BattleLog(BattleLog.Kind.INFO, "Battle begins!")
        var turn = 0
        val allyStart = A.map { it.copy(statuses = mutableListOf()) }
        val enemyStart = E.map { it.copy(statuses = mutableListOf()) }

        while (turn < maxTurns) {
            turn++
            val order = (A + E).filter { it.hp > 0 }.sortedByDescending { it.speed }
            for (actor in order) {
                if (actor.hp <= 0) continue
                if (tickStatuses(actor, log)) continue
                val opponents = if (actor.side == BattleUnit.Side.ALLY) E else A
                if (opponents.all { it.hp <= 0 }) break

                actor.energy = minOf(100, actor.energy + 25 + if (actor.classId == ClassId.MAGE) 10 else 0)
                val active = actor.skills.firstOrNull { it.active }
                if (actor.energy >= 100 && active != null) {
                    actor.energy = 0
                    executeSkill(actor, active, A, E, log, rnd)
                } else {
                    basicAttack(actor, opponents, log, rnd)
                }
                if (A.all { it.hp <= 0 } || E.all { it.hp <= 0 }) break
            }
            for (u in A + E) if (u.hp > 0) applyDots(u, log)
            if (A.all { it.hp <= 0 } || E.all { it.hp <= 0 }) break
        }

        val winner = if (A.any { it.hp > 0 }) BattleUnit.Side.ALLY else BattleUnit.Side.ENEMY
        log += BattleLog(
            if (winner == BattleUnit.Side.ALLY) BattleLog.Kind.VICTORY else BattleLog.Kind.DEFEAT,
            if (winner == BattleUnit.Side.ALLY) "Victory!" else "Defeat."
        )
        return BattleResult(winner, turn, log.toList(), allyStart, enemyStart)
    }

    private fun tickStatuses(u: BattleUnit, log: MutableList<BattleLog>): Boolean {
        var stunned = false
        val keep = mutableListOf<StatusEffect>()
        for (s in u.statuses) {
            if (s.kind == StatusKind.STUN && s.duration > 0) {
                stunned = true
                log += BattleLog(BattleLog.Kind.STATUS, "${u.name} is stunned.", u.uid)
            }
            val d = s.duration - 1
            if (d > 0) keep += s.copy(duration = d)
        }
        u.statuses.clear(); u.statuses.addAll(keep)
        return stunned
    }

    private fun applyDots(u: BattleUnit, log: MutableList<BattleLog>) {
        for (s in u.statuses) when (s.kind) {
            StatusKind.BURN, StatusKind.POISON -> {
                val dmg = (u.maxHp * s.magnitude).roundToInt()
                u.hp = maxOf(0, u.hp - dmg)
                log += BattleLog(BattleLog.Kind.STATUS, "${u.name} suffers $dmg ${s.kind.name.lowercase()} damage.", u.uid, amount = dmg)
                if (u.hp == 0) log += BattleLog(BattleLog.Kind.DEATH, "${u.name} falls.", u.uid)
            }
            StatusKind.REGEN -> {
                val heal = (u.maxHp * s.magnitude).roundToInt()
                u.hp = minOf(u.maxHp, u.hp + heal)
                log += BattleLog(BattleLog.Kind.HEAL, "${u.name} regenerates $heal HP.", u.uid, amount = heal)
            }
            else -> Unit
        }
    }

    private fun pickTarget(actor: BattleUnit, opponents: List<BattleUnit>): BattleUnit? {
        val alive = opponents.filter { it.hp > 0 }
        if (alive.isEmpty()) return null
        if (actor.classId == ClassId.ASSASSIN) return alive.minBy { it.hp }
        if (actor.classId == ClassId.RANGER || actor.classId == ClassId.MAGE) {
            val back = alive.filter { it.slot >= 3 }
            if (back.isNotEmpty()) return back.first()
        }
        val front = alive.filter { it.slot <= 2 }
        return if (front.isNotEmpty()) front.first() else alive.first()
    }

    private fun basicAttack(actor: BattleUnit, opponents: List<BattleUnit>, log: MutableList<BattleLog>, rnd: Random) {
        val t = pickTarget(actor, opponents) ?: return
        val dmg = computeDamage(actor, t, 1.0, rnd)
        t.hp = maxOf(0, t.hp - dmg)
        log += BattleLog(BattleLog.Kind.ATTACK, "${actor.name} attacks ${t.name} for $dmg.", actor.uid, t.uid, dmg)
        if (t.hp == 0) log += BattleLog(BattleLog.Kind.DEATH, "${t.name} falls.", t.uid)
    }

    private fun executeSkill(
        actor: BattleUnit, skill: Skill,
        allies: List<BattleUnit>, enemies: List<BattleUnit>,
        log: MutableList<BattleLog>, rnd: Random,
    ) {
        val opponents = if (actor.side == BattleUnit.Side.ALLY) enemies else allies
        val friends = if (actor.side == BattleUnit.Side.ALLY) allies else enemies
        log += BattleLog(BattleLog.Kind.SKILL, "${actor.name} unleashes ${skill.name}!", actor.uid)
        val power = skill.power
        when {
            skill.target == SkillTarget.ALL_ENEMIES || skill.aoe -> {
                for (t in opponents.filter { it.hp > 0 }) {
                    val dmg = computeDamage(actor, t, power, rnd)
                    t.hp = maxOf(0, t.hp - dmg)
                    log += BattleLog(BattleLog.Kind.ATTACK, "→ hits ${t.name} for $dmg.", actor.uid, t.uid, dmg)
                    skill.status?.let { t.statuses += it.copy() }
                    if (t.hp == 0) log += BattleLog(BattleLog.Kind.DEATH, "${t.name} falls.", t.uid)
                }
            }
            skill.target == SkillTarget.ALL_ALLIES -> {
                val heal = (actor.attack * power).roundToInt()
                for (f in friends.filter { it.hp > 0 }) {
                    f.hp = minOf(f.maxHp, f.hp + heal)
                    log += BattleLog(BattleLog.Kind.HEAL, "→ restores $heal HP to ${f.name}.", actor.uid, f.uid, heal)
                    skill.status?.let { f.statuses += it.copy() }
                }
            }
            else -> {
                val t = pickTarget(actor, opponents) ?: return
                val dmg = computeDamage(actor, t, power, rnd)
                t.hp = maxOf(0, t.hp - dmg)
                log += BattleLog(BattleLog.Kind.ATTACK, "→ strikes ${t.name} for $dmg.", actor.uid, t.uid, dmg)
                skill.status?.let { t.statuses += it.copy() }
                if (t.hp == 0) log += BattleLog(BattleLog.Kind.DEATH, "${t.name} falls.", t.uid)
                if (skill.status?.kind == StatusKind.SHIELD) actor.statuses += skill.status.copy()
            }
        }
    }

    private fun computeDamage(
        attacker: BattleUnit, defender: BattleUnit, powerMul: Double, rnd: Random,
    ): Int {
        val adv = Factions.advantage(attacker.faction, defender.faction)
        var dmg = attacker.attack * powerMul
        if (attacker.classId == ClassId.BERSERKER) {
            val missing = 1.0 - attacker.hp.toDouble() / attacker.maxHp.coerceAtLeast(1)
            dmg *= 1.0 + missing
        }
        if (attacker.classId == ClassId.RANGER) dmg *= 1.15
        val armor = defender.armor * (if (attacker.classId == ClassId.RANGER) 0.7 else 1.0)
        val mitigation = armor / (armor + 500.0)
        dmg *= 1.0 - mitigation
        dmg *= 1.0 + adv
        val critChance = if (attacker.classId == ClassId.ASSASSIN &&
            defender.hp < defender.maxHp * 0.5) 0.5 else 0.2
        val crit = rnd.nextDouble() < critChance
        if (crit) dmg *= 1.8
        if (defender.classId == ClassId.GUARDIAN &&
            (attacker.classId == ClassId.BERSERKER || attacker.classId == ClassId.ASSASSIN ||
                attacker.classId == ClassId.GUARDIAN)) dmg *= 0.85
        val shield = defender.statuses.firstOrNull { it.kind == StatusKind.SHIELD }
        if (shield != null) dmg *= 1.0 - minOf(0.4, shield.magnitude)
        return maxOf(1, dmg.roundToInt())
    }

    data class Rewards(
        val gold: Long, val spirit: Long, val xp: Int, val brawlPoints: Int,
        val firstClearGems: Long = 0,
    )

    fun campaignRewards(chapter: Int, stage: Int, firstClear: Boolean): Rewards {
        val progress = (chapter - 1) * 10 + (stage - 1)
        return Rewards(
            gold = 800L + progress * 120,
            spirit = 30L + progress * 6,
            xp = 40 + progress * 8,
            brawlPoints = 20 + progress * 3,
            firstClearGems = if (firstClear) 20L else 0L,
        )
    }
}
