import type { HeroTemplate, Rarity } from "../types";
import { CLASS_ACTIVE, CLASS_PASSIVE } from "./skills";

// Build standard skill set for a hero. Special signature skill overrides can be passed in.
function baseSkills(classId: keyof typeof CLASS_ACTIVE) {
  return [CLASS_PASSIVE[classId], CLASS_ACTIVE[classId]];
}

// Portrait gradients per faction (used to color hero cards procedurally)
const GRADIENTS: Record<HeroTemplate["faction"], [string, string][]> = {
  vanguard: [["#1a3a6e", "#4aa3ff"], ["#2a4d80", "#6ab6ff"]],
  horde: [["#6b1d0a", "#ff8a3d"], ["#7d2a15", "#ffa55a"]],
  wildwood: [["#1f4a20", "#8ad65f"], ["#2a5e35", "#a5e47f"]],
  arcane: [["#4a1f70", "#b47dff"], ["#5c2d82", "#cba0ff"]],
  radiance: [["#6e5b0a", "#ffd966"], ["#7a6820", "#ffe89e"]],
  abyss: [["#5b0f2a", "#e44a7a"], ["#70203d", "#ff7aa3"]],
};

function pick<T>(arr: readonly T[], seed: string): T {
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  return arr[h % arr.length];
}

