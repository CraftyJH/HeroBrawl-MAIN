package com.herobrawl.game.model

import kotlinx.serialization.Serializable

// ============================================================================
//  Core enums & value types
// ============================================================================

@Serializable
enum class FactionId { VANGUARD, HORDE, WILDWOOD, ARCANE, RADIANCE, ABYSS }

@Serializable
enum class ClassId { GUARDIAN, BERSERKER, MAGE, ASSASSIN, RANGER, CLERIC }

@Serializable
enum class StoneKind { ATTACK, HEALTH, SPEED, ENERGY, CRIT }

@Serializable
enum class StatusKind { BURN, POISON, STUN, SHIELD, REGEN, ATTACK_UP, ARMOR_BREAK }

@Serializable
data class HeroStats(
    val attack: Int,
    val health: Int,
    val armor: Int,
    val speed: Int,
    val willpower: Int,
)

@Serializable
data class StatusEffect(
    val kind: StatusKind,
    val duration: Int,
    val magnitude: Double,
)

@Serializable
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val active: Boolean,
    val energyCost: Int = 0,
    val cooldown: Int = 0,
    val power: Double = 1.0,
    val aoe: Boolean = false,
    val target: SkillTarget = SkillTarget.ENEMY,
    val status: StatusEffect? = null,
)

@Serializable
enum class SkillTarget { ENEMY, ALLY, SELF, ALL_ENEMIES, ALL_ALLIES }

// ============================================================================
//  Faction / Class definitions
// ============================================================================

@Serializable
data class Faction(
    val id: FactionId,
    val name: String,
    val color: Long,
    val accent: Long,
    val lore: String,
    val strongVs: List<FactionId>,
)

@Serializable
data class HeroClass(
    val id: ClassId,
    val name: String,
    val role: String,
    val description: String,
    val baseStats: HeroStats,
)

// ============================================================================
//  Heroes
// ============================================================================

@Serializable
data class HeroTemplate(
    val id: String,
    val name: String,
    val title: String,
    val faction: FactionId,
    val heroClass: ClassId,
    val baseRarity: Int, // 1..5
    val emoji: String,
    val portraitGradient: List<Long>, // [start, end] as ARGB Long
    val signatureColor: Long,
    val bio: String,
    val skills: List<Skill>,
)

@Serializable
data class HeroInstance(
    val instanceId: String,
    val templateId: String,
    val level: Int = 1,
    val stars: Int = 1,
    val xp: Int = 0,
    val echoStacks: Int = 0,
    val gearTier: Int = 0,
    val skillLevels: Map<String, Int> = emptyMap(),
    val equippedStones: Map<String, StoneKind> = emptyMap(), // slot -> stone
)

// ============================================================================
//  Persistent game state
// ============================================================================

@Serializable
data class Currency(
    val gold: Long = 5_000,
    val gems: Long = 500,
    val spirit: Long = 0,
    val prophetOrbs: Long = 0,
    val heroicScrolls: Int = 10,
    val basicScrolls: Int = 20,
    val shards: Long = 0,
    val dust: Long = 0,
    val stoneFragments: Long = 0,
    val eventTokens: Long = 0,
    val vipXp: Long = 0, // accumulated gem spending drives VIP level
)

// ============================================================================
//  Cosmetics: Avatars & Frames
// ============================================================================

@Serializable
enum class AvatarKind { HERO_PORTRAIT, ICON }

@Serializable
data class AvatarId(val kind: AvatarKind, val value: String) {
    companion object {
        fun hero(templateId: String) = AvatarId(AvatarKind.HERO_PORTRAIT, templateId)
        fun icon(iconId: String) = AvatarId(AvatarKind.ICON, iconId)
    }
}

@Serializable
data class CosmeticsState(
    val avatar: AvatarId = AvatarId.icon("default"),
    val frame: String = "wooden",
    val unlockedAvatars: Set<String> = setOf("ICON:default"),
    val unlockedFrames: Set<String> = setOf("wooden"),
)

// ============================================================================
//  Items & Inventory (non-hero collectibles)
// ============================================================================

@Serializable
enum class ItemKind {
    CONSUMABLE_SCROLL_HEROIC, CONSUMABLE_SCROLL_BASIC, CONSUMABLE_PROPHET_ORB,
    XP_POTION_SMALL, XP_POTION_LARGE, ENERGY_DRINK,
    CHEST_COMMON, CHEST_RARE, CHEST_LEGENDARY,
    SKIN_TICKET, AVATAR_TICKET,
    GIFT_BOX,
}

