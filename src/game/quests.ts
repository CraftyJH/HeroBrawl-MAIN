import type { GameState } from "../types";
import { isoDay, pushMessage } from "./state";

export interface QuestDef {
  id: string;
  name: string;
  description: string;
  target: number;
  rewards: { gems?: number; gold?: number; scrolls?: number; dust?: number; orbs?: number };
}

export const DAILY_QUESTS: QuestDef[] = [
  {
    id: "campaign-3",
    name: "Campaign Commander",
    description: "Complete 3 campaign stages.",
    target: 3,
    rewards: { gold: 5000, dust: 10 },
  },
  {
    id: "pulls-1",
    name: "Fateful Summons",
    description: "Perform 1 gacha pull.",
    target: 1,
    rewards: { gems: 40, orbs: 2 },
  },
  {
    id: "arena-2",
    name: "Arena Challenger",
    description: "Fight 2 arena matches.",
    target: 2,
    rewards: { gems: 30, scrolls: 1 },
  },
  {
    id: "idle-claim",
    name: "Diligent Captain",
    description: "Claim your idle rewards.",
    target: 1,
    rewards: { gold: 2000, dust: 5 },
  },
  {
    id: "level-hero",
    name: "Trainer",
    description: "Level up any hero 5 times.",
    target: 5,
    rewards: { gold: 3000, gems: 20 },
  },
];

export const COMPLETION_REWARD = { gems: 100, scrolls: 1 };

export function ensureDailyRollover(state: GameState): GameState {
  const today = isoDay(Date.now());
  if (state.quests.day !== today) {
    return {
      ...state,
      quests: { day: today, progress: {}, claimed: {}, completionClaimed: false },
      arena: { ...state.arena, ticketsUsed: 0, ticketDay: today },
    };
  }
  return state;
}

export function bumpQuest(state: GameState, questId: string, by = 1): GameState {
  const s = ensureDailyRollover(state);
  const cur = s.quests.progress[questId] ?? 0;
  const def = DAILY_QUESTS.find((q) => q.id === questId);
  if (!def) return s;
  const next = Math.min(def.target, cur + by);
  return {
    ...s,
    quests: { ...s.quests, progress: { ...s.quests.progress, [questId]: next } },
  };
}

export function canClaimQuest(state: GameState, questId: string): boolean {
  const def = DAILY_QUESTS.find((q) => q.id === questId);
  if (!def) return false;
  if (state.quests.claimed[questId]) return false;
  const cur = state.quests.progress[questId] ?? 0;
  return cur >= def.target;
}

export function claimQuest(state: GameState, questId: string): GameState {
  if (!canClaimQuest(state, questId)) return state;
  const def = DAILY_QUESTS.find((q) => q.id === questId)!;
  const r = def.rewards;
  const s: GameState = {
    ...state,
    currency: {
      ...state.currency,
      gems: state.currency.gems + (r.gems ?? 0),
      gold: state.currency.gold + (r.gold ?? 0),
      heroicScrolls: state.currency.heroicScrolls + (r.scrolls ?? 0),
      dust: state.currency.dust + (r.dust ?? 0),
      prophetOrbs: state.currency.prophetOrbs + (r.orbs ?? 0),
    },
    quests: { ...state.quests, claimed: { ...state.quests.claimed, [questId]: true } },
  };
  return pushMessage(s, `Quest complete: ${def.name}`, "reward");
}

export function allQuestsCompleted(state: GameState): boolean {
  return DAILY_QUESTS.every((q) => (state.quests.progress[q.id] ?? 0) >= q.target);
}

export function canClaimCompletion(state: GameState): boolean {
  return allQuestsCompleted(state) && !state.quests.completionClaimed;
}

export function claimCompletion(state: GameState): GameState {
  if (!canClaimCompletion(state)) return state;
  const s: GameState = {
    ...state,
    currency: {
      ...state.currency,
      gems: state.currency.gems + COMPLETION_REWARD.gems,
      heroicScrolls: state.currency.heroicScrolls + COMPLETION_REWARD.scrolls,
    },
    quests: { ...state.quests, completionClaimed: true },
  };
  return pushMessage(s, "All daily quests complete! Bonus claimed.", "reward");
}
