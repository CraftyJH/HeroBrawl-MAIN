# ⚔️ HeroBrawl

> A modern open-source idle RPG / gacha — a competitor to **Idle Heroes** (DHGames) and **TapTap Heroes** (Westbund), built to be fairer, more transparent, and more fun for free-to-play players.

HeroBrawl is a Kotlin + Jetpack Compose Android app (plus a reference React web build). It's aimed squarely at shipping on the Google Play Store.

| Target | Stack | Status |
| --- | --- | --- |
| **Android (primary)** | Kotlin 2.0 · Jetpack Compose · DataStore · Kotlinx Serialization | ✅ Debug/release APK + Play Store AAB build green |
| Web (reference) | Vite · React 19 · TypeScript | ✅ Lint/build/test green |

---

## 🎯 What makes HeroBrawl different

| Problem in Idle Heroes / TapTap Heroes | HeroBrawl's fix |
| --- | --- |
| Opaque gacha pity | **Transparent pity** — live 0/60 counter, soft-pity ramp from pull 30, guaranteed 5★ at 60 |
| Duplicates must be fused/sacrificed | **Echo stacks** — duplicates auto-buff heroes, never wasted |
| 8-hour idle cap | **12-hour idle cap** (+50% accrual) |
| VIP-locked heroes | **No VIP paywall** — every hero reachable F2P |
| Seasonal servers wipe progress | **Season-safe roster** — collection always persists |
| Aura/faction rules are opaque | **Clear faction wheel** (rock-paper-scissors) + Dawnfall Pact aura |
| Daily bonus is trivial | **Chunky all-clear bonus**: +100 💎 + 1 📜 every day |
| Skill growth is hidden in RNG | **Explicit skill leveling** (🧪 Dust) |
| Gear is fungible and forgettable | **Equipment Stones** (💠 Fragments) with 5 types × 3 slots |

## 🧱 Game systems (v0.2 iteration)

* **6 factions** — Vanguard → Horde → Wildwood → Arcane → Vanguard, plus the Radiance ↔ Abyss mirror pair.
* **6 classes** — Guardian, Berserker, Mage, Assassin, Ranger, Cleric. Each has a signature active skill, combat passive, and unique targeting.
* **50+ heroes** — each with name, title, emoji sprite, faction-themed gradient portrait, and class skill set. Includes event-only 5★ heroes.
* **Auto-combat engine** — speed-based turn order, energy bars, status effects (burn/poison/stun/shield/regen), class passives (Bloodrage, Hawk's Eye, Killsight, Manaflow, Radiant Aegis), faction advantage, crits.
* **Modes** — Campaign (10×10 stages with scaling idle rates), Arena PvP (10 daily tickets, Elo-style rating, procedural opponents), Summon (Heroic scrolls + Prophet Orbs faction-targeted), Daily Quests, Events, Achievements, Roster management.
* **Progression** — Level (🪙 Gold), Ascend (🔹 Shards + ✨ Spirit + 🪙 Gold; 5★+ needs echo stacks), Gear (6 tiers), **Skill Leveling** (🧪 Dust, Lv 1–10), **Equipment Stones** (💠 Fragments, 3 slots, 5 kinds).
* **Events** — Dawnbloom Festival style tokens earned from normal play; hit milestones to unlock the limited featured 5★ hero.
* **Achievements** — 10 tracked goals that grant permanent gem + fragment rewards.
* **Idle rewards** — 12-hour cap, rates scale with campaign progress, one-click claim.
* **Persistent autosave** — DataStore on Android, localStorage on web.

## 🚀 Android — build & run

Prereqs: Android Studio Giraffe+ (AGP 8.5) or JDK 17 + Android SDK with platform `android-35` and `build-tools;35.0.0`.

```bash
cd android
cp local.properties.template local.properties   # edit sdk.dir
./gradlew :app:assembleDebug                     # app-debug.apk
./gradlew :app:assembleRelease                   # app-release.apk
./gradlew :app:bundleRelease                     # app-release.aab  (Play Store)
./gradlew :app:testDebugUnitTest                 # 11 unit tests
```

Install on a connected device:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

Open in Android Studio: `File → Open → /workspace/android`.

## 🌐 Web reference build

Kept in the repo root for design iteration and browser demos. It's a 1:1 mirror of the game rules.

```bash
npm install
npm run dev     # http://localhost:5173
npm run build
npm run test    # 9 Vitest tests
```

## 🗺️ Roadmap

- Guilds (cooperative raids, guild shop, guild tech).
- Online PvP live replay with manual ability-targeting ("Brawl moves").
- Cloud save with Google Play Games Services.
- In-app purchases via Play Billing Library + server-side entitlement sync.
- KMP `shared` module so the engine compiles to iOS / JS / JVM as-is.
- Illustrated hero portraits (current emoji + faction gradients are the MVP stand-in).

## 🎨 Art style

For the MVP, every hero has:

* A unique emoji sprite (`🛡️ 🏹 🔥 …`) chosen to match faction/class theme.
* A **deterministic faction-themed gradient portrait** (no copyrighted assets).
* Name, title, and lore bio that stand on their own.

Dropping in bespoke illustrations is trivial — every `HeroTemplate` already carries `portraitGradient`, `signatureColor`, and `emoji`. Replace with a drawable reference and tweak `HeroCard.kt` (Android) / `HeroCard.tsx` (web).

## 📝 License

MIT — see `LICENSE`. All hero names, stories, and factions are original to HeroBrawl.

_HeroBrawl is an independent fan-inspired project. It is **not** affiliated with DHGames' Idle Heroes or Westbund's TapTap Heroes._
