import { useEffect, useRef, useState } from "react";
import type { BattleResult, BattleUnit } from "../types";
import { Modal } from "./Modal";

export function BattleStage({
  allies,
  enemies,
  result,
  onClose,
}: {
  allies: BattleUnit[];
  enemies: BattleUnit[];
  result: BattleResult;
  onClose: () => void;
}) {
  // Animate unit HP + log based on result timeline, by replaying log entries
  const [logIdx, setLogIdx] = useState(0);
  const [units, setUnits] = useState<Record<string, { hp: number; maxHp: number; hit?: boolean }>>(
    () => {
      const map: Record<string, { hp: number; maxHp: number }> = {};
      for (const u of [...allies, ...enemies]) {
        map[u.uid] = { hp: u.hp, maxHp: u.maxHp };
      }
      return map;
    }
  );
  const [done, setDone] = useState(false);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    if (logIdx >= result.log.length) {
      timerRef.current = window.setTimeout(() => setDone(true), 0);
      return () => {
        if (timerRef.current) window.clearTimeout(timerRef.current);
      };
    }
    const entry = result.log[logIdx];
    const delay = entry.kind === "info" || entry.kind === "victory" || entry.kind === "defeat" ? 250 : 180;

    timerRef.current = window.setTimeout(() => {
      if (entry.target && entry.amount != null && (entry.kind === "attack")) {
        setUnits((prev) => {
          const cur = prev[entry.target!];
          if (!cur) return prev;
          return {
            ...prev,
            [entry.target!]: { ...cur, hp: Math.max(0, cur.hp - entry.amount!), hit: true },
          };
        });
        window.setTimeout(() => {
          setUnits((prev) => {
            const cur = prev[entry.target!];
            if (!cur) return prev;
            return { ...prev, [entry.target!]: { ...cur, hit: false } };
          });
        }, 280);
      } else if (entry.kind === "heal" && entry.target && entry.amount != null) {
        setUnits((prev) => {
          const cur = prev[entry.target!];
          if (!cur) return prev;
          return {
            ...prev,
            [entry.target!]: { ...cur, hp: Math.min(cur.maxHp, cur.hp + entry.amount!) },
          };
        });
      } else if (entry.kind === "status" && entry.amount && entry.actor) {
        setUnits((prev) => {
          const cur = prev[entry.actor!];
          if (!cur) return prev;
          return {
            ...prev,
            [entry.actor!]: { ...cur, hp: Math.max(0, cur.hp - entry.amount!) },
          };
        });
      }

      setLogIdx((i) => i + 1);
    }, delay);

    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
    };
  }, [logIdx, result.log]);

  const skip = () => {
    // Apply all remaining state
    setUnits(() => {
      const map: Record<string, { hp: number; maxHp: number }> = {};
      for (const u of [...allies, ...enemies]) map[u.uid] = { hp: u.hp, maxHp: u.maxHp };
      for (const l of result.log) {
        if (l.target && l.amount != null && l.kind === "attack") {
          const cur = map[l.target];
          if (cur) map[l.target] = { ...cur, hp: Math.max(0, cur.hp - l.amount) };
        } else if (l.target && l.amount != null && l.kind === "heal") {
          const cur = map[l.target];
          if (cur) map[l.target] = { ...cur, hp: Math.min(cur.maxHp, cur.hp + l.amount) };
        } else if (l.actor && l.amount != null && l.kind === "status") {
          const cur = map[l.actor];
          if (cur) map[l.actor] = { ...cur, hp: Math.max(0, cur.hp - l.amount) };
        }
      }
      return map;
    });
    setLogIdx(result.log.length);
    setDone(true);
  };

  return (
    <Modal onClose={onClose} title={done ? (result.winner === "ally" ? "Victory!" : "Defeat") : "Battle"}>
      <div className="battle-stage">
        <div className="battleground">
          <div className="lineup-field">
            {[0, 1, 2, 3, 4].map((slot) => {
              const u = allies.find((x) => x.slot === slot);
              if (!u) return <div key={slot} className="lineup-slot" />;
              return <UnitView key={u.uid} u={u} view={units[u.uid]} />;
            })}
          </div>
          <div className="lineup-field">
            {[0, 1, 2, 3, 4].map((slot) => {
              const u = enemies.find((x) => x.slot === slot);
              if (!u) return <div key={slot} className="lineup-slot" />;
              return <UnitView key={u.uid} u={u} view={units[u.uid]} mirror />;
            })}
          </div>
        </div>
        <div className="battle-log">
          {result.log.slice(0, logIdx).map((l, i) => (
            <div key={i} className={`line ${l.kind}`}>
              · {l.text}
            </div>
          ))}
        </div>
        <div className="row" style={{ justifyContent: "flex-end" }}>
          {!done ? (
            <button className="btn" onClick={skip}>
              Skip ⏩
            </button>
          ) : (
            <button className="btn primary" onClick={onClose}>
              Continue
            </button>
          )}
        </div>
      </div>
    </Modal>
  );
}

function UnitView({
  u,
  view,
  mirror,
}: {
  u: BattleUnit;
  view?: { hp: number; maxHp: number; hit?: boolean };
  mirror?: boolean;
}) {
  const hp = view?.hp ?? u.hp;
  const maxHp = view?.maxHp ?? u.maxHp;
  const pct = Math.max(0, (hp / maxHp) * 100);
  const low = pct < 33;
  const dead = hp <= 0;
  return (
    <div
      className={`battle-unit ${dead ? "dead" : ""} ${view?.hit ? "hit" : ""}`}
      style={{
        background: `linear-gradient(${mirror ? 200 : 160}deg, ${u.portraitGradient[0]}, ${u.portraitGradient[1]})`,
        border: `1px solid ${u.signatureColor}66`,
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11 }}>
        <strong>{u.name}</strong>
        <span>{u.class}</span>
      </div>
      <div className="big-emoji">{u.emoji}</div>
      <div className={`hp-bar ${low ? "low" : ""}`}>
        <div className="fill" style={{ width: `${pct}%` }} />
      </div>
      <div style={{ fontSize: 11, textAlign: "right" }}>
        {Math.round(hp).toLocaleString()} / {Math.round(maxHp).toLocaleString()}
      </div>
    </div>
  );
}
