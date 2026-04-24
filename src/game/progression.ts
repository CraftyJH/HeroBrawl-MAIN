import type { GameState, HeroInstance, Rarity } from "../types";
import { goldToLevel, xpToNext } from "./stats";
import { templateFor } from "./stats";

export interface LevelUpResult {
  state: GameState;
  gained: number;
  leveledUp: boolean;
}

export function giveHeroXp(state: GameState, instanceId: string, xp: number): LevelUpResult {
  const hero = state.heroes.find((h) => h.instanceId === instanceId);
  if (!hero) return { state, gained: 0, leveledUp: false };

  let xpLeft = xp;
  let leveledUp = false;
  let level = hero.level;
  let cur = hero.xp;
  const cap = levelCap(hero);

  while (xpLeft > 0 && level < cap) {
    const need = xpToNext(level) - cur;
    if (xpLeft >= need) {
      level += 1;
      leveledUp = true;
      xpLeft -= need;
      cur = 0;
    } else {
      cur += xpLeft;
      xpLeft = 0;
    }
  }

  const heroes = state.heroes.map((h) =>
    h.instanceId === instanceId ? { ...h, level, xp: cur } : h
  );

  return { state: { ...state, heroes }, gained: xp, leveledUp };
}

export function levelCap(hero: HeroInstance): number {
  // Cap scales with stars: 5★ = 120, 6★ = 150, 7★ = 180, etc.
  const stars = hero.stars;
  return 30 + stars * 18;
}

export function canLevelUp(state: GameState, hero: HeroInstance): boolean {
  if (hero.level >= levelCap(hero)) return false;
  return state.currency.gold >= goldToLevel(hero.level);
}

export function doLevelUp(state: GameState, instanceId: string): GameState {
  const hero = state.heroes.find((h) => h.instanceId === instanceId);
  if (!hero) return state;
  if (!canLevelUp(state, hero)) return state;
  const cost = goldToLevel(hero.level);
  const updated = {
    ...state,
    currency: { ...state.currency, gold: state.currency.gold - cost },
    heroes: state.heroes.map((h) =>
      h.instanceId === instanceId ? { ...h, level: h.level + 1, xp: 0 } : h
    ),
  };
  return updated;
}

// --- Ascension (a kinder version of Idle Heroes' Creation Circle).
// Instead of sacrificing duplicate heroes, you spend shards to raise
// one hero's star rank. Duplicates already convert to echo stacks in gacha.

export function ascensionCost(stars: Rarity): { shards: number; spirit: number; gold: number } {
  if (stars === 5) return { shards: 60, spirit: 800, gold: 120000 };
  if (stars === 4) return { shards: 30, spirit: 400, gold: 50000 };
  if (stars === 3) return { shards: 15, spirit: 200, gold: 20000 };
  return { shards: 8, spirit: 100, gold: 8000 };
}

export function canAscend(state: GameState, hero: HeroInstance): boolean {
  if (hero.stars >= 5) {
    // 5★+ requires echo stacks (still, no destroy-to-ascend)
    if (hero.stars >= 10) return false;
    if (hero.echoStacks < 2) return false;
  }
  const c = ascensionCost(hero.stars);
  return (
    state.currency.shards >= c.shards &&
    state.currency.spirit >= c.spirit &&
    state.currency.gold >= c.gold
  );
}

export function doAscend(state: GameState, instanceId: string): GameState {
  const hero = state.heroes.find((h) => h.instanceId === instanceId);
  if (!hero) return state;
  if (!canAscend(state, hero)) return state;
  const c = ascensionCost(hero.stars);
  return {
    ...state,
    currency: {
      ...state.currency,
      shards: state.currency.shards - c.shards,
      spirit: state.currency.spirit - c.spirit,
      gold: state.currency.gold - c.gold,
    },
    heroes: state.heroes.map((h) =>
      h.instanceId === instanceId
        ? {
            ...h,
            stars: Math.min(10, h.stars + 1) as Rarity,
            echoStacks: h.stars >= 5 ? h.echoStacks - 2 : h.echoStacks,
          }
        : h
    ),
  };
}

// --- Gear upgrades (simple tiered forge).
export function gearCost(tier: number): { gold: number; shards: number } {
  return { gold: 3000 + tier * 4500, shards: 5 + tier * 6 };
}

export function canUpgradeGear(state: GameState, hero: HeroInstance): boolean {
  if (hero.gearTier >= 6) return false;
  const c = gearCost(hero.gearTier);
  return state.currency.gold >= c.gold && state.currency.shards >= c.shards;
}

export function doUpgradeGear(state: GameState, instanceId: string): GameState {
  const hero = state.heroes.find((h) => h.instanceId === instanceId);
  if (!hero) return state;
  if (!canUpgradeGear(state, hero)) return state;
  const c = gearCost(hero.gearTier);
  return {
    ...state,
    currency: {
      ...state.currency,
      gold: state.currency.gold - c.gold,
      shards: state.currency.shards - c.shards,
    },
    heroes: state.heroes.map((h) =>
      h.instanceId === instanceId ? { ...h, gearTier: h.gearTier + 1 } : h
    ),
  };
}

export function heroDisplayName(state: GameState, instanceId: string): string {
  const h = state.heroes.find((x) => x.instanceId === instanceId);
  if (!h) return "—";
  return templateFor(h).name;
}
