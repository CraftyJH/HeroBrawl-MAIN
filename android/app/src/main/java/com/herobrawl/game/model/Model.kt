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
)

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

@Serializable
data class Settings(val sound: Boolean = true, val haptics: Boolean = true)

@Serializable
data class GameState(
    val version: Int = 2,
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
)
