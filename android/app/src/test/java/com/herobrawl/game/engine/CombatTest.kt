package com.herobrawl.game.engine

import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.HeroInstance
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CombatTest {
    private fun hero(id: String, level: Int = 20, stars: Int = 5, gear: Int = 2) =
        HeroInstance(
            instanceId = "t-$id", templateId = id, level = level,
            stars = stars, xp = 0, echoStacks = 0, gearTier = gear,
        )

    @Test
    fun `battle produces a winner within 40 turns`() {
        val allies = Heroes.all.take(5).mapIndexed { i, h ->
            Combat.unitFromInstance(hero(h.id), com.herobrawl.game.engine.BattleUnit.Side.ALLY, i, 0.0, 0.0, 0.0)
        }
        val enemies = Combat.buildEnemyUnits(1, 1, rnd = Random(1))
        val r = Combat.simulate(allies, enemies, rnd = Random(2))
        assertTrue(r.turns <= 40)
    }

    @Test
    fun `weak heroes lose to late stage enemies`() {
        val allies = Heroes.all.take(5).mapIndexed { i, h ->
            Combat.unitFromInstance(
                hero(h.id, level = 1, stars = 3, gear = 0),
                com.herobrawl.game.engine.BattleUnit.Side.ALLY, i, 0.0, 0.0, 0.0
            )
        }
        val enemies = Combat.buildEnemyUnits(8, 8, rnd = Random(1))
        val r = Combat.simulate(allies, enemies, rnd = Random(2))
        assertTrue(r.winner == com.herobrawl.game.engine.BattleUnit.Side.ENEMY)
    }
}
