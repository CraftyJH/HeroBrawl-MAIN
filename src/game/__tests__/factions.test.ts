import { describe, it, expect } from "vitest";
import { factionAdvantage, lineupAura } from "../factions";

describe("factions", () => {
  it("Vanguard beats Horde", () => {
    const adv = factionAdvantage("vanguard", "horde");
    expect(adv.damage).toBeGreaterThan(0);
  });

  it("Horde suffers vs Vanguard", () => {
    const adv = factionAdvantage("horde", "vanguard");
    expect(adv.damage).toBeLessThan(0);
  });

  it("5-of-a-kind mono-faction gives an aura", () => {
    const a = lineupAura(["abyss", "abyss", "abyss", "abyss", "abyss"]);
    expect(a.faction).toBe("abyss");
    expect(a.bonus.attack).toBeGreaterThan(0);
  });

  it("Dawnfall Pact unlocks with 2 radiance + 2 abyss + 1 other", () => {
    const a = lineupAura(["radiance", "radiance", "abyss", "abyss", "vanguard"]);
    expect(a.faction).toBe("dawnfall");
  });
});
