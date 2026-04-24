package com.herobrawl.game.engine

import com.herobrawl.game.data.Items
import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.ItemKind

object Inventory {
    fun add(state: GameState, itemId: String, count: Int = 1): GameState {
        val cur = state.inventory.items[itemId] ?: 0
        val next = state.inventory.items + (itemId to cur + count)
        return state.copy(inventory = state.inventory.copy(items = next))
    }

    fun remove(state: GameState, itemId: String, count: Int = 1): GameState {
        val cur = state.inventory.items[itemId] ?: 0
        val left = (cur - count).coerceAtLeast(0)
        val next = if (left == 0) state.inventory.items - itemId
        else state.inventory.items + (itemId to left)
        return state.copy(inventory = state.inventory.copy(items = next))
    }

    fun count(state: GameState, itemId: String): Int = state.inventory.items[itemId] ?: 0

    /**
     * Consume a stack of the given kind. Delegates to Inventory.remove and applies
     * the item's gameplay effect. Returns the new state (unchanged if nothing to use).
     */
    fun use(state: GameState, itemId: String, heroInstanceId: String? = null): GameState {
        val def = Items.byId[itemId] ?: return state
        if (!def.usable) return state
        if (count(state, itemId) <= 0) return state
        val base = remove(state, itemId)
        return when (def.kind) {
            ItemKind.XP_POTION_SMALL -> heroInstanceId?.let { Progression.giveXp(base, it, 1_000) } ?: base
            ItemKind.XP_POTION_LARGE -> heroInstanceId?.let { Progression.giveXp(base, it, 5_000) } ?: base
            ItemKind.ENERGY_DRINK ->
                base.copy(arena = base.arena.copy(ticketsUsed = (base.arena.ticketsUsed - 1).coerceAtLeast(0)))
            ItemKind.CHEST_COMMON -> base.copy(
                currency = base.currency.copy(
                    gold = base.currency.gold + 25_000,
                    stoneFragments = base.currency.stoneFragments + 40,
                )
            )
            ItemKind.CHEST_RARE -> base.copy(
                currency = base.currency.copy(
                    gold = base.currency.gold + 100_000,
                    stoneFragments = base.currency.stoneFragments + 150,
                    heroicScrolls = base.currency.heroicScrolls + 1,
                )
            )
            ItemKind.CHEST_LEGENDARY -> base.copy(
                currency = base.currency.copy(
                    gold = base.currency.gold + 300_000,
                    stoneFragments = base.currency.stoneFragments + 400,
                    heroicScrolls = base.currency.heroicScrolls + 3,
                    gems = base.currency.gems + 100,
                )
            )
            ItemKind.GIFT_BOX -> base.copy(
                currency = base.currency.copy(
                    gems = base.currency.gems + 50,
                    heroicScrolls = base.currency.heroicScrolls + 1,
                )
            )
            ItemKind.AVATAR_TICKET -> base // handled in AvatarPicker flow
            else -> base
        }
    }
}
