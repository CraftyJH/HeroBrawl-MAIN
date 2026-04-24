import { useMemo, useState } from "react";
import { useGame } from "../store/useGame";
import { HeroCard } from "../components/HeroCard";
import { Modal } from "../components/Modal";
import { HEROES_BY_ID } from "../game/heroes";
import { FACTIONS } from "../game/factions";
import { CLASSES } from "../game/classes";
import { computeStats, powerOf, xpToNext, goldToLevel } from "../game/stats";
import {
  canLevelUp,
  doLevelUp,
  canAscend,
  doAscend,
  ascensionCost,
  canUpgradeGear,
  doUpgradeGear,
  gearCost,
} from "../game/progression";
import { bumpQuest } from "../game/quests";
import type { HeroInstance, FactionId, ClassId } from "../types";

type FilterFaction = FactionId | "all";
type FilterClass = ClassId | "all";
type SortKey = "power" | "level" | "stars" | "name";

export function RosterPage() {
  const { state, update, notify } = useGame();
  const [selected, setSelected] = useState<string | null>(null);
  const [picking, setPicking] = useState<number | null>(null);
  const [fFac, setFFac] = useState<FilterFaction>("all");
  const [fCls, setFCls] = useState<FilterClass>("all");
  const [sort, setSort] = useState<SortKey>("power");

  const sortedHeroes = useMemo(() => {
    const arr = [...state.heroes];
    arr.sort((a, b) => {
      if (sort === "power") return powerOf(b) - powerOf(a);
      if (sort === "level") return b.level - a.level;
      if (sort === "stars") return b.stars - a.stars;
      return HEROES_BY_ID[a.templateId].name.localeCompare(HEROES_BY_ID[b.templateId].name);
    });
    return arr;
  }, [state.heroes, sort]);

  const filteredHeroes = useMemo(
    () =>
      sortedHeroes.filter((h) => {
        const t = HEROES_BY_ID[h.templateId];
        if (fFac !== "all" && t.faction !== fFac) return false;
        if (fCls !== "all" && t.class !== fCls) return false;
        return true;
      }),
    [sortedHeroes, fFac, fCls]
  );

  const selectedHero = selected ? state.heroes.find((h) => h.instanceId === selected) : null;

  const assignSlot = (slot: number, instanceId: string | null) => {
    update((s) => {
      const slots = [...s.lineup.slots];
      for (let i = 0; i < slots.length; i++) {
        if (slots[i] === instanceId) slots[i] = null;
      }
      slots[slot] = instanceId;
      return { ...s, lineup: { slots } };
    });
  };

  return (
    <div className="page">
      <h1>Heroes</h1>
      <p className="sub">
        {state.heroes.length} collected · Tap a hero to level/ascend; tap a slot to assign.
      </p>

      <div className="card" style={{ marginBottom: 16 }}>
        <h3>Battle Lineup (5 slots)</h3>
        <div className="lineup-editor">
          {state.lineup.slots.map((id, i) => {
            const h = id ? state.heroes.find((x) => x.instanceId === id) : null;
            return (
              <div
                key={i}
                className={`lineup-slot-edit ${h ? "filled" : ""}`}
                onClick={() => setPicking(i)}
                title={i < 3 ? "Front row" : "Back row"}
              >
                {h ? (
                  <HeroCard hero={h} compact />
                ) : (
                  <span>
                    {i < 3 ? "🛡️ Front" : "🏹 Back"}
                    <br />
                    Slot {i + 1}
                  </span>
                )}
              </div>
            );
          })}
        </div>
      </div>

      <div className="filters">
        <button className={`chip ${fFac === "all" ? "active" : ""}`} onClick={() => setFFac("all")}>
          All factions
        </button>
        {Object.values(FACTIONS).map((f) => (
          <button
            key={f.id}
            className={`chip ${fFac === f.id ? "active" : ""}`}
            onClick={() => setFFac(f.id)}
            style={
              fFac === f.id
                ? { background: f.color, color: "black", borderColor: f.accent }
                : undefined
            }
          >
            {f.name}
          </button>
        ))}
      </div>
      <div className="filters">
        <button className={`chip ${fCls === "all" ? "active" : ""}`} onClick={() => setFCls("all")}>
          All classes
        </button>
        {Object.values(CLASSES).map((c) => (
          <button
            key={c.id}
            className={`chip ${fCls === c.id ? "active" : ""}`}
            onClick={() => setFCls(c.id)}
          >
            {c.name}
          </button>
        ))}
      </div>
      <div className="filters">
        {(["power", "level", "stars", "name"] as SortKey[]).map((s) => (
          <button
            key={s}
            className={`chip ${sort === s ? "active" : ""}`}
            onClick={() => setSort(s)}
          >
            Sort · {s}
          </button>
        ))}
      </div>

      {filteredHeroes.length === 0 ? (
        <div className="card empty">
          No heroes match this filter. Head to the Summon page to recruit.
        </div>
      ) : (
        <div className="grid grid-heroes">
          {filteredHeroes.map((h) => (
            <HeroCard
              key={h.instanceId}
              hero={h}
              onClick={() => setSelected(h.instanceId)}
              selected={selected === h.instanceId}
            />
          ))}
        </div>
      )}

      {picking !== null && (
        <Modal onClose={() => setPicking(null)} title={`Assign slot ${picking + 1}`}>
          <div className="row" style={{ marginBottom: 10 }}>
            <button
              className="btn ghost"
              onClick={() => {
                assignSlot(picking, null);
                setPicking(null);
              }}
            >
              Clear slot
            </button>
          </div>
          <div className="grid grid-heroes">
            {sortedHeroes.map((h) => (
              <HeroCard
                key={h.instanceId}
                hero={h}
                onClick={() => {
                  assignSlot(picking, h.instanceId);
                  setPicking(null);
                }}
              />
            ))}
          </div>
        </Modal>
      )}

      {selectedHero && (
        <Modal
          onClose={() => setSelected(null)}
          title={`${HEROES_BY_ID[selectedHero.templateId].name}, ${HEROES_BY_ID[selectedHero.templateId].title}`}
        >
          <HeroDetails
            hero={selectedHero}
            onLevel={() => {
              if (!canLevelUp(state, selectedHero)) return;
              update((s) => doLevelUp(s, selectedHero.instanceId));
              update((s) => bumpQuest(s, "level-hero", 1));
              notify(`Leveled up ${HEROES_BY_ID[selectedHero.templateId].name}!`, "success");
            }}
            onAscend={() => {
              if (!canAscend(state, selectedHero)) return;
              update((s) => doAscend(s, selectedHero.instanceId));
              notify(`${HEROES_BY_ID[selectedHero.templateId].name} ascended!`, "reward");
            }}
            onGear={() => {
              if (!canUpgradeGear(state, selectedHero)) return;
              update((s) => doUpgradeGear(s, selectedHero.instanceId));
              notify("Gear upgraded.", "success");
            }}
          />
        </Modal>
      )}
    </div>
  );
}

