package com.herobrawl.game.data

import com.herobrawl.game.model.Faction
import com.herobrawl.game.model.FactionId

object Factions {
    val all: Map<FactionId, Faction> = mapOf(
        FactionId.VANGUARD to Faction(
            FactionId.VANGUARD, "Vanguard", 0xFF4AA3FF, 0xFF82C4FF,
            "Disciplined knights of the crystal citadel.",
            listOf(FactionId.HORDE)
        ),
        FactionId.HORDE to Faction(
            FactionId.HORDE, "Horde", 0xFFFF8A3D, 0xFFFFB370,
            "Tribal warriors from the scorched badlands.",
            listOf(FactionId.WILDWOOD)
        ),
        FactionId.WILDWOOD to Faction(
            FactionId.WILDWOOD, "Wildwood", 0xFF8AD65F, 0xFFB6EA8F,
            "Druids and beastkin of the old forests.",
            listOf(FactionId.ARCANE)
        ),
        FactionId.ARCANE to Faction(
            FactionId.ARCANE, "Arcane", 0xFFB47DFF, 0xFFD3B1FF,
            "Rune-etched sorcerers and starborn scholars.",
            listOf(FactionId.VANGUARD)
        ),
        FactionId.RADIANCE to Faction(
            FactionId.RADIANCE, "Radiance", 0xFFFFD966, 0xFFFFE89E,
            "Seraphic champions of the dawn.",
            listOf(FactionId.ABYSS)
        ),
        FactionId.ABYSS to Faction(
            FactionId.ABYSS, "Abyss", 0xFFE44A7A, 0xFFFF7AA3,
            "Infernal lords and shadow-sworn sovereigns.",
            listOf(FactionId.RADIANCE)
        ),
    )

    data class Aura(
        val label: String,
        val attack: Double,
        val health: Double,
        val speed: Double,
    )

    fun lineupAura(factions: List<FactionId>): Aura {
        if (factions.isEmpty()) return Aura("Empty lineup", 0.0, 0.0, 0.0)
        val counts = factions.groupingBy { it }.eachCount()
        val max = counts.maxByOrNull { it.value }
        val rad = counts[FactionId.RADIANCE] ?: 0
        val aby = counts[FactionId.ABYSS] ?: 0
        if (rad >= 2 && aby >= 2 && factions.size == 5) {
            return Aura("Dawnfall Pact · 2 Radiance + 2 Abyss", 0.18, 0.20, 0.08)
        }
        if (max != null && max.value == 5) {
            return when (max.key) {
                FactionId.VANGUARD -> Aura("Mono-Vanguard Aura", 0.10, 0.30, 0.00)
                FactionId.HORDE -> Aura("Mono-Horde Aura", 0.22, 0.10, 0.05)
                FactionId.WILDWOOD -> Aura("Mono-Wildwood Aura", 0.10, 0.20, 0.12)
                FactionId.ARCANE -> Aura("Mono-Arcane Aura", 0.25, 0.05, 0.05)
                FactionId.RADIANCE -> Aura("Mono-Radiance Aura", 0.12, 0.20, 0.08)
                FactionId.ABYSS -> Aura("Mono-Abyss Aura", 0.20, 0.15, 0.08)
            }
        }
        if (max != null && max.value == 4) {
            val name = all.getValue(max.key).name
            return Aura("$name Bond (4)", 0.08, 0.10, 0.03)
        }
        return Aura("Balanced lineup", 0.0, 0.0, 0.0)
    }

    fun advantage(attacker: FactionId, defender: FactionId): Double = when {
        defender in all.getValue(attacker).strongVs -> 0.20
        attacker in all.getValue(defender).strongVs -> -0.15
        else -> 0.0
    }
}
