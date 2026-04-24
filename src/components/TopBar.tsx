import { useGame } from "../store/useGame";

export function TopBar() {
  const { state } = useGame();
  const c = state.currency;

  return (
    <div className="topbar">
      <div className="brand">
        <span className="bolt">⚔️</span>
        HeroBrawl
        <small>Beta</small>
      </div>
      <div className="spacer" />
      <div className="curr">
        <span className="pill gold" title="Gold — hero leveling & gear">
          <span className="ico">🪙</span>
          {formatNum(c.gold)}
        </span>
        <span className="pill gems" title="Gems — premium currency">
          <span className="ico">💎</span>
          {formatNum(c.gems)}
        </span>
        <span className="pill scroll" title="Heroic Scrolls — gacha pulls">
          <span className="ico">📜</span>
          {c.heroicScrolls}
        </span>
        <span className="pill orb" title="Prophet Orbs — faction summons">
          <span className="ico">🔮</span>
          {c.prophetOrbs}
        </span>
        <span className="pill spirit" title="Spirit — ascension material">
          <span className="ico">✨</span>
          {formatNum(c.spirit)}
        </span>
        <span className="pill shard" title="Shards — ascension + gear">
          <span className="ico">🔹</span>
          {formatNum(c.shards)}
        </span>
      </div>
    </div>
  );
}

function formatNum(n: number) {
  if (n >= 1e9) return (n / 1e9).toFixed(2) + "B";
  if (n >= 1e6) return (n / 1e6).toFixed(2) + "M";
  if (n >= 1e4) return (n / 1e3).toFixed(1) + "K";
  return n.toLocaleString();
}
