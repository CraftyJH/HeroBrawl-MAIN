package com.herobrawl.game.data

data class VipTier(
    val level: Int,
    val xpRequired: Long, // cumulative VIP XP (≈ gems spent)
    val perks: List<String>,
    val dailyGemsBonus: Int,
    val idleCapBonusHours: Int,
)

/**
 * Small / medium monetization focus — VIP gives quality-of-life, not exclusives.
 * Every VIP perk is either time-save or cosmetic. No heroes are VIP-locked.
 */
object Vip {
    val tiers: List<VipTier> = listOf(
        VipTier(0, 0, listOf("Welcome!"), 0, 0),
        VipTier(1, 50, listOf("+5% gold from idle", "Unlock Rose Gold frame"), 5, 0),
        VipTier(2, 200, listOf("+10% gold from idle", "2x battle speed", "Unlock Duskwing avatar"), 10, 1),
        VipTier(3, 500, listOf("+15% idle rates", "Auto-claim idle every 4h", "Unlock Iron Crown frame"), 20, 2),
        VipTier(4, 1_200, listOf("+20% idle rates", "+1 arena ticket", "Custom chat color"), 35, 2),
        VipTier(5, 3_000, listOf("+25% idle rates", "+2 arena tickets", "Unlock Aurora frame"), 60, 3),
        VipTier(6, 7_500, listOf("+30% idle rates", "Exclusive daily pack (gems)"), 100, 3),
        VipTier(7, 15_000, listOf("+40% idle rates", "Unlock Emberwing avatar"), 160, 4),
        VipTier(8, 30_000, listOf("+50% idle rates", "Double monthly card value"), 250, 5),
        VipTier(9, 60_000, listOf("+60% idle rates", "Unlock Celestial frame"), 400, 6),
        VipTier(10, 120_000, listOf("+75% idle rates", "Unlock Starforged avatar + frame"), 600, 8),
    )

    fun tierFor(level: Int): VipTier = tiers.getOrNull(level.coerceIn(0, tiers.size - 1)) ?: tiers[0]
    fun levelForXp(xp: Long): Int {
        var lvl = 0
        for (t in tiers) if (xp >= t.xpRequired) lvl = t.level
        return lvl
    }
    fun xpToNextLevel(xp: Long): Pair<Long, Long> {
        val cur = levelForXp(xp)
        val next = tiers.firstOrNull { it.level == cur + 1 } ?: return xp to tiers.last().xpRequired
        val baseFloor = tiers.first { it.level == cur }.xpRequired
        return (xp - baseFloor) to (next.xpRequired - baseFloor)
    }
}
