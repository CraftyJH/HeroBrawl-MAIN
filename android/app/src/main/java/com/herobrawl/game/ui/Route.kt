package com.herobrawl.game.ui

/**
 * Screens the bottom nav can switch to. The 5 primary destinations map to the
 * 5 bottom-nav icons (Castle, Bag, Summon, Shop, Swords). Secondary screens
 * like Mail, AvatarPicker, Events, Achievements, Guide are pushed on top.
 */
sealed class Route(val title: String) {
    // Primary (tab) destinations
    object Castle : Route("Castle")
    object Inventory : Route("Inventory")
    object Summon : Route("Summon")
    object Shop : Route("Shop")
    object Battles : Route("Battles")

    // Secondary (push) destinations
    object Mail : Route("Mail")
    object AvatarPicker : Route("Profile")
    object Campaign : Route("Campaign")
    object Arena : Route("Arena")
    object Events : Route("Events")
    object Quests : Route("Quests")
    object Achievements : Route("Medals")
    object Vip : Route("VIP")
    object Guide : Route("Guide")
}

enum class BottomTab(val route: Route, val label: String, val emoji: String) {
    CASTLE(Route.Castle, "Castle", "🏰"),
    INVENTORY(Route.Inventory, "Bag", "🎒"),
    SUMMON(Route.Summon, "Summon", "✨"),
    SHOP(Route.Shop, "Shop", "🛒"),
    BATTLES(Route.Battles, "Battles", "⚔️"),
}
