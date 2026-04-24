package com.herobrawl.game.engine

import com.herobrawl.game.data.Factions
import com.herobrawl.game.model.FactionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FactionsTest {
    @Test
    fun `vanguard beats horde`() {
        assertTrue(Factions.advantage(FactionId.VANGUARD, FactionId.HORDE) > 0)
    }

    @Test
    fun `horde suffers vs vanguard`() {
        assertTrue(Factions.advantage(FactionId.HORDE, FactionId.VANGUARD) < 0)
    }

    @Test
    fun `five of a kind grants mono aura`() {
        val a = Factions.lineupAura(List(5) { FactionId.ABYSS })
        assertTrue(a.attack > 0)
    }

    @Test
    fun `dawnfall pact activates`() {
        val a = Factions.lineupAura(
            listOf(FactionId.RADIANCE, FactionId.RADIANCE, FactionId.ABYSS, FactionId.ABYSS, FactionId.VANGUARD)
        )
        assertEquals(0.18, a.attack, 0.001)
    }
}
