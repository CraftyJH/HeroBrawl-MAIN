package com.herobrawl.game.data

data class AvatarIcon(
    val id: String,
    val emoji: String,
    val name: String,
    val gradient: Pair<Long, Long>,
    val unlockHint: String = "Default",
)

data class Frame(
    val id: String,
    val name: String,
    val stroke: Long, // ARGB Long
    val accent: Long,
    val width: Int, // dp stroke
    val unlockHint: String,
)

object Cosmetics {
    val defaultIcons: List<AvatarIcon> = listOf(
        AvatarIcon("default", "⚔️", "Champion",     0xFF2B1948 to 0xFFFF4F9D),
        AvatarIcon("flame",   "🔥", "Flameborn",    0xFF6B1D0A to 0xFFFF8A3D),
        AvatarIcon("moon",    "🌙", "Moonwalker",   0xFF1F4A20 to 0xFF8AD65F),
        AvatarIcon("spark",   "✨", "Sparkwright",  0xFF4A1F70 to 0xFFB47DFF),
        AvatarIcon("sun",     "☀️", "Sunspeaker",   0xFF6E5B0A to 0xFFFFD966, unlockHint = "Achievement"),
        AvatarIcon("skull",   "💀", "Duskborn",     0xFF5B0F2A to 0xFFE44A7A, unlockHint = "Reach VIP 2"),
        AvatarIcon("duskwing","🦇", "Duskwing",     0xFF2C0A33 to 0xFF8B3DD1, unlockHint = "VIP 2"),
        AvatarIcon("emberwing","🔥", "Emberwing",   0xFF8B1E00 to 0xFFFFB54A, unlockHint = "VIP 7"),
        AvatarIcon("starforged","⭐","Starforged",   0xFF101330 to 0xFFFFD04A, unlockHint = "VIP 10"),
        AvatarIcon("crown",   "👑", "Crowned",      0xFF4E3000 to 0xFFFFD04A, unlockHint = "Reach Arena 1500"),
    )

    val frames: List<Frame> = listOf(
        Frame("wooden",   "Wooden",    0xFF6D4A2A, 0xFFB07A4A, 2, unlockHint = "Default"),
        Frame("iron",     "Iron",      0xFFA0A6B8, 0xFFDDE0EF, 2, unlockHint = "Clear Chapter 2"),
        Frame("gold",     "Gold",      0xFFFFD04A, 0xFFFFE89E, 3, unlockHint = "Own a 5★ hero"),
        Frame("rose_gold","Rose Gold", 0xFFFFA1B4, 0xFFFFD6DE, 3, unlockHint = "VIP 1"),
        Frame("iron_crown","Iron Crown",0xFFB1B4C9, 0xFFFFD04A, 3, unlockHint = "VIP 3"),
        Frame("aurora",   "Aurora",    0xFF7DF5C5, 0xFF5EE3FF, 3, unlockHint = "VIP 5"),
        Frame("celestial","Celestial", 0xFF6B61FF, 0xFFFF4F9D, 4, unlockHint = "VIP 9"),
        Frame("starforged","Starforged",0xFFFFD04A, 0xFFFF4F9D, 4, unlockHint = "VIP 10"),
    )

    val iconsById: Map<String, AvatarIcon> = defaultIcons.associateBy { it.id }
    val framesById: Map<String, Frame> = frames.associateBy { it.id }

    fun isFrameUnlocked(frameId: String, unlockedFrames: Set<String>, vipLevel: Int, arenaRating: Int, heroes: List<com.herobrawl.game.model.HeroInstance>, campaignChapter: Int): Boolean {
        if (frameId in unlockedFrames) return true
        return when (frameId) {
            "wooden" -> true
            "iron" -> campaignChapter >= 3
            "gold" -> heroes.any { it.stars >= 5 }
            "rose_gold" -> vipLevel >= 1
            "iron_crown" -> vipLevel >= 3
            "aurora" -> vipLevel >= 5
            "celestial" -> vipLevel >= 9
            "starforged" -> vipLevel >= 10
            else -> false
        }
    }

    fun isIconUnlocked(iconId: String, unlockedIcons: Set<String>, vipLevel: Int, arenaRating: Int, heroes: List<com.herobrawl.game.model.HeroInstance>): Boolean {
        if ("ICON:$iconId" in unlockedIcons) return true
        return when (iconId) {
            "default", "flame", "moon", "spark" -> true
            "skull", "duskwing" -> vipLevel >= 2
            "emberwing" -> vipLevel >= 7
            "starforged" -> vipLevel >= 10
            "sun" -> heroes.any { it.stars >= 5 }
            "crown" -> arenaRating >= 1500
            else -> false
        }
    }
}
