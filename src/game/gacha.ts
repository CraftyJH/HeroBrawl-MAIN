import type { GameState, HeroInstance, HeroTemplate, Rarity } from "../types";
import { HEROES, HEROES_BY_RARITY } from "./heroes";

// HeroBrawl's transparent pity system (an improvement vs Idle Heroes' opaque gacha):
//   - Heroic Pull rates: 3★ 65%, 4★ 28%, 5★ 7%
//   - Soft pity at 30 pulls: 5★ rate doubles each pull
//   - Hard pity at 60 pulls: guaranteed 5★ (pity resets)
//   - Every 10th pull is guaranteed 4★ or better
//   - Duplicate heroes convert to "echo stacks" (never wasted)

export interface PullResult {
  template: HeroTemplate;
  rarity: Rarity;
  isDuplicate: boolean;
  newEchoStacks?: number;
}

export interface PullSummary {
  results: PullResult[];
  updatedState: GameState;
}

const BASE_RATES: Record<Rarity, number> = {
  1: 0,
  2: 0,
  3: 0.65,
  4: 0.28,
  5: 0.07,
};

function rollRarity(pulls: number, sinceEpic: number): Rarity {
  // Hard pity
  if (pulls >= 59) return 5;
  // Guaranteed 4★+ every 10 pulls
  const guaranteedEpicNow = (sinceEpic + 1) % 10 === 0;

  // Soft pity ramp
  let fiveRate = BASE_RATES[5];
  if (pulls >= 30) {
    const over = pulls - 29; // 1..30
    fiveRate = Math.min(0.07 + over * 0.025, 1);
  }
  const fourRate = BASE_RATES[4];
  const threeRate = 1 - fiveRate - fourRate;

  const roll = Math.random();
  if (roll < fiveRate) return 5;
  if (guaranteedEpicNow) return 4;
  if (roll < fiveRate + fourRate) return 4;
  if (threeRate > 0 && roll < fiveRate + fourRate + Math.max(0, threeRate)) return 3;
  return 3;
}

function pickTemplate(rarity: Rarity, factionFilter?: HeroTemplate["faction"]): HeroTemplate {
  let pool = HEROES_BY_RARITY[rarity];
  if (factionFilter) pool = pool.filter((h) => h.faction === factionFilter);
  if (pool.length === 0) pool = HEROES.filter((h) => h.baseRarity === rarity);
  return pool[Math.floor(Math.random() * pool.length)];
}

export function performPull(
  state: GameState,
  opts: { count: 1 | 10; kind: "heroic" | "prophet"; factionFilter?: HeroTemplate["faction"] }
): PullSummary {
  const results: PullResult[] = [];
  let pity = state.gacha.pityCount;
  let sinceEpic = state.gacha.sinceEpic;
  const newHeroes: HeroInstance[] = [];
  const heroes = [...state.heroes];

  for (let i = 0; i < opts.count; i++) {
    const rarity = rollRarity(pity, sinceEpic);
    const template = pickTemplate(rarity, opts.factionFilter);

    const existing = heroes.find((h) => h.templateId === template.id);
    if (existing) {
      existing.echoStacks += 1;
      results.push({
        template,
        rarity,
        isDuplicate: true,
        newEchoStacks: existing.echoStacks,
      });
    } else {
      const inst: HeroInstance = {
        instanceId: crypto.randomUUID(),
        templateId: template.id,
        level: 1,
        stars: template.baseRarity,
        xp: 0,
        echoStacks: 0,
        gearTier: 0,
      };
      heroes.push(inst);
      newHeroes.push(inst);
      results.push({ template, rarity, isDuplicate: false });
    }

    if (rarity >= 5) {
      pity = 0;
    } else {
      pity += 1;
    }
    if (rarity >= 4) sinceEpic = 0;
    else sinceEpic += 1;
  }

  const updatedState: GameState = {
    ...state,
    heroes,
    gacha: {
      heroicPulls: state.gacha.heroicPulls + opts.count,
      pityCount: pity,
      sinceEpic,
    },
  };

  return { results, updatedState };
}

export function pullCost(count: 1 | 10, kind: "heroic" | "prophet") {
  if (kind === "heroic") {
    if (count === 1) return { scrolls: 1, gems: 0 };
    return { scrolls: 10, gems: 0 };
  }
  // prophet summon uses prophet orbs
  if (count === 1) return { scrolls: 0, orbs: 1 } as const;
  return { scrolls: 0, orbs: 10 } as const;
}