@Serializable
data class Item(
    val id: String,
    val kind: ItemKind,
    val count: Int,
)

@Serializable
data class Inventory(val items: Map<String, Int> = emptyMap())

@Serializable
data class IdleRates(val gold: Long, val spirit: Long, val shards: Long)

@Serializable
data class IdleState(
    val startedAt: Long,
    val tickedAt: Long,
    val ratePerHour: IdleRates,
    val capHours: Int = 12,
)

@Serializable
data class Lineup(val slots: List<String?> = List(5) { null })

@Serializable
data class CampaignProgress(val chapter: Int = 1, val stage: Int = 1)

@Serializable
data class ArenaState(
    val rating: Int = 1000,
    val wins: Int = 0,
    val losses: Int = 0,
    val ticketsUsed: Int = 0,
    val ticketDay: String = "",
)

@Serializable
data class QuestState(
    val day: String = "",
    val progress: Map<String, Int> = emptyMap(),
    val claimed: Map<String, Boolean> = emptyMap(),
    val completionClaimed: Boolean = false,
)

@Serializable
data class GachaState(
    val heroicPulls: Int = 0,
    val pityCount: Int = 0,
    val sinceEpic: Int = 0,
)

@Serializable
data class AchievementState(
    val unlocked: Set<String> = emptySet(),
    val claimed: Set<String> = emptySet(),
)

@Serializable
data class EventState(
    val activeEventId: String = "dawnbloom_festival",
    val tokensEarned: Long = 0,
    val milestonesClaimed: Set<String> = emptySet(),
)

@Serializable
data class GameMessage(
    val id: String,
    val at: Long,
    val text: String,
    val kind: String, // info/success/warn/reward
)

// ============================================================================
//  VIP / Shop / Mail
// ============================================================================

@Serializable
data class VipState(
    val level: Int = 0,
    val monthlyCardExpiresAt: Long = 0L,
    val dailyGemsClaimedAt: Long = 0L,
)

@Serializable
data class ShopState(
    val dailyDealsGenAt: Long = 0L,
    val purchases: Map<String, Int> = emptyMap(), // packId -> count owned/bought
    val lastDaily: String = "",
)

@Serializable
data class MailMessage(
    val id: String,
    val sentAt: Long,
    val sender: String,
    val subject: String,
    val body: String,
    val rewards: List<MailReward> = emptyList(),
    val claimed: Boolean = false,
)

@Serializable
data class MailReward(val kind: String, val amount: Long = 0, val itemId: String? = null)

@Serializable
data class MailState(val messages: List<MailMessage> = emptyList())

// ============================================================================
//  Town Buildings (new iteration layer)
// ============================================================================

@Serializable
enum class BuildingId {
    CASTLE, SUMMONING_CIRCLE, ARENA, CAMPAIGN_GATE,
    BLACKSMITH, MARKET, MAILBOX, TAVERN, EVENT_PAVILION,
}

@Serializable
data class BuildingState(
    val levels: Map<String, Int> = emptyMap(), // buildingId.name -> level
)

@Serializable
data class Settings(val sound: Boolean = true, val haptics: Boolean = true)

@Serializable
data class GameState(
    val version: Int = 3,
    val createdAt: Long,
    val playerName: String = "Champion",
    val playerLevel: Int = 1,
    val playerXp: Int = 0,
    val currency: Currency = Currency(),
    val heroes: List<HeroInstance> = emptyList(),
    val lineup: Lineup = Lineup(),
    val idle: IdleState,
    val campaign: CampaignProgress = CampaignProgress(),
    val arena: ArenaState = ArenaState(),
    val quests: QuestState = QuestState(),
    val gacha: GachaState = GachaState(),
    val achievements: AchievementState = AchievementState(),
    val events: EventState = EventState(),
    val messages: List<GameMessage> = emptyList(),
    val settings: Settings = Settings(),
    val firstRunClaimed: Boolean = false,
    val cosmetics: CosmeticsState = CosmeticsState(),
    val inventory: Inventory = Inventory(),
    val vip: VipState = VipState(),
    val shop: ShopState = ShopState(),
    val mail: MailState = MailState(),
    val buildings: BuildingState = BuildingState(),
)
