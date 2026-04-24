package com.herobrawl.game.engine

import com.herobrawl.game.data.Items
import com.herobrawl.game.model.MailMessage
import com.herobrawl.game.model.MailReward
import com.herobrawl.game.store.SaveStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MailInventoryTest {
    @Test
    fun `claim mail grants rewards`() {
        val s = SaveStore.newGame(0L)
        val m = MailMessage(
            id = "m1", sentAt = 0L, sender = "Test", subject = "S", body = "B",
            rewards = listOf(MailReward("gems", 100), MailReward("gold", 5000)),
        )
        val withMail = MailEngine.send(s, m)
        val claimed = MailEngine.claim(withMail, "m1")
        assertEquals(s.currency.gems + 100, claimed.currency.gems)
        assertEquals(s.currency.gold + 5000, claimed.currency.gold)
        assertTrue(claimed.mail.messages.first().claimed)
    }

    @Test
    fun `use XP potion levels the targeted hero`() {
        val template = com.herobrawl.game.data.Heroes.all.first()
        val inst = com.herobrawl.game.model.HeroInstance(
            instanceId = "h1", templateId = template.id, stars = 5,
        )
        val s = SaveStore.newGame(0L).copy(heroes = listOf(inst))
        val withItem = Inventory.add(s, "xp_large", 1)
        val used = Inventory.use(withItem, "xp_large", heroInstanceId = "h1")
        assertEquals(0, Inventory.count(used, "xp_large"))
        assertTrue(used.heroes.first().level >= 1)
    }

    @Test
    fun `open legendary chest grants scrolls and gems`() {
        val s = SaveStore.newGame(0L)
        val withChest = Inventory.add(s, "chest_legendary", 1)
        val used = Inventory.use(withChest, "chest_legendary")
        assertTrue(used.currency.heroicScrolls > s.currency.heroicScrolls)
        assertTrue(used.currency.gems > s.currency.gems)
    }

    @Test
    fun `daily login sends mail`() {
        val s = SaveStore.newGame(0L)
        val after = DailyLogin.checkIn(s, 24L * 3600 * 1000)
        assertTrue(after.mail.messages.isNotEmpty())
        val again = DailyLogin.checkIn(after, 24L * 3600 * 1000 + 1000)
        // same day → no new mail
        assertEquals(after.mail.messages.size, again.mail.messages.size)
    }
}
