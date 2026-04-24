package com.herobrawl.game.data

// Two kinds of shop entries:
//   - Gem packs: cost real money, give gems (+ sometimes bonuses + VIP XP).
//   - Gem offers: cost gems, give in-game loot.
// For an offline game these are simulated — pressing "Buy" with money just
// awards the gems (and VIP XP as if the player spent). In a live build we
// swap Gem packs to use Google Play Billing.

data class GemPack(
    val id: String,
    val title: String,
    val price: String,        // formatted USD label
    val gems: Int,
    val bonusGems: Int = 0,
    val vipXp: Long,          // 1 VIP XP ≈ $0.01 (i.e. gems purchased)
    val highlight: String? = null,
    val emoji: String = "💎",
)

data class GemOffer(
    val id: String,
    val title: String,
    val description: String,
    val costGems: Long,
    val emoji: String,
    val rewardKind: String,    // "currency" | "item" | "hero_shard"
    val rewardValue: String,   // key (e.g. "gold", "heroicScrolls", "ev_solara")
    val rewardAmount: Long,
    val dailyLimit: Int = 5,
)

data class PackDeal(
    val id: String,
    val title: String,
    val price: String,
    val vipXp: Long,
    val banner: String,
    val description: String,
    val rewards: List<ShopReward>,
    val limitPerDay: Int = 1,
    val isMonthlyCard: Boolean = false,
)

data class ShopReward(val kind: String, val amount: Long = 0, val label: String)

object Shop {
    val gemPacks: List<GemPack> = listOf(
        GemPack("pack_starter", "Starter Stash", "$0.99", gems = 80, bonusGems = 20, vipXp = 100, emoji = "🪙", highlight = "Starter"),
        GemPack("pack_small",   "Pocket Pouch",  "$4.99", gems = 500, bonusGems = 50, vipXp = 500, emoji = "💎"),
        GemPack("pack_med",     "Hero's Trove",  "$9.99", gems = 1_100, bonusGems = 150, vipXp = 1_000, emoji = "💠"),
        GemPack("pack_large",   "Champion Vault","$19.99", gems = 2_400, bonusGems = 400, vipXp = 2_000, emoji = "👑", highlight = "Popular"),
        GemPack("pack_huge",    "Legend's Hoard","$49.99", gems = 6_500, bonusGems = 1_500, vipXp = 5_000, emoji = "💰"),
        GemPack("pack_mega",    "King's Bounty", "$99.99", gems = 14_000, bonusGems = 4_000, vipXp = 10_000, emoji = "🏆", highlight = "Best value"),
    )

    val packDeals: List<PackDeal> = listOf(
        PackDeal(
            "deal_monthly",
            title = "Monthly Card",
            price = "$4.99",
            vipXp = 500,
            banner = "🗓️",
            description = "300 gems now, then 100 gems delivered to your mailbox every day for 30 days.",
            rewards = listOf(
                ShopReward("gems", 300, "💎 300 now"),
                ShopReward("mail_daily_gems", 100, "💎 100/day for 30d"),
            ),
            limitPerDay = 1,
            isMonthlyCard = true,
        ),
        PackDeal(
            "deal_first",
            title = "Rookie Pack",
            price = "$0.99",
            vipXp = 100,
            banner = "🎁",
            description = "A big boost for new players. Includes scrolls, orbs, stones, and a guaranteed 4★+ hero.",
            rewards = listOf(
                ShopReward("gems", 100, "💎 100"),
                ShopReward("heroicScrolls", 10, "📜 x10"),
                ShopReward("prophetOrbs", 20, "🔮 x20"),
                ShopReward("stoneFragments", 300, "💠 x300"),
                ShopReward("item:CHEST_RARE", 1, "📦 Rare Chest"),
            ),
        ),
        PackDeal(
            "deal_growth",
            title = "Growth Fund",
            price = "$9.99",
            vipXp = 1_000,
            banner = "📈",
            description = "Get returns as you progress. Scales up with your campaign chapters.",
            rewards = listOf(
                ShopReward("gems", 400, "💎 400"),
                ShopReward("mail_chapter_gems", 40, "💎 40 per chapter cleared"),
            ),
        ),
        PackDeal(
            "deal_weekly",
            title = "Weekly Bundle",
            price = "$4.99",
            vipXp = 500,
            banner = "🗡️",
            description = "Great for active players. Expires at week's end.",
            rewards = listOf(
                ShopReward("heroicScrolls", 15, "📜 x15"),
                ShopReward("dust", 100, "🧪 x100"),
                ShopReward("gold", 500_000, "🪙 x500K"),
            ),
            limitPerDay = 7,
        ),
        PackDeal(
            "deal_event",
            title = "Event Supreme",
            price = "$29.99",
            vipXp = 3_000,
            banner = "🎉",
            description = "Limited — adds 400 event tokens and a skin ticket.",
            rewards = listOf(
                ShopReward("eventTokens", 400, "🎟️ 400 tokens"),
                ShopReward("item:SKIN_TICKET", 1, "🎨 Skin Ticket"),
                ShopReward("heroicScrolls", 20, "📜 x20"),
            ),
        ),
    )

    val gemOffers: List<GemOffer> = listOf(
        GemOffer("gem_gold_small",  "Small Gold Sack", "100K gold", costGems = 100, emoji = "🪙",
            rewardKind = "currency", rewardValue = "gold", rewardAmount = 100_000, dailyLimit = 5),
        GemOffer("gem_gold_large",  "Big Gold Sack",   "1M gold",   costGems = 800, emoji = "🪙",
            rewardKind = "currency", rewardValue = "gold", rewardAmount = 1_000_000, dailyLimit = 3),
        GemOffer("gem_scroll_1",    "Heroic Scroll",   "1 summon",  costGems = 300, emoji = "📜",
            rewardKind = "currency", rewardValue = "heroicScrolls", rewardAmount = 1, dailyLimit = 10),
        GemOffer("gem_scroll_10",   "Scroll Bundle",   "10 summons",costGems = 2_700, emoji = "📜",
            rewardKind = "currency", rewardValue = "heroicScrolls", rewardAmount = 10, dailyLimit = 5),
        GemOffer("gem_orb_1",       "Prophet Orb",     "1 orb",     costGems = 180, emoji = "🔮",
            rewardKind = "currency", rewardValue = "prophetOrbs", rewardAmount = 1, dailyLimit = 10),
        GemOffer("gem_frag_big",    "Stone Fragments", "500 💠",     costGems = 400, emoji = "💠",
            rewardKind = "currency", rewardValue = "stoneFragments", rewardAmount = 500, dailyLimit = 5),
        GemOffer("gem_dust",        "Skill Dust",      "200 🧪",     costGems = 250, emoji = "🧪",
            rewardKind = "currency", rewardValue = "dust", rewardAmount = 200, dailyLimit = 5),
        GemOffer("gem_arena_tkt",   "Extra Arena Ticket","+1 🎫",    costGems = 100, emoji = "🎫",
            rewardKind = "currency", rewardValue = "arenaTicket", rewardAmount = 1, dailyLimit = 5),
        GemOffer("gem_chest",       "Rare Chest",      "1 random drop", costGems = 600, emoji = "📦",
            rewardKind = "item", rewardValue = "CHEST_RARE", rewardAmount = 1, dailyLimit = 3),
    )
}
