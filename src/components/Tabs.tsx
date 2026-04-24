export type TabKey =
  | "home"
  | "campaign"
  | "summon"
  | "roster"
  | "arena"
  | "quests"
  | "help";

export function Tabs({
  active,
  onChange,
}: {
  active: TabKey;
  onChange: (k: TabKey) => void;
}) {
  const tabs: { k: TabKey; label: string; icon: string }[] = [
    { k: "home", label: "Home", icon: "🏰" },
    { k: "campaign", label: "Campaign", icon: "🗺️" },
    { k: "summon", label: "Summon", icon: "✨" },
    { k: "roster", label: "Heroes", icon: "🛡️" },
    { k: "arena", label: "Arena", icon: "⚔️" },
    { k: "quests", label: "Quests", icon: "📜" },
    { k: "help", label: "Guide", icon: "📖" },
  ];
  return (
    <div className="tabs">
      {tabs.map((t) => (
        <button
          key={t.k}
          className={`tab ${active === t.k ? "active" : ""}`}
          onClick={() => onChange(t.k)}
        >
          <span style={{ marginRight: 6 }}>{t.icon}</span>
          {t.label}
        </button>
      ))}
    </div>
  );
}