export const HEROES: HeroTemplate[] = [
  // --- VANGUARD ---
  h("vn_aldric", "Aldric", "Stormshield", "vanguard", "guardian", 5, "🛡️", "Master-at-arms of the Crystal Citadel; his tower shield once turned a dragon."),
  h("vn_mira", "Mira", "Lightlance", "vanguard", "ranger", 5, "🏹", "A markswoman who sanctifies every arrow with a prayer to dawn."),
  h("vn_cassius", "Cassius", "Oathsworn", "vanguard", "berserker", 4, "⚔️", "Broken knight reforged — his rage is always on a leash. Always."),
  h("vn_elara", "Elara", "Lightweaver", "vanguard", "cleric", 4, "✨", "Field chaplain; her bandages double as sigils of protection."),
  h("vn_theo", "Theo", "Duelist", "vanguard", "assassin", 3, "🗡️", "Youngest fencer to best the royal champion. Has a scar, and a smirk."),
  h("vn_brunnhilde", "Brunnhilde", "Bannerlord", "vanguard", "guardian", 5, "⚜️", "Leads from the front; her banner gives courage to even the dying."),
  h("vn_ysolt", "Ysolt", "Runemage", "vanguard", "mage", 4, "📜", "Codex-marked scholar-warrior of the glass libraries."),

  // --- HORDE ---
  h("hd_grashk", "Grashk", "Bonebreaker", "horde", "berserker", 5, "💀", "Warchief-blooded. His greatsword is stitched from chieftain bones."),
  h("hd_zin", "Zin'kala", "Ashwalker", "horde", "mage", 5, "🔥", "A shaman who speaks to the fires that sleep beneath the badlands."),
  h("hd_ruk", "Ruk", "Tuskguard", "horde", "guardian", 4, "🐗", "Rides a warboar. The boar has more kills than you."),
  h("hd_shiva", "Shiva", "Feralblade", "horde", "assassin", 4, "🗡️", "Never takes off her warpaint. Never apologizes."),
  h("hd_orrak", "Orrak", "Skyshot", "horde", "ranger", 3, "🏹", "Hunts gryphons for fun. Eats them too."),
  h("hd_mamatu", "Mama Tu", "Bonechanter", "horde", "cleric", 4, "🦴", "A bone-witch who heals with songs and shaming."),
  h("hd_vargen", "Vargen", "Rimecleaver", "horde", "berserker", 5, "🪓", "Exiled prince who cleaves through blizzards and doubts alike."),

  // --- WILDWOOD ---
  h("wd_thalia", "Thalia", "Greenheart", "wildwood", "cleric", 5, "🌿", "Druid-queen of the silver grove. Plants bloom where she sleeps."),
  h("wd_finn", "Finn", "Moonhunter", "wildwood", "ranger", 5, "🌙", "Silent hunter with silver arrows made for wolves that shouldn't exist."),
  h("wd_bram", "Bram", "Barkwarden", "wildwood", "guardian", 4, "🌳", "Half-treant bodyguard; roots into the earth to hold the line."),
  h("wd_nyx", "Nyx", "Vinewhisper", "wildwood", "mage", 4, "🪴", "Her spellbook has roots and sometimes snarls."),
  h("wd_kade", "Kade", "Fangmark", "wildwood", "assassin", 3, "🐺", "Raised by wolves. Literally. Has the table manners to prove it."),
  h("wd_ursa", "Ursa", "Stormpaw", "wildwood", "berserker", 4, "🐻", "Part bear, entirely angry, surprisingly polite."),
  h("wd_sable", "Sable", "Mistwalker", "wildwood", "assassin", 5, "🍃", "She appears in the mist, leaves a blade, and vanishes without a sound."),

  // --- ARCANE ---
  h("ar_seraphine", "Seraphine", "Starspeaker", "arcane", "mage", 5, "🌟", "Chief astronomer of the Glass Spire; reads destiny in supernovae."),
  h("ar_lumen", "Lumen", "Chronomancer", "arcane", "mage", 5, "⏳", "Can stop a raindrop mid-fall. Cannot stop a hangover."),
  h("ar_iron", "Iron", "Golemwright", "arcane", "guardian", 4, "🤖", "Artificer riding a clockwork warbeast. Both are grumpy."),
  h("ar_veska", "Veska", "Mindblade", "arcane", "assassin", 4, "🔮", "Kills you with the idea of a dagger first. The real one is a formality."),
  h("ar_kael", "Kael", "Hexmark", "arcane", "ranger", 4, "🎯", "His crossbow fires guided curses. Some of them love him back."),
  h("ar_opal", "Opal", "Sparkwright", "arcane", "cleric", 3, "💡", "Engineer-cleric who patches heroes with light and solder."),
  h("ar_morvain", "Morvain", "Voidreaver", "arcane", "berserker", 5, "🌀", "Split from his shadow; now they're both armed and very tired."),

  // --- RADIANCE ---
  h("rd_auriel", "Auriel", "Dawnbringer", "radiance", "cleric", 5, "☀️", "A seraph of first light. Her hymn can knit cracked marble."),
  h("rd_gideon", "Gideon", "Sunspear", "radiance", "guardian", 5, "🛡️", "Wields a spear forged from a fallen star's last heartbeat."),
  h("rd_sanna", "Sanna", "Haloshot", "radiance", "ranger", 4, "🏹", "Her bow sings when she draws it. Enemies also sing. Briefly."),
  h("rd_micah", "Micah", "Swordsaint", "radiance", "berserker", 4, "⚔️", "Takes a vow of silence each dawn; speaks only in swordstrokes."),
  h("rd_ember", "Ember", "Firstflame", "radiance", "mage", 5, "🔥", "A spark that became a woman that became a sun that became a problem."),
  h("rd_tama", "Tama", "Veilwatcher", "radiance", "assassin", 3, "👁️", "Hunts across the veil for those who hide from judgement."),

  // --- ABYSS ---
  h("ab_nyxara", "Nyxara", "Queen of Shards", "abyss", "mage", 5, "🖤", "Her crown is a crack in reality. She likes the draft."),
  h("ab_varis", "Varis", "Bloodcaller", "abyss", "berserker", 5, "🩸", "Vampire-knight who duels at dawn to keep himself interesting."),
  h("ab_saelen", "Saelen", "Soulreaver", "abyss", "assassin", 5, "💀", "Has a library of stolen last words. Alphabetized."),
  h("ab_khorr", "Khorr", "Chainbreaker", "abyss", "guardian", 4, "⛓️", "Broke out of a hell; dragged the chains with him for souvenirs."),
  h("ab_tira", "Tira", "Witchhound", "abyss", "ranger", 4, "🏹", "Hunts witches with wolves she enchanted from dogs."),
  h("ab_mordecai", "Mordecai", "Blackbinder", "abyss", "cleric", 4, "📕", "Priest of the unfriendly gods. Surprisingly tender bedside manner."),
  h("ab_zeranna", "Zeranna", "Veinwitch", "abyss", "mage", 3, "🩸", "Her spells need blood, hers or otherwise. She is not picky."),
];

function h(
  id: string,
  name: string,
  title: string,
  faction: HeroTemplate["faction"],
  cls: HeroTemplate["class"],
  baseRarity: Rarity,
  emoji: string,
  bio: string
): HeroTemplate {
  const grad = pick(GRADIENTS[faction], id);
  return {
    id,
    name,
    title,
    faction,
    class: cls,
    baseRarity,
    emoji,
    portraitGradient: [grad[0], grad[1]],
    signatureColor: grad[1],
    bio,
    skills: baseSkills(cls),
  };
}

export const HEROES_BY_ID: Record<string, HeroTemplate> = Object.fromEntries(
  HEROES.map((h) => [h.id, h])
);

export const HEROES_BY_RARITY: Record<Rarity, HeroTemplate[]> = {
  1: [],
  2: [],
  3: HEROES.filter((h) => h.baseRarity === 3),
  4: HEROES.filter((h) => h.baseRarity === 4),
  5: HEROES.filter((h) => h.baseRarity === 5),
};
