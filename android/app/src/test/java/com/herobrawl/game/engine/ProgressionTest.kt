package com.herobrawl.game.engine

import com.herobrawl.game.data.Heroes
import com.herobrawl.game.model.HeroInstance
import com.herobrawl.game.model.StoneKind
import com.herobrawl.game.store.SaveStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {
    @Test
    fun `equipping a stone consumes fragments`() {
        val inst = HeroInstance(
            instanceId = "h1", templateId = Heroes.all.first().id, stars = 3
        )
        val s = SaveStore.newGame(0L).let {
            it.copy(
                heroes = listOf(inst),
                currency = it.currency.copy(stoneFragments = 1000),
            )
        }
        val next = Progression.equipStone(s, inst.instanceId, slot = 0, StoneKind.ATTACK)
        assertEquals(StoneKind.ATTACK, next.heroes.first().equippedStones["0"])
        assertTrue(next.currency.stoneFragments < s.currency.stoneFragments)
    }

    @Test
    fun `skill upgrade consumes dust`() {
        val template = Heroes.all.first()
        val inst = HeroInstance(instanceId = "h1", templateId = template.id, stars = 5)
        val s = SaveStore.newGame(0L).let {
            it.copy(heroes = listOf(inst), currency = it.currency.copy(dust = 1_000))
        }
        val skillId = template.skills.first().id
        val next = Progression.upgradeSkill(s, inst.instanceId, skillId)
        assertEquals(2, Progression.skillLevel(next.heroes.first(), skillId))
        assertTrue(next.currency.dust < s.currency.dust)
    }
}
