package com.herobrawl.game.engine

import com.herobrawl.game.model.GameState
import com.herobrawl.game.model.MailMessage
import com.herobrawl.game.model.MailReward
import java.util.UUID

/**
 * 7-day cumulative login reward cycle. Rewards land in the mailbox so the
 * player is never interrupted by a giant dialog on launch.
 */
object DailyLogin {
    private const val KEY_DAY = "lastLoginDay"
    private const val KEY_STREAK = "loginStreak"

    data class Reward(val title: String, val rewards: List<MailReward>)

    val cycle: List<Reward> = listOf(
        Reward("Day 1",  listOf(MailReward("gems", 40))),
        Reward("Day 2",  listOf(MailReward("heroicScrolls", 1))),
        Reward("Day 3",  listOf(MailReward("prophetOrbs", 5))),
        Reward("Day 4",  listOf(MailReward("gold", 100_000))),
        Reward("Day 5",  listOf(MailReward("stoneFragments", 150))),
        Reward("Day 6",  listOf(MailReward("dust", 100))),
        Reward("Day 7",  listOf(MailReward("heroicScrolls", 5), MailReward("gems", 100))),
    )

    fun dayKey(now: Long): String =
        java.time.LocalDate.ofInstant(java.time.Instant.ofEpochMilli(now), java.time.ZoneOffset.UTC).toString()

    fun checkIn(state: GameState, now: Long): GameState {
        // We store the day-of-year (1..366) × year-offset in quests.progress under KEY_DAY.
        // That fits in an Int and round-trips cleanly; we don't care about the exact numeric
        // value, only that "same day" produces the same key.
        val today = daySlot(now)
        val last = state.quests.progress[KEY_DAY] ?: 0
        if (last == today) return state

        val streak = (state.quests.progress[KEY_STREAK] ?: 0) + 1
        val idx = ((streak - 1).coerceAtLeast(0)) % cycle.size
        val reward = cycle[idx]

        val msg = MailMessage(
            id = UUID.randomUUID().toString(),
            sentAt = now,
            sender = "Keep",
            subject = "Daily sign-in — ${reward.title}",
            body = "You've logged in $streak day${if (streak == 1) "" else "s"} in a row. Enjoy the streak reward!",
            rewards = reward.rewards,
        )
        val progress = state.quests.progress +
            (KEY_DAY to today) +
            (KEY_STREAK to streak)
        return MailEngine.send(
            state.copy(quests = state.quests.copy(progress = progress)),
            msg,
        )
    }

    private fun daySlot(now: Long): Int {
        val d = java.time.LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(now),
            java.time.ZoneOffset.UTC,
        )
        return d.year * 1000 + d.dayOfYear
    }
}
