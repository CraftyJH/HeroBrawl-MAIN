import { useEffect, useMemo, useState } from "react";
import { useGame } from "../store/useGame";
import { HEROES_BY_RARITY } from "../game/heroes";
import { unitFromInstance, simulateBattle, buildAllyUnits } from "../game/combat";
import type { BattleResult, BattleUnit, FactionId, HeroInstance, Rarity } from "../types";
import { BattleStage } from "../components/BattleStage";
import { bumpQuest } from "../game/quests";
import { isoDay, pushMessage } from "../game/state";
import { FACTIONS } from "../game/factions";

const DAILY_TICKETS = 10;

interface Opponent {
  id: string;
  name: string;
  rating: number;
  heroes: HeroInstance[];
  faction: FactionId;
}

function seedOpponents(rating: number): Opponent[] {
  const list: Opponent[] = [];
  const namePool = [
    "Crown of Daggers",
    "Ironhand Guild",
    "Silver Tempest",
    "Ember Covenant",
    "Nightglass Circle",
    "Wildsong Pack",
    "Moonbreaker Clan",
    "Veilwatcher Host",
  ];
  for (let i = 0; i < 5; i++) {
    const delta = Math.round((Math.random() * 240 - 60) + i * 10);
    const target = Math.max(600, rating + delta);

    const factions = Object.keys(FACTIONS) as FactionId[];
    const baseFac = factions[Math.floor(Math.random() * factions.length)];

    // Pick 5 heroes, with power scaling by rating.
    const pulls: HeroInstance[] = [];
    for (let s = 0; s < 5; s++) {
      const r = (Math.random() < 0.25 ? 5 : Math.random() < 0.55 ? 4 : 3) as Rarity;
      const pool = HEROES_BY_RARITY[r];
      const t = pool[Math.floor(Math.random() * pool.length)];
      const level = Math.max(1, Math.round(10 + target / 40 + Math.random() * 5));
      const stars = r;
      pulls.push({
        instanceId: `op-${i}-${s}`,
        templateId: t.id,
        level,
        stars,
        xp: 0,
        echoStacks: 0,
        gearTier: Math.min(6, Math.floor(target / 350)),
      });
    }
    list.push({
      id: `op-${i}`,
      name: namePool[(i + Math.floor(rating / 100)) % namePool.length],
      rating: target,
      heroes: pulls,
      faction: baseFac,
    });
  }
  return list.sort((a, b) => a.rating - b.rating);
}

