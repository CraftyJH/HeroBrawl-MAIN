import type { HeroInstance } from "../types";
import { FACTIONS } from "../game/factions";
import { CLASSES } from "../game/classes";
import { HEROES_BY_ID } from "../game/heroes";
import { powerOf } from "../game/stats";

export function HeroCard({
  hero,
  onClick,
  selected,
  compact,
}: {
  hero: HeroInstance;
  onClick?: () => void;
  selected?: boolean;
  compact?: boolean;
}) {
  const t = HEROES_BY_ID[hero.templateId];
  if (!t) return null;
  const faction = FACTIONS[t.faction];
  const cls = CLASSES[t.class];
  const [c1, c2] = t.portraitGradient;

  return (
    <div
      className={`hero-card ${selected ? "selected" : ""}`}
      onClick={onClick}
      role={onClick ? "button" : undefined}
    >
      <div
        className="portrait"
        style={{
          background: `linear-gradient(160deg, ${c1}, ${c2})`,
        }}
      >
        <span style={{ position: "relative", zIndex: 1 }}>{t.emoji}</span>
        <div className="badges">
          <span className="badge faction" style={{ background: faction.color }}>
            {faction.name}
          </span>
          <span className="badge class">{cls.name}</span>
        </div>
        <div className="rarity">Lv {hero.level}</div>
        <div className="stars">{"★".repeat(hero.stars)}</div>
      </div>
      {!compact && (
        <div className="footer">
          <div>
            <div className="hero-name">{t.name}</div>
            <div className="hero-title">{t.title}</div>
          </div>
          <div style={{ color: "var(--text-dim)", fontSize: 11 }}>
            ⚡ {powerOf(hero).toLocaleString()}
          </div>
        </div>
      )}
    </div>
  );
}
