package com.herobrawl.game.data

import com.herobrawl.game.model.ClassId
import com.herobrawl.game.model.FactionId
import com.herobrawl.game.model.HeroTemplate

object Heroes {
    private val gradients: Map<FactionId, List<Pair<Long, Long>>> = mapOf(
        FactionId.VANGUARD to listOf(0xFF1A3A6E to 0xFF4AA3FF, 0xFF2A4D80 to 0xFF6AB6FF),
        FactionId.HORDE to listOf(0xFF6B1D0A to 0xFFFF8A3D, 0xFF7D2A15 to 0xFFFFA55A),
        FactionId.WILDWOOD to listOf(0xFF1F4A20 to 0xFF8AD65F, 0xFF2A5E35 to 0xFFA5E47F),
        FactionId.ARCANE to listOf(0xFF4A1F70 to 0xFFB47DFF, 0xFF5C2D82 to 0xFFCBA0FF),
        FactionId.RADIANCE to listOf(0xFF6E5B0A to 0xFFFFD966, 0xFF7A6820 to 0xFFFFE89E),
        FactionId.ABYSS to listOf(0xFF5B0F2A to 0xFFE44A7A, 0xFF70203D to 0xFFFF7AA3),
    )

    private fun gradient(id: String, fac: FactionId): Pair<Long, Long> {
        val pool = gradients.getValue(fac)
        val h = id.fold(0) { acc, ch -> (acc * 31 + ch.code) and 0x7FFFFFFF }
        return pool[h % pool.size]
    }

    private fun h(
        id: String, name: String, title: String,
        faction: FactionId, cls: ClassId, baseRarity: Int,
        emoji: String, bio: String,
    ): HeroTemplate {
        val g = gradient(id, faction)
        return HeroTemplate(
            id = id, name = name, title = title,
            faction = faction, heroClass = cls, baseRarity = baseRarity,
            emoji = emoji,
            portraitGradient = listOf(g.first, g.second),
            signatureColor = g.second,
            bio = bio,
            skills = Skills.defaultFor(cls),
        )
    }

