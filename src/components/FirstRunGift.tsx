import { useEffect, useState } from "react";
import { useGame } from "../store/useGame";
import { Modal } from "./Modal";
import { performPull } from "../game/gacha";

const FLAG = "herobrawl.firstRun.v1";

export function FirstRunGift() {
  const { state, update, notify } = useGame();
  const [show, setShow] = useState(false);

  useEffect(() => {
    const t = window.setTimeout(() => {
      if (state.heroes.length === 0 && !localStorage.getItem(FLAG)) {
        setShow(true);
      }
    }, 0);
    return () => window.clearTimeout(t);
  }, [state.heroes.length]);

  const claim = () => {
    update((s) => {
      // Guarantee 3 heroes: 1 legendary + 2 epics across factions.
      const { results: r1, updatedState: s1 } = performPull(
        { ...s, gacha: { heroicPulls: 59, pityCount: 59, sinceEpic: 9 } },
        { count: 1, kind: "heroic" }
      );
      const { results: r2, updatedState: s2 } = performPull(s1, { count: 1, kind: "heroic" });
      const { results: r3, updatedState: s3 } = performPull(s2, { count: 1, kind: "heroic" });
      const all = [...r1, ...r2, ...r3];
      const final = {
        ...s3,
        currency: {
          ...s3.currency,
          heroicScrolls: s3.currency.heroicScrolls + 15,
          gems: s3.currency.gems + 500,
          prophetOrbs: s3.currency.prophetOrbs + 20,
        },
        lineup: {
          slots: [
            s3.heroes[0]?.instanceId ?? null,
            s3.heroes[1]?.instanceId ?? null,
            s3.heroes[2]?.instanceId ?? null,
            null,
            null,
          ],
        },
      };
      // restore natural pity state (fresh)
      final.gacha = { heroicPulls: 3, pityCount: 0, sinceEpic: 0 };
      notify(
        `Starter pack opened: ${all.map((a) => a.template.name).join(", ")}!`,
        "reward"
      );
      return final;
    });
    localStorage.setItem(FLAG, "1");
    setShow(false);
  };

  if (!show) return null;
  return (
    <Modal onClose={() => {}} title="Welcome to HeroBrawl!">
      <p style={{ color: "var(--text-dim)" }}>
        Here's your starter pack — enough to field a full lineup, summon more, and explore the game right away.
      </p>
      <div className="tile-grid">
        <div className="tile">
          <span className="label">Guaranteed</span>
          <span className="value">🛡️ 3 heroes</span>
          <small className="float-info">including one guaranteed 5★</small>
        </div>
        <div className="tile">
          <span className="label">Scrolls</span>
          <span className="value" style={{ color: "var(--scroll)" }}>📜 +15</span>
        </div>
        <div className="tile">
          <span className="label">Gems</span>
          <span className="value" style={{ color: "var(--gems)" }}>💎 +500</span>
        </div>
        <div className="tile">
          <span className="label">Prophet Orbs</span>
          <span className="value" style={{ color: "var(--orb)" }}>🔮 +20</span>
        </div>
      </div>
      <div className="actions">
        <button className="btn primary" onClick={claim}>Claim & Play</button>
      </div>
    </Modal>
  );
}
