import type { Faction, FactionId } from "../types";

// Rock-paper-scissors cycle + mirror pair, similar in spirit to TapTap Heroes
// but rebalanced for HeroBrawl with transparent advantage values.
export const FACTIONS: Record<FactionId, Faction> = {
  vanguard: {
    id: "vanguard",
    name: "Vanguard",
    color: "#4aa3ff",
    accent: "#82c4ff",
    lore: "Disciplined knights of the crystal citadel.",
    strongVs: ["horde"],
  },
  horde: {
    id: "horde",
    name: "Horde",
    color: "#ff8a3d",
    accent: "#ffb370",
    lore: "Tribal warriors from the scorched badlands.",
    strongVs: ["wildwood"],
  },
  wildwood: {
    id: "wildwood",
    name: "Wildwood",
    color: "#8ad65f",
    accent: "#b6ea8f",
    lore: "Druids and beastkin of the old forests.",
    strongVs: ["arcane"],
  },
  arcane: {
    id: "arcane",
    name: "Arcane",
    color: "#b47dff",
    accent: "#d3b1ff",
    lore: "Rune-etched sorcerers and starborn scholars.",
    strongVs: ["vanguard"],
  },
  radiance: {
    id: "radiance",
    name: "Radiance",
    color: "#ffd966",
    accent: "#ffe89e",
    lore: "Seraphic champions of the dawn.",
    strongVs: ["abyss"],
  },
  abyss: {
    id: "abyss",
    name: "Abyss",
    color: "#e44a7a",
    accent: "#ff7aa3",
    lore: "Infernal lords and shadow-sworn sovereigns.",
    strongVs: ["radiance"],
  },
};

export const FACTION_IDS: FactionId[] = [
  "vanguard",
  "horde",
  "wildwood",
  "arcane",
  "radiance",
  "abyss",
];

// Aura bonus: 5 of the same faction in lineup grants a strong bonus.
export function lineupAura(factions: FactionId[]): {
  faction: FactionId | "mixed" | "dawnfall";
  description: string;
  bonus: { attack: number; health: number; speed: number };
} {
  const counts = new Map<FactionId, number>();
  for (const f of factions) counts.set(f, (counts.get(f) ?? 0) + 1);

  const maxEntry = [...counts.entries()].sort((a, b) => b[1] - a[1])[0];

  // Dawnfall pact: 2 Radiance + 2 Abyss + any 1 = big balanced bonus
  const rad = counts.get("radiance") ?? 0;
  const aby = counts.get("abyss") ?? 0;
  if (rad >= 2 && aby >= 2 && factions.length === 5) {
    return {
      faction: "dawnfall",
      description: "Dawnfall Pact · 2 Radiance + 2 Abyss",
      bonus: { attack: 0.18, health: 0.2, speed: 0.08 },
    };
  }

  if (maxEntry && maxEntry[1] === 5) {
    const bonuses: Record<FactionId, { attack: number; health: number; speed: number }> = {
      vanguard: { attack: 0.1, health: 0.3, speed: 0 },
      horde: { attack: 0.22, health: 0.1, speed: 0.05 },
      wildwood: { attack: 0.1, health: 0.2, speed: 0.12 },
      arcane: { attack: 0.25, health: 0.05, speed: 0.05 },
      radiance: { attack: 0.12, health: 0.2, speed: 0.08 },
      abyss: { attack: 0.2, health: 0.15, speed: 0.08 },
    };
    return {
      faction: maxEntry[0],
      description: `Mono-${FACTIONS[maxEntry[0]].name} Aura`,
      bonus: bonuses[maxEntry[0]],
    };
  }

  if (maxEntry && maxEntry[1] === 4) {
    return {
      faction: maxEntry[0],
      description: `${FACTIONS[maxEntry[0]].name} Bond (4)`,
      bonus: { attack: 0.08, health: 0.1, speed: 0.03 },
    };
  }

  return {
    faction: "mixed",
    description: "Balanced lineup",
    bonus: { attack: 0, health: 0, speed: 0 },
  };
}

// +20% dmg, +10% speed when attacker's faction is strong vs defender's
export function factionAdvantage(
  attacker: FactionId,
  defender: FactionId
): { damage: number; speed: number } {
  if (FACTIONS[attacker].strongVs.includes(defender)) {
    return { damage: 0.2, speed: 0.1 };
  }
  if (FACTIONS[defender].strongVs.includes(attacker)) {
    return { damage: -0.15, speed: -0.05 };
  }
  return { damage: 0, speed: 0 };
}
