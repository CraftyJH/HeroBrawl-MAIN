import { useEffect, useMemo, useState } from "react";
import { useGame } from "../store/useGame";
import { claimIdleReward, computeIdleReward, upgradeIdleRates } from "../game/idle";
import { bumpQuest } from "../game/quests";
import { HeroCard } from "../components/HeroCard";
import { lineupAura } from "../game/factions";
import { HEROES_BY_ID } from "../game/heroes";

export function HomePage({ go }: { go: (k: string) => void }) {
  const { state, update, notify } = useGame();
  const [now, setNow] = useState(0);

  useEffect(() => {
    const t = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(t);
  }, []);

  const reward = useMemo(() => computeIdleReward(state, now || state.idle.tickedAt), [state, now]);

  const claim = () => {
    update((s) => {
      const next = claimIdleReward(s, Date.now());
      const rates = upgradeIdleRates(
        { gold: 1200, spirit: 60, shards: 4 },
        s.campaign.chapter,
        s.campaign.stage
      );
      return {
        ...next,
        idle: { ...next.idle, ratePerHour: rates },
      };
    });
    update((s) => bumpQuest(s, "idle-claim", 1));
    notify(
      `Claimed ${reward.gold.toLocaleString()} gold, ${reward.spirit.toLocaleString()} spirit, ${reward.shards} shards.`,
      "reward"
    );
  };

  const lineupHeroes = state.lineup.slots
    .map((id) => (id ? state.heroes.find((h) => h.instanceId === id) : null))
    .filter(Boolean);
  const aura = lineupAura(
    lineupHeroes
      .map((h) => (h ? HEROES_BY_ID[h.templateId]?.faction : null))
      .filter((f): f is NonNullable<typeof f> => Boolean(f))
  );

  const hoursUntilCap = Math.max(0, state.idle.capHours - reward.hours);

  return (
    <div className="page">
      <div className="hero-banner">
        <div
          className="portrait-xl"
          style={{
            background:
              "linear-gradient(160deg, #2b1948, #6b61ff 60%, #ff4f9d)",
          }}
        >
          ⚔️
        </div>
        <div>
          <h1>Welcome back, {state.playerName}</h1>
          <div className="meta">
            Chapter {state.campaign.chapter}, Stage {state.campaign.stage} ·
            {" "}Arena rating {state.arena.rating.toLocaleString()}
          </div>
          <div className="row">
            <button className="btn primary" onClick={() => go("campaign")}>Continue Campaign</button>
            <button className="btn" onClick={() => go("summon")}>Summon Heroes</button>
            <button className="btn" onClick={() => go("roster")}>Manage Lineup</button>
          </div>
        </div>
      </div>

      <div style={{ height: 20 }} />

      <div className="grid grid-2">
        <div className="card">
          <h3>Idle Rewards</h3>
          <p className="float-info">
            HeroBrawl caps idle at <strong>{state.idle.capHours}h</strong>
            {" "}(+50% vs Idle Heroes' 8h cap). Check in anytime.
          </p>
          <div className="tile-grid" style={{ marginTop: 8 }}>
            <div className="tile">
              <span className="label">Gold</span>
              <span className="value" style={{ color: "var(--gold)" }}>
                +{reward.gold.toLocaleString()}
              </span>
              <small className="float-info">
                {state.idle.ratePerHour.gold.toLocaleString()} / hr
              </small>
            </div>
            <div className="tile">
              <span className="label">Spirit</span>
              <span className="value" style={{ color: "var(--spirit)" }}>
                +{reward.spirit.toLocaleString()}
              </span>
              <small className="float-info">
                {state.idle.ratePerHour.spirit} / hr
              </small>
            </div>
            <div className="tile">
              <span className="label">Shards</span>
              <span className="value" style={{ color: "var(--shard)" }}>
                +{reward.shards}
              </span>
              <small className="float-info">
                {state.idle.ratePerHour.shards} / hr
              </small>
            </div>
          </div>
          <div style={{ marginTop: 10 }} className="progress">
            <div
              className="fill"
              style={{ width: `${Math.min(100, (reward.hours / state.idle.capHours) * 100)}%` }}
            />
          </div>
          <div className="float-info" style={{ marginTop: 6 }}>
            {reward.capped ? (
              <span style={{ color: "var(--gold)" }}>⚠ Idle cap reached — claim now!</span>
            ) : (
              <>Accrued {reward.hours.toFixed(2)}h · {hoursUntilCap.toFixed(2)}h until cap</>
            )}
          </div>
          <div style={{ marginTop: 12 }}>
            <button className="btn primary block" onClick={claim}>
              Claim Idle Rewards
            </button>
          </div>
        </div>

        <div className="card">
          <h3>Lineup</h3>
          <p className="float-info">{aura.description} · +{Math.round(aura.bonus.attack * 100)}% ATK, +{Math.round(aura.bonus.health * 100)}% HP</p>
          <div className="lineup-editor" style={{ maxWidth: "100%", marginTop: 10 }}>
            {state.lineup.slots.map((id, i) => {
              const h = id ? state.heroes.find((h) => h.instanceId === id) : null;
              return (
                <div key={i} className={`lineup-slot-edit ${h ? "filled" : ""}`}>
                  {h ? <HeroCard hero={h} compact /> : <span>Slot {i + 1}</span>}
                </div>
              );
            })}
          </div>
          <div style={{ marginTop: 10 }}>
            <button className="btn" onClick={() => go("roster")}>Edit Lineup →</button>
          </div>
        </div>
      </div>

      <div style={{ height: 20 }} />

      <div className="grid grid-2">
        <div className="card">
          <h3>Recent Activity</h3>
          {state.messages.length === 0 ? (
            <div className="empty">No activity yet.</div>
          ) : (
            state.messages.slice(0, 8).map((m) => (
              <div key={m.id} className={`msg ${m.kind}`}>
                {m.text}
              </div>
            ))
          )}
        </div>
        <div className="card">
          <h3>Why HeroBrawl is better</h3>
          <ul style={{ margin: 0, paddingLeft: 18, color: "var(--text-dim)", lineHeight: 1.7 }}>
            <li>🎯 <strong>Transparent pity</strong> — guaranteed legendary every 60 pulls</li>
            <li>♻️ <strong>Echo stacks</strong> — duplicates make heroes stronger, never wasted</li>
            <li>⏳ <strong>12h idle cap</strong> (up from 8h)</li>
            <li>🌟 <strong>No VIP paywall</strong> — every hero obtainable F2P</li>
            <li>⚡ <strong>Faction wheel</strong> — clear rock-paper-scissors vs opaque auras</li>
            <li>🆓 <strong>Season roster stays</strong> — no wiping your collection</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