function HeroDetails({
  hero,
  onLevel,
  onAscend,
  onGear,
}: {
  hero: HeroInstance;
  onLevel: () => void;
  onAscend: () => void;
  onGear: () => void;
}) {
  const { state } = useGame();
  const t = HEROES_BY_ID[hero.templateId];
  const faction = FACTIONS[t.faction];
  const cls = CLASSES[t.class];
  const stats = computeStats(hero);
  const nextXp = xpToNext(hero.level);
  const lvCost = goldToLevel(hero.level);
  const asc = ascensionCost(hero.stars);
  const gc = gearCost(hero.gearTier);

  return (
    <div>
      <div className="hero-banner">
        <div
          className="portrait-xl"
          style={{
            background: `linear-gradient(160deg, ${t.portraitGradient[0]}, ${t.portraitGradient[1]})`,
          }}
        >
          {t.emoji}
        </div>
        <div>
          <div className="meta">
            <span className="badge faction" style={{ background: faction.color }}>
              {faction.name}
            </span>{" "}
            <span className="badge class">{cls.name}</span>{" "}
            <span style={{ color: "var(--rarity-5)" }}>{"★".repeat(hero.stars)}</span>
          </div>
          <div style={{ color: "var(--text-dim)", marginBottom: 8, fontStyle: "italic" }}>
            "{t.bio}"
          </div>
          <div className="stat-row">
            <span>Level</span>
            <strong>{hero.level}</strong>
          </div>
          <div className="stat-row">
            <span>Power</span>
            <strong>⚡ {powerOf(hero).toLocaleString()}</strong>
          </div>
          <div className="stat-row">
            <span>Echo Stacks</span>
            <strong>{hero.echoStacks}</strong>
          </div>
          <div className="stat-row">
            <span>Gear Tier</span>
            <strong>{"◆".repeat(hero.gearTier)}{"◇".repeat(6 - hero.gearTier)}</strong>
          </div>
        </div>
      </div>

      <div className="grid grid-2" style={{ marginTop: 14 }}>
        <div className="card">
          <h3>Stats</h3>
          <div className="stat-row"><span>Attack</span><strong>{stats.attack.toLocaleString()}</strong></div>
          <div className="stat-row"><span>Health</span><strong>{stats.health.toLocaleString()}</strong></div>
          <div className="stat-row"><span>Armor</span><strong>{stats.armor.toLocaleString()}</strong></div>
          <div className="stat-row"><span>Speed</span><strong>{stats.speed}</strong></div>
          <div className="stat-row"><span>Willpower</span><strong>{stats.willpower}</strong></div>
          <div className="float-info" style={{ marginTop: 6 }}>Next level at {nextXp} xp (auto from campaign).</div>
        </div>
        <div className="card">
          <h3>Skills</h3>
          {t.skills.map((sk) => (
            <div key={sk.id} style={{ marginBottom: 10 }}>
              <div style={{ fontWeight: 700 }}>
                {sk.kind === "active" ? "⚡ " : "✨ "}
                {sk.name}
              </div>
              <div className="float-info">{sk.description}</div>
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-3" style={{ marginTop: 14 }}>
        <button className="btn primary" disabled={!canLevelUp(state, hero)} onClick={onLevel}>
          Level Up · 🪙 {lvCost.toLocaleString()}
        </button>
        <button className="btn gold" disabled={!canAscend(state, hero)} onClick={onAscend}>
          Ascend · 🔹 {asc.shards} · ✨ {asc.spirit}
        </button>
        <button className="btn" disabled={!canUpgradeGear(state, hero)} onClick={onGear}>
          Gear Up · 🪙 {gc.gold.toLocaleString()} · 🔹 {gc.shards}
        </button>
      </div>
      {hero.stars >= 5 && hero.echoStacks < 2 && (
        <div className="float-info" style={{ marginTop: 8, color: "var(--gold)" }}>
          Ascending beyond 5★ requires 2+ echo stacks (gained from duplicates).
        </div>
      )}
    </div>
  );
}
