# ⚔️ HeroBrawl

> A modern, open-source idle RPG / gacha — a direct competitor to **Idle Heroes** (DHGames) and **TapTap Heroes** (Westbund), built to be fairer, more transparent, and more fun for free-to-play players.

HeroBrawl runs entirely in your browser. It's a polished React + TypeScript app with persistent localStorage saves, offline idle accrual, an auto-combat engine, a gacha system with full transparent pity, a campaign, arena PvP vs. procedural opponents, daily quests, and 40+ heroes across 6 factions and 6 classes.

---

## 🎯 Design pillars (what makes it better than its inspirations)

| Problem in Idle Heroes / TapTap Heroes | HeroBrawl's fix |
| --- | --- |
| Opaque gacha pity | **Transparent pity counter** — guaranteed 5★ every 60 pulls, soft pity ramps from pull 30, guaranteed 4★+ every 10 |
| Duplicate heroes must be fused/sacrificed | **Echo stacks** — duplicates auto-boost the hero you already own |
| 8-hour idle cap | **12-hour idle cap** (+50% accrual) |
| VIP paywalls hide heroes | **No VIP paywall** — every hero reachable F2P |
| Seasonal servers wipe progress | **Season-safe roster** — collection always persists |
| Aura rules are opaque | **Clear faction wheel** (rock-paper-scissors) + transparent mono-faction & Dawnfall Pact bonuses |
| Campaign stages feel same-y | **10 chapters × 10 stages**, each boosting your idle rates permanently |
| Daily quest bonus is trivial | Bigger all-clear bonus: **+100 💎 + 1 📜** every day, achievable F2P |

## 🧱 Game systems

* **6 factions** with a rock-paper-scissors counter wheel: Vanguard → Horde → Wildwood → Arcane → Vanguard, plus the Radiance ↔ Abyss mirror pair.
* **6 classes**: Guardian, Berserker, Mage, Assassin, Ranger, Cleric. Each has a signature active skill and a combat passive.
* **40+ signature heroes**, each with name, title, lore bio, a procedural faction-themed portrait, emoji sprite, and class-based skills.
* **Auto-combat engine** with speed-based turn order, energy bars, status effects (burn, poison, shield, stun, regen), class passives (Bloodrage, Hawk's Eye, Killsight, Manaflow, etc.), faction advantages, and critical hits.
* **Campaign mode** — 10 chapters × 10 stages with scaling opponents, permanent idle-rate boosts on first clear, and first-clear gem rewards.
* **Arena PvP** — daily tickets, procedurally generated opposing rosters, Elo-ish rating updates, opponent refresh.
* **Gacha** — Heroic scrolls with transparent pity, Prophet Orb faction-targeted summons, ten-pull guaranteed 4★+, starter guaranteed-5★ welcome pack.
* **Hero progression** — level-up (gold), ascend to raise stars (shards + spirit + gold; 5★+ also needs 2 echo stacks), gear tiers (6), plus class-specific passives and scaling.
* **Idle rewards** — 12-hour cap, rates scale with campaign progress, one-click claim.
* **Daily quests** — 5 rotating quests with gem + orb + scroll rewards, plus an all-clear bonus.
* **Persistent autosave** — every change is debounce-saved to `localStorage`.

## 🚀 Getting started

```bash
npm install
npm run dev     # http://localhost:5173
```

Production build:

```bash
npm run build
npm run preview
```

Lint:

```bash
npm run lint
```

## 🧩 Project structure

```
src/
  App.tsx                 # top-level layout + tab routing
  types.ts                # all shared TypeScript types
  index.css               # entire visual theme (dark, game-y, glassy)
  main.tsx                # Vite entry
  store/
    GameContext.tsx       # React context provider
    context.ts            # context shape
    useGame.ts            # hook consumed by pages
  game/
    classes.ts            # 6 hero classes
    factions.ts           # 6 factions + advantage/aura logic
    heroes.ts             # 40+ hero templates
    skills.ts             # class signature skills (active + passive)
    stats.ts              # stat math: level/star/gear/echo multipliers, power
    progression.ts        # level up, ascend, gear
    gacha.ts              # pity, rates, pulls
    combat.ts             # auto-combat engine + log + rewards
    idle.ts               # offline accrual
    quests.ts             # daily quests and rollover
    state.ts              # save/load, defaults, messages
  components/
    TopBar.tsx            # sticky top bar with currency pills
    Tabs.tsx              # nav tabs
    HeroCard.tsx          # reusable hero tile
    BattleStage.tsx       # animated battle playback modal
    Modal.tsx             # reusable dialog
    FirstRunGift.tsx      # welcome starter pack
  pages/
    HomePage.tsx          # idle claim, lineup peek, activity log
    CampaignPage.tsx      # chapter grid, battle launcher
    SummonPage.tsx        # gacha UI
    RosterPage.tsx        # filters, lineup editor, hero detail
    ArenaPage.tsx         # procedural opponents, ticketed fights
    QuestsPage.tsx        # dailies + all-clear bonus
    HelpPage.tsx          # in-game guide + reset save
```

## 🗺️ Roadmap

* Guilds (coop), guild raids, guild shop.
* Skill leveling with dust (UI hook is in progress, currency already tracked).
* Stones (Crit/ATK/Speed/Energy/HP) equippable slots.
* Real-time arena battle playback (currently a fast replay).
* Cross-device save (Cloud Save button).

## 🎨 Art style

The hero sprites are currently **faction-themed procedural portraits with emoji "stand-ins"**, chosen to keep the repo lightweight and license-safe. Each hero card's gradient is deterministic from the hero id, so portraits are visually consistent while avoiding any copyrighted asset.

Switching to bespoke illustrated portraits (à la TapTap Heroes) is a drop-in change: every hero template already has a `portraitGradient`, `signatureColor`, and emoji slot — replace those with a PNG path and a tiny `<img>` inside `HeroCard`.

## 📝 License

MIT — see `LICENSE`. Hero names, stories, and factions are original to HeroBrawl.

---

_HeroBrawl is an independent fan-inspired project. It is **not** affiliated with DHGames' Idle Heroes or Westbund's TapTap Heroes._