    val all: List<HeroTemplate> = listOf(
        // --- VANGUARD ---
        h("vn_aldric", "Aldric", "Stormshield", FactionId.VANGUARD, ClassId.GUARDIAN, 5, "🛡️",
            "Master-at-arms of the Crystal Citadel; his tower shield once turned a dragon."),
        h("vn_mira", "Mira", "Lightlance", FactionId.VANGUARD, ClassId.RANGER, 5, "🏹",
            "A markswoman who sanctifies every arrow with a prayer to dawn."),
        h("vn_cassius", "Cassius", "Oathsworn", FactionId.VANGUARD, ClassId.BERSERKER, 4, "⚔️",
            "Broken knight reforged — his rage is always on a leash. Always."),
        h("vn_elara", "Elara", "Lightweaver", FactionId.VANGUARD, ClassId.CLERIC, 4, "✨",
            "Field chaplain; her bandages double as sigils of protection."),
        h("vn_theo", "Theo", "Duelist", FactionId.VANGUARD, ClassId.ASSASSIN, 3, "🗡️",
            "Youngest fencer to best the royal champion. Has a scar, and a smirk."),
        h("vn_brunnhilde", "Brunnhilde", "Bannerlord", FactionId.VANGUARD, ClassId.GUARDIAN, 5, "⚜️",
            "Leads from the front; her banner gives courage to even the dying."),
        h("vn_ysolt", "Ysolt", "Runemage", FactionId.VANGUARD, ClassId.MAGE, 4, "📜",
            "Codex-marked scholar-warrior of the glass libraries."),
        h("vn_pax", "Pax", "Lawbringer", FactionId.VANGUARD, ClassId.CLERIC, 3, "⚖️",
            "Judge, jury, and healer — in that uncomfortable order."),

        // --- HORDE ---
        h("hd_grashk", "Grashk", "Bonebreaker", FactionId.HORDE, ClassId.BERSERKER, 5, "💀",
            "Warchief-blooded. His greatsword is stitched from chieftain bones."),
        h("hd_zin", "Zin'kala", "Ashwalker", FactionId.HORDE, ClassId.MAGE, 5, "🔥",
            "A shaman who speaks to the fires that sleep beneath the badlands."),
        h("hd_ruk", "Ruk", "Tuskguard", FactionId.HORDE, ClassId.GUARDIAN, 4, "🐗",
            "Rides a warboar. The boar has more kills than you."),
        h("hd_shiva", "Shiva", "Feralblade", FactionId.HORDE, ClassId.ASSASSIN, 4, "🗡️",
            "Never takes off her warpaint. Never apologizes."),
        h("hd_orrak", "Orrak", "Skyshot", FactionId.HORDE, ClassId.RANGER, 3, "🏹",
            "Hunts gryphons for fun. Eats them too."),
        h("hd_mamatu", "Mama Tu", "Bonechanter", FactionId.HORDE, ClassId.CLERIC, 4, "🦴",
            "A bone-witch who heals with songs and shaming."),
        h("hd_vargen", "Vargen", "Rimecleaver", FactionId.HORDE, ClassId.BERSERKER, 5, "🪓",
            "Exiled prince who cleaves through blizzards and doubts alike."),
        h("hd_tala", "Tala", "Stormcaller", FactionId.HORDE, ClassId.MAGE, 3, "⚡",
            "Her first word was thunder. Hasn't shut up since."),

        // --- WILDWOOD ---
        h("wd_thalia", "Thalia", "Greenheart", FactionId.WILDWOOD, ClassId.CLERIC, 5, "🌿",
            "Druid-queen of the silver grove. Plants bloom where she sleeps."),
        h("wd_finn", "Finn", "Moonhunter", FactionId.WILDWOOD, ClassId.RANGER, 5, "🌙",
            "Silent hunter with silver arrows made for wolves that shouldn't exist."),
        h("wd_bram", "Bram", "Barkwarden", FactionId.WILDWOOD, ClassId.GUARDIAN, 4, "🌳",
            "Half-treant bodyguard; roots into the earth to hold the line."),
        h("wd_nyx", "Nyx", "Vinewhisper", FactionId.WILDWOOD, ClassId.MAGE, 4, "🪴",
            "Her spellbook has roots and sometimes snarls."),
        h("wd_kade", "Kade", "Fangmark", FactionId.WILDWOOD, ClassId.ASSASSIN, 3, "🐺",
            "Raised by wolves. Literally. Has the table manners to prove it."),
        h("wd_ursa", "Ursa", "Stormpaw", FactionId.WILDWOOD, ClassId.BERSERKER, 4, "🐻",
            "Part bear, entirely angry, surprisingly polite."),
        h("wd_sable", "Sable", "Mistwalker", FactionId.WILDWOOD, ClassId.ASSASSIN, 5, "🍃",
            "She appears in the mist, leaves a blade, and vanishes without a sound."),
        h("wd_rin", "Rin", "Thornsinger", FactionId.WILDWOOD, ClassId.CLERIC, 3, "🥀",
            "Heals bodies with brambles, mends minds with lullabies."),

        // --- ARCANE ---
        h("ar_seraphine", "Seraphine", "Starspeaker", FactionId.ARCANE, ClassId.MAGE, 5, "🌟",
            "Chief astronomer of the Glass Spire; reads destiny in supernovae."),
        h("ar_lumen", "Lumen", "Chronomancer", FactionId.ARCANE, ClassId.MAGE, 5, "⏳",
            "Can stop a raindrop mid-fall. Cannot stop a hangover."),
        h("ar_iron", "Iron", "Golemwright", FactionId.ARCANE, ClassId.GUARDIAN, 4, "🤖",
            "Artificer riding a clockwork warbeast. Both are grumpy."),
        h("ar_veska", "Veska", "Mindblade", FactionId.ARCANE, ClassId.ASSASSIN, 4, "🔮",
            "Kills you with the idea of a dagger first. The real one is a formality."),
        h("ar_kael", "Kael", "Hexmark", FactionId.ARCANE, ClassId.RANGER, 4, "🎯",
            "His crossbow fires guided curses. Some of them love him back."),
        h("ar_opal", "Opal", "Sparkwright", FactionId.ARCANE, ClassId.CLERIC, 3, "💡",
            "Engineer-cleric who patches heroes with light and solder."),
        h("ar_morvain", "Morvain", "Voidreaver", FactionId.ARCANE, ClassId.BERSERKER, 5, "🌀",
            "Split from his shadow; now they're both armed and very tired."),
        h("ar_tess", "Tess", "Glassweaver", FactionId.ARCANE, ClassId.MAGE, 3, "💠",
            "Spins illusions so convincing the enemy sometimes loses to themselves."),

        // --- RADIANCE ---
        h("rd_auriel", "Auriel", "Dawnbringer", FactionId.RADIANCE, ClassId.CLERIC, 5, "☀️",
            "A seraph of first light. Her hymn can knit cracked marble."),
        h("rd_gideon", "Gideon", "Sunspear", FactionId.RADIANCE, ClassId.GUARDIAN, 5, "🛡️",
            "Wields a spear forged from a fallen star's last heartbeat."),
        h("rd_sanna", "Sanna", "Haloshot", FactionId.RADIANCE, ClassId.RANGER, 4, "🏹",
            "Her bow sings when she draws it. Enemies also sing. Briefly."),
        h("rd_micah", "Micah", "Swordsaint", FactionId.RADIANCE, ClassId.BERSERKER, 4, "⚔️",
            "Takes a vow of silence each dawn; speaks only in swordstrokes."),
        h("rd_ember", "Ember", "Firstflame", FactionId.RADIANCE, ClassId.MAGE, 5, "🔥",
            "A spark that became a woman that became a sun that became a problem."),
        h("rd_tama", "Tama", "Veilwatcher", FactionId.RADIANCE, ClassId.ASSASSIN, 3, "👁️",
            "Hunts across the veil for those who hide from judgement."),
        h("rd_noa", "Noa", "Stormwing", FactionId.RADIANCE, ClassId.RANGER, 3, "🕊️",
            "Rides a thunderbird. They are very loud together."),

        // --- ABYSS ---
        h("ab_nyxara", "Nyxara", "Queen of Shards", FactionId.ABYSS, ClassId.MAGE, 5, "🖤",
            "Her crown is a crack in reality. She likes the draft."),
        h("ab_varis", "Varis", "Bloodcaller", FactionId.ABYSS, ClassId.BERSERKER, 5, "🩸",
            "Vampire-knight who duels at dawn to keep himself interesting."),
        h("ab_saelen", "Saelen", "Soulreaver", FactionId.ABYSS, ClassId.ASSASSIN, 5, "💀",
            "Has a library of stolen last words. Alphabetized."),
        h("ab_khorr", "Khorr", "Chainbreaker", FactionId.ABYSS, ClassId.GUARDIAN, 4, "⛓️",
            "Broke out of a hell; dragged the chains with him for souvenirs."),
        h("ab_tira", "Tira", "Witchhound", FactionId.ABYSS, ClassId.RANGER, 4, "🏹",
            "Hunts witches with wolves she enchanted from dogs."),
        h("ab_mordecai", "Mordecai", "Blackbinder", FactionId.ABYSS, ClassId.CLERIC, 4, "📕",
            "Priest of the unfriendly gods. Surprisingly tender bedside manner."),
        h("ab_zeranna", "Zeranna", "Veinwitch", FactionId.ABYSS, ClassId.MAGE, 3, "🩸",
            "Her spells need blood, hers or otherwise. She is not picky."),
        h("ab_dreyve", "Dreyve", "Gravewarden", FactionId.ABYSS, ClassId.GUARDIAN, 3, "⚰️",
            "Stands guard over a tomb no one has opened. Yet."),

        // --- NEW: Limited-banner event heroes (all 5★) ---
        h("ev_solara", "Solara", "Sunsovereign", FactionId.RADIANCE, ClassId.MAGE, 5, "🌞",
            "New Year miracle-hero. Her presence scorches lies from the air."),
        h("ev_koros", "Koros", "Eclipsebringer", FactionId.ABYSS, ClassId.ASSASSIN, 5, "🌒",
            "An event-only assassin who leaves no shadow behind."),
        h("ev_ivorn", "Ivorn", "Rootking", FactionId.WILDWOOD, ClassId.GUARDIAN, 5, "🌲",
            "A walking fortress of oak. Polite to bees."),
    )

    val byId: Map<String, HeroTemplate> = all.associateBy { it.id }
    val byRarity: Map<Int, List<HeroTemplate>> = all.groupBy { it.baseRarity }
}
