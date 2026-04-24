import { useState } from "react";
import { useGame } from "../store/useGame";
import { performPull, type PullResult } from "../game/gacha";
import { Modal } from "../components/Modal";
import { FACTIONS } from "../game/factions";
import { bumpQuest } from "../game/quests";

export function SummonPage() {
  const { state, update, notify } = useGame();
  const [showResults, setShowResults] = useState<PullResult[] | null>(null);

  const doPull = (count: 1 | 10, kind: "heroic" | "prophet") => {
    update((s) => {
      if (kind === "heroic") {
        if (s.currency.heroicScrolls < count) {
          notify("Not enough Heroic Scrolls.", "warn");
          return s;
        }
        const { results, updatedState } = performPull(s, { count, kind });
        setShowResults(results);
        const epics = results.filter((r) => r.rarity >= 5).length;
        if (epics > 0) notify(`Summoned ${epics} legendary hero${epics > 1 ? "es" : ""}!`, "reward");
        return bumpQuest(
          {
            ...updatedState,
            currency: {
              ...updatedState.currency,
              heroicScrolls: updatedState.currency.heroicScrolls - count,
            },
          },
          "pulls-1",
          count
        );
      } else {
        if (s.currency.prophetOrbs < count) {
          notify("Not enough Prophet Orbs.", "warn");
          return s;
        }
        const { results, updatedState } = performPull(s, { count, kind });
        setShowResults(results);
        return bumpQuest(
          {
            ...updatedState,
            currency: {
              ...updatedState.currency,
              prophetOrbs: updatedState.currency.prophetOrbs - count,
            },
          },
          "pulls-1",
          count
        );
      }
    });
  };

  const pityCount = state.gacha.pityCount;
  const toPity = 60 - pityCount;

  return (
    <div className="page">
      <h1>Summon</h1>
      <p className="sub">
        Pity progress: <strong>{pityCount}/60</strong> · {toPity} pulls until guaranteed legendary.
      </p>

      <div className="summon-stage">
        <div className="summon-banner">
          <small style={{ letterSpacing: "0.2em", color: "rgba(255,255,255,0.7)" }}>
            HEROIC SUMMON
          </small>
          <h2>Forge of Legends</h2>
          <p style={{ color: "rgba(255,255,255,0.8)", margin: 0 }}>
            The core gacha. 5★ rate ramps hard after 30 pulls; guaranteed 5★ at 60.
          </p>
          <div className="rates">
            <div className="stat">
              <strong>7%</strong>
              <small>5★ base</small>
            </div>
            <div className="stat">
              <strong>28%</strong>
              <small>4★</small>
            </div>
            <div className="stat">
              <strong>65%</strong>
              <small>3★</small>
            </div>
          </div>
          <div className="summon-buttons">
            <button
              className="btn"
              disabled={state.currency.heroicScrolls < 1}
              onClick={() => doPull(1, "heroic")}
            >
              Summon x1 · 📜 1
            </button>
            <button
              className="btn primary"
              disabled={state.currency.heroicScrolls < 10}
              onClick={() => doPull(10, "heroic")}
            >
              Summon x10 · 📜 10
            </button>
          </div>
          <div className="progress" style={{ marginTop: 8 }}>
            <div className="fill" style={{ width: `${(pityCount / 60) * 100}%` }} />
          </div>
          <small style={{ color: "rgba(255,255,255,0.7)" }}>
            Every 10th pull is a guaranteed 4★+.
          </small>
        </div>

        <div className="summon-banner" style={{ background: "linear-gradient(135deg, rgba(255, 208, 74, 0.2), rgba(255, 154, 209, 0.2))" }}>
          <small style={{ letterSpacing: "0.2em", color: "rgba(255,255,255,0.7)" }}>
            PROPHET SUMMON
          </small>
          <h2>Oracle's Tree</h2>
          <p style={{ color: "rgba(255,255,255,0.8)", margin: 0 }}>
            Choose a faction, summon only from it. Perfect for filling a mono-faction aura.
          </p>
          <FactionPicker />
        </div>
      </div>

      {showResults && (
        <Modal onClose={() => setShowResults(null)} title="Summon Results">
          <div className="pull-results">
            {showResults.map((r, i) => (
              <div
                key={i}
                className={`pull-result ${r.isDuplicate ? "dup" : "new"}`}
                style={{
                  borderColor: r.rarity >= 5 ? "var(--rarity-5)" : r.rarity >= 4 ? "var(--rarity-4)" : "var(--stroke)",
                  background: `linear-gradient(180deg, ${r.template.portraitGradient[0]}55, ${r.template.portraitGradient[1]}22)`,
                }}
              >
                <div className="r">{r.template.emoji}</div>
                <div className="nm">{r.template.name}</div>
                <div className="tt">
                  {"★".repeat(r.rarity)} · {r.template.title}
                </div>
              </div>
            ))}
          </div>
          <div className="actions">
            <button className="btn primary" onClick={() => setShowResults(null)}>
              Awesome!
            </button>
          </div>
        </Modal>
      )}
    </div>
  );

  function FactionPicker() {
    const [sel, setSel] = useState<keyof typeof FACTIONS>("vanguard");
    return (
      <>
        <div className="filters" style={{ margin: "6px 0" }}>
          {Object.values(FACTIONS).map((f) => (
            <button
              key={f.id}
              className={`chip ${sel === f.id ? "active" : ""}`}
              onClick={() => setSel(f.id)}
              style={
                sel === f.id
                  ? { background: f.color, color: "black", borderColor: f.accent }
                  : undefined
              }
            >
              {f.name}
            </button>
          ))}
        </div>
        <div className="summon-buttons">
          <button
            className="btn"
            disabled={state.currency.prophetOrbs < 1}
            onClick={() =>
              update((s) => {
                if (s.currency.prophetOrbs < 1) return s;
                const { results, updatedState } = performPull(s, {
                  count: 1,
                  kind: "prophet",
                  factionFilter: sel,
                });
                setShowResults(results);
                return {
                  ...updatedState,
                  currency: {
                    ...updatedState.currency,
                    prophetOrbs: updatedState.currency.prophetOrbs - 1,
                  },
                };
              })
            }
          >
            Summon x1 · 🔮 1
          </button>
          <button
            className="btn gold"
            disabled={state.currency.prophetOrbs < 10}
            onClick={() =>
              update((s) => {
                if (s.currency.prophetOrbs < 10) return s;
                const { results, updatedState } = performPull(s, {
                  count: 10,
                  kind: "prophet",
                  factionFilter: sel,
                });
                setShowResults(results);
                return {
                  ...updatedState,
                  currency: {
                    ...updatedState.currency,
                    prophetOrbs: updatedState.currency.prophetOrbs - 10,
                  },
                };
              })
            }
          >
            Summon x10 · 🔮 10
          </button>
        </div>
      </>
    );
  }
}
