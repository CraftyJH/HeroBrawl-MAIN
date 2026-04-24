package com.herobrawl.game.data

import com.herobrawl.game.model.BuildingId

data class BuildingDef(
    val id: BuildingId,
    val name: String,
    val emoji: String,
    val description: String,
    val tag: String, // short screen-label for home scene ("Campaign", "Arena", etc.)
    val route: String,
    val accent: Long,
)

/** The home/castle screen shows these as tappable icon "buildings". */
object Buildings {
    val all: List<BuildingDef> = listOf(
        BuildingDef(BuildingId.CASTLE, "Castle Keep", "🏰", "Your base of operations.", "Home", "home", 0xFFFFD04A),
        BuildingDef(BuildingId.CAMPAIGN_GATE, "War Gate", "🏯", "Enter the campaign.", "Campaign", "campaign", 0xFFFF4F9D),
        BuildingDef(BuildingId.ARENA, "Coliseum", "⚔️", "PvP glory.", "Arena", "arena", 0xFFB47DFF),
        BuildingDef(BuildingId.SUMMONING_CIRCLE, "Summoning Circle", "✨", "Pull new heroes.", "Summon", "summon", 0xFF5EE3FF),
        BuildingDef(BuildingId.BLACKSMITH, "Blacksmith", "⚒️", "Upgrade gear and stones.", "Forge", "roster", 0xFFFF8A3D),
        BuildingDef(BuildingId.MARKET, "Market", "🏪", "Spend gold and gems.", "Shop", "shop", 0xFFFFD966),
        BuildingDef(BuildingId.MAILBOX, "Mailbox", "📬", "Claim rewards and news.", "Mail", "mail", 0xFF8AD65F),
        BuildingDef(BuildingId.TAVERN, "Tavern", "🍻", "Hero bios and skins.", "Tavern", "roster", 0xFFC4B4FF),
        BuildingDef(BuildingId.EVENT_PAVILION, "Event Pavilion", "🎉", "Seasonal milestones.", "Events", "events", 0xFFFF7AA3),
    )

    val byId: Map<BuildingId, BuildingDef> = all.associateBy { it.id }
}
