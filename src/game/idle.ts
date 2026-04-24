import type { GameState, IdleRates } from "../types";

export interface IdleReward {
  gold: number;
  spirit: number;
  shards: number;
  hours: number;
  capped: boolean;
}

export function computeIdleReward(state: GameState, now = Date.now()): IdleReward {
  const elapsedMs = now - state.idle.tickedAt;
  const capMs = state.idle.capHours * 3600 * 1000;
  const effective = Math.max(0, Math.min(elapsedMs, capMs));
  const hours = effective / 3600000;

  const { gold, spirit, shards } = state.idle.ratePerHour;

  return {
    gold: Math.floor(gold * hours),
    spirit: Math.floor(spirit * hours),
    shards: Math.floor(shards * hours),
    hours,
    capped: elapsedMs >= capMs && capMs > 0,
  };
}

export function claimIdleReward(state: GameState, now = Date.now()): GameState {
  const r = computeIdleReward(state, now);
  return {
    ...state,
    idle: { ...state.idle, tickedAt: now },
    currency: {
      ...state.currency,
      gold: state.currency.gold + r.gold,
      spirit: state.currency.spirit + r.spirit,
      shards: state.currency.shards + r.shards,
    },
  };
}

export function upgradeIdleRates(base: IdleRates, chapter: number, stage: number): IdleRates {
  const progress = (chapter - 1) * 10 + (stage - 1);
  const mul = 1 + progress * 0.08;
  return {
    gold: Math.round(base.gold * mul),
    spirit: Math.round(base.spirit * mul),
    shards: Math.round(base.shards * mul),
  };
}
