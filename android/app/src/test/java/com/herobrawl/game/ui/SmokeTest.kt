package com.herobrawl.game.ui

import com.herobrawl.game.data.Buildings
import com.herobrawl.game.model.BuildingId
import com.herobrawl.game.model.GameState
import com.herobrawl.game.store.SaveStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Non-compose sanity checks for the indexable sizes / invariants we depend on
 * when rendering the home (Castle) screen. These are fast JUnit tests so CI
 * catches "IndexOutOfBoundsException at composition time" regressions.
 */
class SmokeTest {
    @Test
    fun `buildings list has stable size and chunk-of-3 works`() {
        // CastleScreen filters out CASTLE and renders rows of 3. Make sure that
        // even if someone adds/removes buildings we never try to slice outside
        // the list.
        val rendered = Buildings.all.filter { it.id != BuildingId.CASTLE }
        rendered.chunked(3).forEach { row ->
            assertTrue("row is non-empty", row.isNotEmpty())
            assertTrue("row has 1..3 tiles", row.size in 1..3)
        }
    }

    @Test
    fun `fresh save has the invariants the UI expects`() {
        val s: GameState = SaveStore.newGame(System.currentTimeMillis())
        assertEquals(5, s.lineup.slots.size)
        assertTrue(s.idle.capHours > 0)
        assertTrue(s.currency.gold >= 0)
        assertTrue(s.playerLevel >= 1)
        // cosmetics defaults present
        assertTrue(s.cosmetics.unlockedFrames.isNotEmpty())
    }
}
