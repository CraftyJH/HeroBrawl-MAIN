package com.herobrawl.game.engine

import com.herobrawl.game.data.Shop
import com.herobrawl.game.store.SaveStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VipShopTest {
    @Test
    fun `buying gem pack grants gems + VIP xp`() {
        val s = SaveStore.newGame(0L)
        val pack = Shop.gemPacks.first()
        val next = ShopEngine.buyGemPack(s, pack, 0L)
        assertEquals(s.currency.gems + pack.gems + pack.bonusGems, next.currency.gems)
        assertTrue(next.currency.vipXp >= pack.vipXp)
    }

    @Test
    fun `enough vip xp levels up`() {
        val s = SaveStore.newGame(0L)
        var n = VipEngine.addVipXp(s, 5_000) // crosses tiers 1..5
        assertTrue(n.vip.level >= 5)
    }

    @Test
    fun `monthly card activates`() {
        val s = SaveStore.newGame(0L)
        val deal = Shop.packDeals.first { it.isMonthlyCard }
        val n = ShopEngine.buyPackDeal(s, deal, 1_000L)
        assertTrue(VipEngine.monthlyCardActive(n, 1_000L))
    }
}
