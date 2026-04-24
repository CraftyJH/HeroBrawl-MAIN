import type {
  HeroInstance,
  HeroStats,
  HeroTemplate,
  Rarity,
} from "../types";
import { CLASSES } from "./classes";
import { HEROES_BY_ID } from "./heroes";

// Level up multiplier (quadratic feel, capped)
export function levelMultiplier(level: number): number {
  return 1 + (level - 1) * 0.06 + Math.pow(level - 1, 1.45) * 0.004;
}

export function starMultiplier(stars: Rarity): number {
  return 1 + (stars - 1) * 0.18;
}

export function gearMultiplier(tier: number): number {
  return 1 + tier * 0.09;
}

export function echoMultiplier(stacks: number): number {
  return 1 + Math.min(stacks, 10) * 0.03;
}

export function templateFor(inst: HeroInstance): HeroTemplate {
  return HEROES_BY_ID[inst.templateId];
}

export function computeStats(inst: HeroInstance): HeroStats {
  const t = templateFor(inst);
  const base = CLASSES[t.class].baseStats;
  const rarityMul = 0.85 + t.baseRarity * 0.1;
  const mul =
    levelMultiplier(inst.level) *
    starMultiplier(inst.stars) *
    gearMultiplier(inst.gearTier) *
    echoMultiplier(inst.echoStacks) *
    rarityMul;

  const stoneBonus = stoneBonuses(inst);

  return {
    attack: Math.round(base.attack * mul * (1 + stoneBonus.attack)),
    health: Math.round(base.health * mul * (1 + stoneBonus.health)),
    armor: Math.round(base.armor * mul * 0.85),
    speed: Math.round(base.speed * (1 + stoneBonus.speed)),
    willpower: Math.round(base.willpower * (1 + stoneBonus.willpower)),
  };
}

function stoneBonuses(inst: HeroInstance) {
  const b = { attack: 0, health: 0, speed: 0, willpower: 0, crit: 0, energy: 0 };
  switch (inst.equippedStone) {
    case "attack":
      b.attack = 0.12;
      break;
    case "health":
      b.health = 0.15;
      break;
    case "speed":
      b.speed = 0.12;
      break;
    case "energy":
      b.energy = 0.2;
      break;
    case "crit":
      b.crit = 0.15;
      break;
  }
  return b;
}

export function powerOf(inst: HeroInstance): number {
  const s = computeStats(inst);
  return Math.round(s.attack * 4 + s.health * 0.5 + s.armor * 2 + s.speed * 5);
}

export function xpToNext(level: number): number {
  return Math.round(60 + Math.pow(level, 1.9) * 14);
}

export function goldToLevel(level: number): number {
  return Math.round(40 + Math.pow(level, 1.8) * 9);
}
