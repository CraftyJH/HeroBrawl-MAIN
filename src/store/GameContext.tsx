import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { GameState } from "../types";
import { loadGame, newGameState, pushMessage, saveGame } from "../game/state";
import { ensureDailyRollover } from "../game/quests";
import { GameContext } from "./context";

export function GameProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<GameState>(() => {
    const loaded = loadGame();
    return ensureDailyRollover(loaded ?? newGameState());
  });

  const update = useCallback((fn: (s: GameState) => GameState) => {
    setState((prev) => {
      const next = fn(prev);
      return ensureDailyRollover(next);
    });
  }, []);

  const notify = useCallback(
    (text: string, kind: "info" | "success" | "warn" | "reward" = "info") => {
      setState((prev) => pushMessage(prev, text, kind));
    },
    []
  );

  const reset = useCallback(() => {
    setState(newGameState());
  }, []);

  const saveTimer = useRef<number | null>(null);
  useEffect(() => {
    if (saveTimer.current) window.clearTimeout(saveTimer.current);
    saveTimer.current = window.setTimeout(() => {
      saveGame(state);
    }, 400);
    return () => {
      if (saveTimer.current) window.clearTimeout(saveTimer.current);
    };
  }, [state]);

  useEffect(() => {
    const onFocus = () => setState((s) => ensureDailyRollover(s));
    window.addEventListener("focus", onFocus);
    const t = window.setInterval(onFocus, 60 * 1000);
    return () => {
      window.removeEventListener("focus", onFocus);
      window.clearInterval(t);
    };
  }, []);

  const value = useMemo(
    () => ({ state, setState, update, reset, notify }),
    [state, update, reset, notify]
  );
  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}
