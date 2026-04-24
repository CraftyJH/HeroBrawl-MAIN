import { useGame } from "../store/useGame";
import {
  DAILY_QUESTS,
  COMPLETION_REWARD,
  canClaimQuest,
  claimQuest,
  canClaimCompletion,
  claimCompletion,
} from "../game/quests";

export function QuestsPage() {
  const { state, update, notify } = useGame();

  const completion = canClaimCompletion(state);

  return (
    <div className="page">
      <h1>Daily Quests</h1>
      <p className="sub">
        Resets at midnight (UTC). Clear them all for a chunky bonus. No VIP wall.
      </p>

      <div className="card" style={{ marginBottom: 16 }}>
        <h3>All-Clear Bonus</h3>
        <p className="float-info">
          Completing every daily quest grants <strong>+{COMPLETION_REWARD.gems} 💎</strong> and{" "}
          <strong>+{COMPLETION_REWARD.scrolls} 📜</strong>.
        </p>
        <button
          className="btn gold"
          disabled={!completion}
          onClick={() => {
            update((s) => claimCompletion(s));
            notify("All quests complete! Bonus claimed.", "reward");
          }}
        >
          {state.quests.completionClaimed ? "Already claimed" : completion ? "Claim bonus" : "Complete all quests first"}
        </button>
      </div>

      <div className="grid">
        {DAILY_QUESTS.map((q) => {
          const progress = state.quests.progress[q.id] ?? 0;
          const pct = Math.min(100, (progress / q.target) * 100);
          const claimed = !!state.quests.claimed[q.id];
          const canClaim = canClaimQuest(state, q.id);
          return (
            <div key={q.id} className="quest">
              <div>
                <div className="title">{q.name}</div>
                <div className="desc">{q.description}</div>
                <div className="progress">
                  <div className="fill" style={{ width: `${pct}%` }} />
                </div>
                <div className="rewards" style={{ marginTop: 6 }}>
                  <span>Progress: {progress}/{q.target}</span>
                  {q.rewards.gems && <span>💎 {q.rewards.gems}</span>}
                  {q.rewards.gold && <span>🪙 {q.rewards.gold}</span>}
                  {q.rewards.scrolls && <span>📜 {q.rewards.scrolls}</span>}
                  {q.rewards.dust && <span>🧪 {q.rewards.dust}</span>}
                  {q.rewards.orbs && <span>🔮 {q.rewards.orbs}</span>}
                </div>
              </div>
              <div>
                <button
                  className={`btn ${canClaim ? "primary" : ""}`}
                  disabled={!canClaim}
                  onClick={() => update((s) => claimQuest(s, q.id))}
                >
                  {claimed ? "Claimed" : canClaim ? "Claim" : "Incomplete"}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
