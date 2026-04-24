package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.MailMessage
import com.herobrawl.game.model.MailReward

object MailEngine {
    fun send(state: GameState, msg: MailMessage): GameState =
        state.copy(mail = state.mail.copy(messages = listOf(msg) + state.mail.messages))

    fun claim(state: GameState, id: String): GameState {
        val msg = state.mail.messages.firstOrNull { it.id == id } ?: return state
        if (msg.claimed) return state
        var s = state
        for (r in msg.rewards) s = applyReward(s, r)
        val newMessages = state.mail.messages.map { if (it.id == id) it.copy(claimed = true) else it }
        return s.copy(mail = s.mail.copy(messages = newMessages))
    }

    fun claimAll(state: GameState): GameState {
        var s = state
        for (m in state.mail.messages) if (!m.claimed) s = claim(s, m.id)
        return s
    }

    fun delete(state: GameState, id: String): GameState =
        state.copy(mail = state.mail.copy(messages = state.mail.messages.filterNot { it.id == id }))

    fun deleteClaimed(state: GameState): GameState =
        state.copy(mail = state.mail.copy(messages = state.mail.messages.filterNot { it.claimed }))

    private fun applyReward(state: GameState, r: MailReward): GameState = when (r.kind) {
        "gems" -> state.copy(currency = state.currency.copy(gems = state.currency.gems + r.amount))
        "gold" -> state.copy(currency = state.currency.copy(gold = state.currency.gold + r.amount))
        "heroicScrolls" -> state.copy(currency = state.currency.copy(heroicScrolls = state.currency.heroicScrolls + r.amount.toInt()))
        "prophetOrbs" -> state.copy(currency = state.currency.copy(prophetOrbs = state.currency.prophetOrbs + r.amount))
        "stoneFragments" -> state.copy(currency = state.currency.copy(stoneFragments = state.currency.stoneFragments + r.amount))
        "dust" -> state.copy(currency = state.currency.copy(dust = state.currency.dust + r.amount))
        "item" -> r.itemId?.let { Inventory.add(state, it, r.amount.toInt()) } ?: state
        else -> state
    }

    fun unclaimedCount(state: GameState): Int = state.mail.messages.count { !it.claimed }
}
