import { describe, it, expect } from "vitest";
import { buildEnemyUnits, simulateBattle, unitFromInstance } from "../combat";
import type { HeroInstance } from "../../types";
import { HEROES } from "../heroes";

function makeHero(
  templateId: string,
  overrides: Partial<HeroInstance> = {}
): HeroInstance {
  return {
    instanceId: `t-${templateId}`,
    templateId,
    level: 20,
    stars: 5,
    xp: 0,
    echoStacks: 0,
    gearTier: 2,
    ...overrides,
  };
}

describe("combat", () => {
  it("produces a winner within 40 turns", () => {
    const allies = HEROES.slice(0, 5).map((h, i) =>
      unitFromInstance(makeHero(h.id), "ally", i)
    );
    const enemies = buildEnemyUnits(1, 1);
    const r = simulateBattle(allies, enemies);
    expect(["ally", "enemy"]).toContain(r.winner);
    expect(r.turns).toBeLessThanOrEqual(40);
  });

  it("scaling enemies win against level-1 heroes eventually", () => {
    const weakHeroes = HEROES.slice(0, 5).map((h, i) =>
      unitFromInstance(makeHero(h.id, { level: 1, stars: 3, gearTier: 0 }), "ally", i)
    );
    const toughEnemies = buildEnemyUnits(8, 8);
    const r = simulateBattle(weakHeroes, toughEnemies);
    expect(r.winner).toBe("enemy");
  });
});
