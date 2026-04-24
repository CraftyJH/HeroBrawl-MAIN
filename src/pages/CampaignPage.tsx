import { useMemo, useState } from "react";
import { useGame } from "../store/useGame";
import {
  buildAllyUnits,
  buildEnemyUnits,
  rewardsForCampaign,
  simulateBattle,
} from "../game/combat";
import type { BattleResult, BattleUnit } from "../types";
import { BattleStage } from "../components/BattleStage";
import { bumpQuest } from "../game/quests";
import { giveHeroXp } from "../game/progression";
import { upgradeIdleRates } from "../game/idle";

export function CampaignPage() {
  const { state, update, notify } = useGame();
  const [battle, setBattle] = useState<{ allies: BattleUnit[]; enemies: BattleUnit[]; result: BattleResult } | null>(null);

  const chapters = 10;
  const stagesPerChapter = 10;

  const totalProgress = (state.campaign.chapter - 1) * stagesPerChapter + (state.campaign.stage - 1);

  const chapterStages = useMemo(() => {
    return Array.from({ length: stagesPerChapter }, (_, i) => i + 1);
  }, []);

  const allyCount = state.lineup.slots.filter(Boolean).length;

  const start = (chapter: number, stage: number) => {
    if (allyCount === 0) {
      notify("Assign at least one hero to your lineup.", "warn");
      return;
    }
    const allies = buildAllyUnits(state);
    const enemies = buildEnemyUnits(chapter, stage);
    const result = simulateBattle(allies, enemies);
    setBattle({ allies, enemies, result });
  };

  const onBattleEnd = (chapter: number, stage: number, result: BattleResult) => {
    if (result.winner !== "ally") {
      notify("Defeat. Try leveling your heroes or tweaking your lineup.", "warn");
      return;
    }

    const firstClear =
      state.campaign.chapter < chapter ||
      (state.campaign.chapter === chapter && state.campaign.stage <= stage);

    const r = rewardsForCampaign(chapter, stage, firstClear);
    update((s) => {
      let next = {
        ...s,
        currency: {
          ...s.currency,
          gold: s.currency.gold + r.gold,
          spirit: s.currency.spirit + r.spirit,
          gems: s.currency.gems + (r.firstClear?.gems ?? 0),
        },
        brawl: { value: s.brawl.value + r.brawlPoints },
      };

      if (firstClear) {
        let nextChapter = chapter;
        let nextStage = stage + 1;
        if (nextStage > stagesPerChapter) {
          nextChapter += 1;
          nextStage = 1;
        }
        if (nextChapter > chapters) {
          nextChapter = chapters;
          nextStage = stagesPerChapter;
        }
        next = {
          ...next,
          campaign: { chapter: nextChapter, stage: nextStage },
          idle: {
            ...next.idle,
            ratePerHour: upgradeIdleRates(
              { gold: 1200, spirit: 60, shards: 4 },
              nextChapter,
              nextStage
            ),
          },
        };
      }

      for (const slot of next.lineup.slots) {
        if (!slot) continue;
        const res = giveHeroXp(next, slot, r.xp);
        next = res.state;
      }

      return bumpQuest(next, "campaign-3", 1);
    });

    notify(
      `Stage cleared! +${r.gold} gold, +${r.spirit} spirit${
        r.firstClear ? `, +${r.firstClear.gems} gems` : ""
      }.`,
      "reward"
    );
  };

  return (
    <div className="page">
      <h1>Campaign</h1>
      <p className="sub">
        Chapter <strong>{state.campaign.chapter}</strong> · Stage {state.campaign.stage} ·
        Cleared <strong>{totalProgress}</strong> stages
      </p>

      <div className="grid grid-2">
        <div className="card">
          <h3>Chapter {state.campaign.chapter}</h3>
          <p className="float-info">{chapterBlurb(state.campaign.chapter)}</p>
          <div className="stage-grid" style={{ marginTop: 12 }}>
            {chapterStages.map((stage) => {
              const done = stage < state.campaign.stage;
              const current = stage === state.campaign.stage;
              return (
                <button
                  key={stage}
                  className={`stage-btn ${done ? "done" : ""} ${current ? "next" : ""}`}
                  disabled={stage > state.campaign.stage}
                  onClick={() => start(state.campaign.chapter, stage)}
                >
                  {done ? "✓ " : ""}
                  {state.campaign.chapter}-{stage}
                </button>
              );
            })}
          </div>
          <div className="row" style={{ marginTop: 16 }}>
            <button
              className="btn"
              disabled={state.campaign.chapter <= 1}
              onClick={() => update((s) => ({ ...s, campaign: { ...s.campaign, chapter: s.campaign.chapter - 1, stage: 1 } }))}
            >
              ← Prev Chapter
            </button>
            <button
              className="btn"
              disabled={state.campaign.chapter >= chapters}
              onClick={() =>
                update((s) => {
                  const next = Math.min(chapters, s.campaign.chapter + 1);
                  if (next <= s.campaign.chapter) return s;
                  if ((s.campaign.chapter - 1) * stagesPerChapter + s.campaign.stage < next * stagesPerChapter) {
                    return s;
                  }
                  return { ...s, campaign: { ...s.campaign, chapter: next, stage: 1 } };
                })
              }
            >
              Next Chapter →
            </button>
          </div>
        </div>

        <div className="card">
          <h3>Stage Preview</h3>
          <p className="float-info">
            Your lineup ({allyCount} heroes) vs. 5 scaled opponents. Win to progress, level your roster, and unlock more idle rates.
          </p>
          <p style={{ color: "var(--text-dim)" }}>
            Tip: Front-row slots (1–3) soak damage; put <strong>Guardians</strong> and <strong>Berserkers</strong> there. Back row (4–5) is safer for <strong>Rangers</strong>, <strong>Mages</strong>, and <strong>Clerics</strong>.
          </p>
          <ul style={{ color: "var(--text-dim)", lineHeight: 1.7, paddingLeft: 18 }}>
            <li>Faction advantage: +20% damage vs the faction you counter.</li>
            <li>5-of-a-kind mono-faction aura gives up to +25% atk & +30% hp.</li>
            <li>Mixing 2 Radiance + 2 Abyss unlocks the Dawnfall Pact.</li>
          </ul>
        </div>
      </div>

      {battle && (
        <BattleStage
          allies={battle.allies}
          enemies={battle.enemies}
          result={battle.result}
          onClose={() => {
            onBattleEnd(state.campaign.chapter, state.campaign.stage, battle.result);
            setBattle(null);
          }}
        />
      )}
    </div>
  );
}

function chapterBlurb(chapter: number) {
  const blurbs: Record<number, string> = {
    1: "The Fallow Marches — rogue sellswords burn the border hamlets.",
    2: "Ashkeep Ridge — the Horde wakes an old fire beneath the badlands.",
    3: "The Witchwood — roots older than kings, and just as proud.",
    4: "Glass Spire — the Arcane towers flicker. Something is drinking the light.",
    5: "Dawnmarch — Radiance opens its gates, and the Abyss answers.",
    6: "The Shardlands — broken geometry, broken gods.",
    7: "Voidreach — stars falling, bargains forming.",
    8: "The Hollow Throne — a queen of shadows wakes.",
    9: "Ember & Ash — the final shore before the last light.",
    10: "Endgame — HeroBrawl's last trial. Beat it. We dare you.",
  };
  return blurbs[chapter] ?? "Unknown lands.";
}
