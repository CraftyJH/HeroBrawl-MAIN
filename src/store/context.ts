import { createContext } from "react";
import type { GameState } from "../types";

export interface GameCtx {
  state: GameState;
  setState: React.Dispatch<React.SetStateAction<GameState>>;
  update: (fn: (s: GameState) => GameState) => void;
  reset: () => void;
  notify: (text: string, kind?: "info" | "success" | "warn" | "reward") => void;
}

export const GameContext = createContext<GameCtx | null>(null);
