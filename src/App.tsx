import { useState } from "react";
import { GameProvider } from "./store/GameContext";
import { useGame } from "./store/useGame";
import { TopBar } from "./components/TopBar";
import { Tabs, type TabKey } from "./components/Tabs";
import { HomePage } from "./pages/HomePage";
import { SummonPage } from "./pages/SummonPage";
import { RosterPage } from "./pages/RosterPage";
import { CampaignPage } from "./pages/CampaignPage";
import { ArenaPage } from "./pages/ArenaPage";
import { QuestsPage } from "./pages/QuestsPage";
import { HelpPage } from "./pages/HelpPage";
import { FirstRunGift } from "./components/FirstRunGift";

function Shell() {
  const [tab, setTab] = useState<TabKey>("home");
  const go = (k: string) => setTab(k as TabKey);
  return (
    <div className="app">
      <TopBar />
      <Tabs active={tab} onChange={setTab} />
      <main>
        {tab === "home" && <HomePage go={go} />}
        {tab === "campaign" && <CampaignPage />}
        {tab === "summon" && <SummonPage />}
        {tab === "roster" && <RosterPage />}
        {tab === "arena" && <ArenaPage />}
        {tab === "quests" && <QuestsPage />}
        {tab === "help" && <HelpPage />}
      </main>
      <div className="footer-bar">
        HeroBrawl · A modern open-source idle RPG/gacha · Built in a cloud agent sprint · v0.1
      </div>
      <FirstRunGift />
    </div>
  );
}

function AppInner() {
  // Keep provider-aware tree mounted.
  useGame();
  return <Shell />;
}

export default function App() {
  return (
    <GameProvider>
      <AppInner />
    </GameProvider>
  );
}
