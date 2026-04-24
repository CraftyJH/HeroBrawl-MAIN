import { FACTIONS } from "../game/factions";
import { CLASSES } from "../game/classes";
import { wipeSave } from "../game/state";
import { useGame } from "../store/useGame";

export function HelpPage() {
  const { reset, state } = useGame();
  return (
    <div className="page">
      <h1>HeroBrawl Guide</h1>
      <p className="sub">
        A distilled, jargon-free guide to the game — and where it diverges from Idle Heroes and TapTap Heroes.
      </p>

      <div className="grid grid-2">
        <div className="card">
          <h3>Core Loop</h3>
          <ol style={{ paddingLeft: 18, lineHeight: 1.8, color: "var(--text-dim)" }}>
            <li>Summon heroes with scrolls or prophet orbs.</li>
            <li>Assign 5 to your lineup (front 1-3, back 4-5).</li>
            <li>Clear campaign stages to gain gold, spirit, and XP.</li>
            <li>Claim idle rewards every few hours (12h cap).</li>
            <li>Level + ascend heroes; fight arena for gems; repeat.</li>
          </ol>
        </div>
        <div className="card">
          <h3>What's Better Than Idle Heroes / TapTap Heroes</h3>
          <ul style={{ paddingLeft: 18, lineHeight: 1.8, color: "var(--text-dim)" }}>
            <li><strong>Transparent pity</strong> with a live counter (60-pull guaranteed 5★).</li>
            <li><strong>Echo stacks</strong>: duplicates make heroes stronger without sacrifice.</li>
            <li><strong>12h idle cap</strong> (+50% accrual) vs. 8h elsewhere.</li>
            <li><strong>No VIP paywall</strong> — every hero reachable F2P.</li>
            <li><strong>Daily quest bonus</strong> is bigger and everyone can finish it.</li>
            <li><strong>Seasonal roster preserved</strong> — no wiping your collection.</li>
            <li><strong>Clear faction wheel</strong> — you always know what counters what.</li>
          </ul>
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3>Factions & Counters</h3>
        <div className="grid grid-2">
          {Object.values(FACTIONS).map((f) => (
            <div
              key={f.id}
              className="tile"
              style={{
                borderColor: `${f.accent}55`,
                background: `linear-gradient(160deg, ${f.color}20, ${f.accent}10)`,
              }}
            >
              <span className="label">{f.name}</span>
              <span style={{ color: "var(--text-dim)", fontSize: 13 }}>{f.lore}</span>
              <span className="float-info">
                Strong vs <strong>{f.strongVs.map((id) => FACTIONS[id].name).join(", ") || "—"}</strong>
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3>Classes</h3>
        <div className="grid grid-3">
          {Object.values(CLASSES).map((c) => (
            <div key={c.id} className="tile">
              <span className="label">{c.name}</span>
              <span style={{ fontSize: 13, color: "var(--text-dim)" }}>{c.description}</span>
              <span className="float-info">Role: {c.role}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3>Progression Cheatsheet</h3>
        <ul style={{ paddingLeft: 18, lineHeight: 1.8, color: "var(--text-dim)" }}>
          <li><strong>Level Up</strong> — gold cost. Instant via the roster screen.</li>
          <li><strong>Ascend</strong> — shards + spirit + gold. Beyond 5★ also needs 2 echo stacks.</li>
          <li><strong>Gear Up</strong> — gold + shards. Up to Tier 6.</li>
          <li><strong>Stones</strong> (coming soon) — attack/health/speed/etc.</li>
        </ul>
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3>Save / Settings</h3>
        <p className="float-info">
          Your save lives in your browser's local storage. Started {new Date(state.createdAt).toLocaleString()}.
        </p>
        <div className="row">
          <button
            className="btn"
            onClick={() => {
              if (!confirm("Reset your entire save? This cannot be undone.")) return;
              wipeSave();
              reset();
            }}
          >
            Reset Save
          </button>
        </div>
      </div>
    </div>
  );
}
