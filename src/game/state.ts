import type {
  CurrencyState,
  GameMessage,
  GameState,
  HeroInstance,
} from "../types";

const SAVE_KEY = "herobrawl.save.v1";
const SAVE_VERSION = 1;

export function defaultCurrency(): CurrencyState {
  return {
    gold: 5000,
    gems: 500,
    spirit: 0,
    prophetOrbs: 0,
    heroicScrolls: 10,
    basicScrolls: 20,
    shards: 0,
    dust: 0,
  };
}

export function newGameState(playerName = "Champion"): GameState {
  const now = Date.now();
  return {
    version: SAVE_VERSION,
    createdAt: now,
    playerName,
    playerLevel: 1,
    playerXp: 0,
    currency: defaultCurrency(),
    heroes: [],
    lineup: { slots: [null, null, null, null, null] },
    idle: {
      startedAt: now,
      tickedAt: now,
      ratePerHour: { gold: 1200, spirit: 60, shards: 4 },
      capHours: 12,
    },
    campaign: { chapter: 1, stage: 1 },
    arena: {
      rating: 1000,
      wins: 0,
      losses: 0,
      lastFight: 0,
      ticketsUsed: 0,
      ticketDay: isoDay(now),
    },
    quests: { day: isoDay(now), progress: {}, claimed: {}, completionClaimed: false },
    gacha: { heroicPulls: 0, pityCount: 0, sinceEpic: 0 },
    brawl: { value: 0 },
    messages: [
      welcomeMessage(),
    ],
    settings: { sound: true, confetti: true },
  };
}

function welcomeMessage(): GameMessage {
  return {
    id: crypto.randomUUID(),
    at: Date.now(),
    text: "Welcome to HeroBrawl! Pull your first heroes and enter the arena.",
    kind: "info",
  };
}

export function isoDay(ms: number): string {
  return new Date(ms).toISOString().slice(0, 10);
}

export function loadGame(): GameState | null {
  try {
    const raw = localStorage.getItem(SAVE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as GameState;
    if (parsed.version !== SAVE_VERSION) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function saveGame(state: GameState) {
  try {
    localStorage.setItem(SAVE_KEY, JSON.stringify(state));
  } catch {
    // ignore quota errors silently
  }
}

export function wipeSave() {
  localStorage.removeItem(SAVE_KEY);
}

export function pushMessage(
  state: GameState,
  text: string,
  kind: GameMessage["kind"] = "info"
): GameState {
  const messages = [
    { id: crypto.randomUUID(), at: Date.now(), text, kind },
    ...state.messages,
  ].slice(0, 30);
  return { ...state, messages };
}

export function findHero(state: GameState, instanceId: string): HeroInstance | undefined {
  return state.heroes.find((h) => h.instanceId === instanceId);
}
