package com.herobrawl.game.data

import com.herobrawl.game.model.ItemKind

data class ItemDef(
    val id: String,
    val kind: ItemKind,
    val name: String,
    val description: String,
    val emoji: String,
    val rarity: Int, // 1..5
    val usable: Boolean,
)

object Items {
    val all: List<ItemDef> = listOf(
        ItemDef("heroic_scroll", ItemKind.CONSUMABLE_SCROLL_HEROIC, "Heroic Scroll",
            "Spend 1 to summon a hero from the Forge of Legends.", "📜", 4, usable = false),
        ItemDef("basic_scroll", ItemKind.CONSUMABLE_SCROLL_BASIC, "Basic Scroll",
            "Cheaper pulls, mostly 1–3★ heroes; useful as fodder.", "📃", 2, usable = false),
        ItemDef("prophet_orb", ItemKind.CONSUMABLE_PROPHET_ORB, "Prophet Orb",
            "Use at the Oracle's Tree for faction-targeted summons.", "🔮", 4, usable = false),
        ItemDef("xp_small", ItemKind.XP_POTION_SMALL, "Veteran's Elixir",
            "Grants 1,000 XP to a selected hero.", "🧴", 2, usable = true),
        ItemDef("xp_large", ItemKind.XP_POTION_LARGE, "Grand Elixir",
            "Grants 5,000 XP to a selected hero.", "⚗️", 3, usable = true),
        ItemDef("energy_drink", ItemKind.ENERGY_DRINK, "Battle Rations",
            "+1 arena ticket when consumed.", "🍶", 2, usable = true),
        ItemDef("chest_common", ItemKind.CHEST_COMMON, "Common Chest",
            "Rolls for gold and a small chance at stone fragments.", "📦", 2, usable = true),
        ItemDef("chest_rare", ItemKind.CHEST_RARE, "Rare Chest",
            "Rolls for gold, fragments, and a chance at a scroll.", "🎁", 4, usable = true),
        ItemDef("chest_legendary", ItemKind.CHEST_LEGENDARY, "Legendary Chest",
            "Always gives scrolls + fragments + gems.", "🪆", 5, usable = true),
        ItemDef("skin_ticket", ItemKind.SKIN_TICKET, "Skin Ticket",
            "Future: spend at the Tavern for a random hero skin.", "🎨", 4, usable = false),
        ItemDef("avatar_ticket", ItemKind.AVATAR_TICKET, "Avatar Ticket",
            "Unlock any avatar icon or frame of your choice.", "🖼️", 4, usable = true),
        ItemDef("gift_box", ItemKind.GIFT_BOX, "Gift Box",
            "A friendly gift from the HeroBrawl team.", "💝", 3, usable = true),
    )

    val byId: Map<String, ItemDef> = all.associateBy { it.id }
    fun byKind(kind: ItemKind): ItemDef? = all.firstOrNull { it.kind == kind }
}
