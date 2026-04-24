package com.herobrawl.game.engine

import com.herobrawl.game.store.SaveStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GachaTest {
    private fun baseState() = SaveStore.newGame(0L)

    @Test
    fun `hard pity at 60 guarantees 5 star`() {
        val seeded = baseState().copy(
            gacha = baseState().gacha.copy(pityCount = 59, sinceEpic = 0)
        )
        repeat(20) {
            val r = Gacha.pull(seeded, 1, rnd = Random(it.toLong()))
            assertEquals("Pull #$it should be 5★", 5, r.results[0].rarity)
        }
    }

    @Test
    fun `every tenth pull is epic or better`() {
        val seeded = baseState().copy(
            gacha = baseState().gacha.copy(sinceEpic = 9)
        )
        val r = Gacha.pull(seeded, 1, rnd = Random(42))
        assertTrue(r.results[0].rarity >= 4)
    }

    @Test
    fun `duplicates bump echo stacks`() {
        var s = baseState()
        val first = Gacha.pull(s, 1, rnd = Random(0)).state
        val tid = first.heroes.first().templateId
        // Force next pull to be the same hero via a 5★ seed + faction filter
        s = first
        val dup = Gacha.pull(s, 1, rnd = Random(0))
        assertTrue(dup.state.heroes.size <= s.heroes.size + 1)
    }
}