export function ArenaPage() {
  const { state, update, notify } = useGame();
  const [opponents, setOpponents] = useState<Opponent[]>(() => seedOpponents(state.arena.rating));
  const [battle, setBattle] = useState<{ allies: BattleUnit[]; enemies: BattleUnit[]; result: BattleResult; opponent: Opponent } | null>(null);
  const [today, setToday] = useState(state.arena.ticketDay);

  useEffect(() => {
    const t = window.setInterval(() => setToday(isoDay(Date.now())), 30_000);
    // Fire once shortly after mount to establish the real "today".
    const first = window.setTimeout(() => setToday(isoDay(Date.now())), 0);
    return () => {
      window.clearInterval(t);
      window.clearTimeout(first);
    };
  }, []);
  const ticketsUsedToday = state.arena.ticketDay === today ? state.arena.ticketsUsed : 0;
  const ticketsLeft = Math.max(0, DAILY_TICKETS - ticketsUsedToday);

  const allyCount = state.lineup.slots.filter(Boolean).length;

  const fight = (op: Opponent) => {
    if (ticketsLeft <= 0) {
      notify("No arena tickets left today. Come back tomorrow!", "warn");
      return;
    }
    if (allyCount === 0) {
      notify("Set up a lineup first.", "warn");
      return;
    }
    const allies = buildAllyUnits(state);
    const enemies: BattleUnit[] = op.heroes.map((h, slot) => unitFromInstance(h, "enemy", slot));
    const result = simulateBattle(allies, enemies);
    setBattle({ allies, enemies, result, opponent: op });
  };

  const concludeBattle = (result: BattleResult, opponent: Opponent) => {
    const delta = result.winner === "ally"
      ? Math.max(6, Math.round(20 + (opponent.rating - state.arena.rating) / 8))
      : -Math.max(4, Math.round(15 - (opponent.rating - state.arena.rating) / 10));

    update((s) => {
      const baseTickets = s.arena.ticketDay === today ? s.arena.ticketsUsed : 0;
      let next = {
        ...s,
        arena: {
          ...s.arena,
          rating: Math.max(300, s.arena.rating + delta),
          wins: s.arena.wins + (result.winner === "ally" ? 1 : 0),
          losses: s.arena.losses + (result.winner === "ally" ? 0 : 1),
          lastFight: Date.now(),
          ticketsUsed: baseTickets + 1,
          ticketDay: today,
        },
        currency: {
          ...s.currency,
          gems: s.currency.gems + (result.winner === "ally" ? 12 : 4),
          prophetOrbs: s.currency.prophetOrbs + (result.winner === "ally" ? 2 : 0),
        },
      };
      next = bumpQuest(next, "arena-2", 1);
      if (result.winner === "ally") {
        next = pushMessage(next, `Arena win vs ${opponent.name}. Rating ${s.arena.rating} → ${next.arena.rating}.`, "success");
      } else {
        next = pushMessage(next, `Arena loss vs ${opponent.name}. Rating ${s.arena.rating} → ${next.arena.rating}.`, "warn");
      }
      return next;
    });

    // re-seed opponents from new rating
    setOpponents(seedOpponents(state.arena.rating + delta));
  };

  const refresh = () => {
    if (state.currency.gems < 10) {
      notify("Need 10 gems to refresh opponents.", "warn");
      return;
    }
    update((s) => ({ ...s, currency: { ...s.currency, gems: s.currency.gems - 10 } }));
    setOpponents(seedOpponents(state.arena.rating));
  };

  const winRate = useMemo(() => {
    const total = state.arena.wins + state.arena.losses;
    if (total === 0) return "—";
    return `${Math.round((state.arena.wins / total) * 100)}%`;
  }, [state.arena.wins, state.arena.losses]);

  return (
    <div className="page">
      <h1>Arena</h1>
      <p className="sub">
        Rating <strong>{state.arena.rating}</strong> · Win rate {winRate} · {ticketsLeft}/{DAILY_TICKETS} tickets today
      </p>

      <div className="row" style={{ marginBottom: 12 }}>
        <button className="btn" onClick={refresh}>Refresh Opponents · 💎 10</button>
      </div>

      <div className="grid grid-2">
        {opponents.map((op) => (
          <div key={op.id} className="card">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <div>
                <div style={{ fontWeight: 800, fontSize: 16 }}>{op.name}</div>
                <div className="float-info">Rating {op.rating} · {FACTIONS[op.faction].name}-leaning</div>
              </div>
              <span
                className="badge"
                style={{
                  background:
                    op.rating > state.arena.rating + 50
                      ? "rgba(255, 79, 157, 0.3)"
                      : op.rating < state.arena.rating - 50
                      ? "rgba(125, 245, 197, 0.25)"
                      : "var(--bg-3)",
                }}
              >
                {op.rating > state.arena.rating + 50
                  ? "HARD"
                  : op.rating < state.arena.rating - 50
                  ? "EASY"
                  : "EVEN"}
              </span>
            </div>
            <div style={{ display: "flex", gap: 6, marginTop: 10 }}>
              {op.heroes.map((h) => (
                <MiniHero key={h.instanceId} h={h} />
              ))}
            </div>
            <div className="row" style={{ marginTop: 12 }}>
              <button className="btn primary" disabled={ticketsLeft <= 0 || allyCount === 0} onClick={() => fight(op)}>
                Fight!
              </button>
              <span className="float-info">Win: +12 💎, +2 🔮 · Lose: +4 💎</span>
            </div>
          </div>
        ))}
      </div>

      {battle && (
        <BattleStage
          allies={battle.allies}
          enemies={battle.enemies}
          result={battle.result}
          onClose={() => {
            concludeBattle(battle.result, battle.opponent);
            setBattle(null);
          }}
        />
      )}
    </div>
  );
}

function MiniHero({ h }: { h: HeroInstance }) {
  const t = HEROES_BY_RARITY[h.stars]?.find((x) => x.id === h.templateId);
  const emoji = t?.emoji ?? "❔";
  const [c1, c2] = t?.portraitGradient ?? ["#333", "#555"];
  return (
    <div
      style={{
        width: 44,
        height: 44,
        borderRadius: 8,
        background: `linear-gradient(160deg, ${c1}, ${c2})`,
        display: "grid",
        placeItems: "center",
        fontSize: 20,
        border: "1px solid var(--stroke)",
      }}
      title={`${t?.name} Lv${h.level} ${"★".repeat(h.stars)}`}
    >
      {emoji}
    </div>
  );
}
