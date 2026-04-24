# HeroBrawl — Android (Kotlin + Jetpack Compose)

This is the **primary** HeroBrawl client. It's a 100% Kotlin Android app built with Jetpack Compose, DataStore, and Kotlinx Serialization — ready for Android Studio and, eventually, the Google Play Store.

```
android/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/herobrawl/game/
│       │   │   ├── MainActivity.kt
│       │   │   ├── model/Model.kt          # All @Serializable data classes
│       │   │   ├── data/
│       │   │   │   ├── Factions.kt         # 6 factions + aura/advantage logic
│       │   │   │   ├── Classes.kt          # 6 hero classes
│       │   │   │   ├── Skills.kt           # Class signature actives + passives
│       │   │   │   └── Heroes.kt           # 50+ hero templates
│       │   │   ├── engine/
│       │   │   │   ├── Stats.kt            # Stat math, levelCap, power
│       │   │   │   ├── Progression.kt      # Level/ascend/gear/skill/stone upgrades
│       │   │   │   ├── Gacha.kt            # Transparent pity, 10-pull, echo stacks
│       │   │   │   ├── Idle.kt             # 12h offline accrual
│       │   │   │   ├── Combat.kt           # Auto-combat engine + battle log
│       │   │   │   ├── Quests.kt           # Daily quests + rollover
│       │   │   │   ├── Achievements.kt     # Evaluate / claim
│       │   │   │   └── Events.kt           # Seasonal event banners (new)
│       │   │   ├── store/SaveStore.kt      # DataStore + JSON persistence
│       │   │   ├── vm/GameViewModel.kt     # Coroutines-backed state holder
│       │   │   └── ui/
│       │   │       ├── Theme.kt            # Dark HeroBrawl palette
│       │   │       ├── Common.kt           # Reusable Compose components
│       │   │       ├── HeroCard.kt         # Portrait cards, HP/energy bars
│       │   │       ├── Shell.kt            # Top bar + tab navigation
│       │   │       └── screens/
│       │   │           ├── HomeScreen.kt
│       │   │           ├── CampaignScreen.kt
│       │   │           ├── SummonScreen.kt
│       │   │           ├── RosterScreen.kt
│       │   │           ├── ArenaScreen.kt
│       │   │           ├── EventsScreen.kt         (new)
│       │   │           ├── QuestsScreen.kt
│       │   │           ├── AchievementsScreen.kt   (new)
│       │   │           ├── GuideScreen.kt
│       │   │           ├── BattlePlayback.kt
│       │   │           └── FirstRunGift.kt
│       │   └── res/                        # Theme, adaptive launcher icon, backup rules
│       └── test/java/com/herobrawl/game/engine/    # Unit tests (11 passing)
├── build.gradle.kts                       # Root plugins
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/gradle-wrapper.jar
└── gradle/wrapper/gradle-wrapper.properties
```

## Build it

Requirements:
* Android Studio Giraffe (AGP 8.5+) or JDK 17 + Gradle 8.9 CLI
* Android SDK with platform 35 and build-tools 35.0.0 installed
* Copy `local.properties.template` → `local.properties` or let Android Studio populate `sdk.dir` for you

```bash
cd android
./gradlew :app:assembleDebug         # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease       # signed-debug release APK for sideloading
./gradlew :app:bundleRelease         # app-release.aab for Play Store upload
./gradlew :app:testDebugUnitTest     # run Kotlin engine tests (11/11)
```

A default debug keystore is used for the `release` build type so developers can install it without extra setup. Before uploading to the Play Store, generate a real keystore and wire it in `app/build.gradle.kts` via `signingConfigs {}`.

## Install on a device

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

## Where to expand

Everything under `engine/` is pure Kotlin with **no Android dependencies**, so it ships unchanged to:
* iOS via Kotlin Multiplatform (recommended — the shared module would mirror `model/` + `engine/`)
* Backend / server simulator
* The web client (already in the repo root) via KMP or a straight port

## What's new in v0.2 (this iteration)

* **Equipment Stones**: 3 slots × 5 stone kinds per hero, unlockable with 💠 Fragments earned from stages and events.
* **Skill Leveling**: both passives and actives can be upgraded up to Lv 10 with 🧪 Dust.
* **Events**: a full Dawnbloom Festival banner with 5 token milestones, including a guaranteed limited hero (`Solara, Sunsovereign`).
* **Achievements**: 10 tracked goals with gem / scroll / fragment rewards.
* **More heroes**: roster expanded from 40+ to 50+ including event-only 5★ heroes.
* **Save v2**: new DataStore-backed save with kotlinx.serialization JSON.
* **Android-native build** with adaptive icon, edge-to-edge dark theme, and Play Store–ready AAB bundling.
