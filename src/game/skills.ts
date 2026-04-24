import type { ClassId, Skill } from "../types";

// Signature active skill per class (generic, used as default)
export const CLASS_ACTIVE: Record<ClassId, Skill> = {
  guardian: {
    id: "gd_active",
    name: "Bulwark Strike",
    description:
      "Slam the nearest enemy for 180% ATK and gain a shield equal to 25% max HP.",
    kind: "active",
    energyCost: 100,
    cooldown: 0,
    power: 1.8,
    target: "enemy",
    status: { kind: "shield", duration: 2, magnitude: 0.25 },
  },
  berserker: {
    id: "bz_active",
    name: "Crimson Rend",
    description: "Cleave the frontline for 220% ATK to up to 3 enemies.",
    kind: "active",
    energyCost: 100,
    power: 2.2,
    aoe: true,
    target: "all-enemies",
  },
  mage: {
    id: "mg_active",
    name: "Arcane Cascade",
    description: "Blast all enemies for 150% ATK and apply Burn (2 turns).",
    kind: "active",
    energyCost: 100,
    power: 1.5,
    aoe: true,
    target: "all-enemies",
    status: { kind: "burn", duration: 2, magnitude: 0.15 },
  },
  assassin: {
    id: "as_active",
    name: "Shadow Pounce",
    description: "Teleport and strike the enemy with the lowest HP for 320% ATK.",
    kind: "active",
    energyCost: 100,
    power: 3.2,
    target: "enemy",
  },
  ranger: {
    id: "rg_active",
    name: "Piercing Volley",
    description: "Fire 3 arrows that each hit for 120% ATK, ignoring 40% armor.",
    kind: "active",
    energyCost: 100,
    power: 1.2 * 3 * 0.72, // approximated for sim
    target: "enemy",
  },
  cleric: {
    id: "cl_active",
    name: "Dawnbloom",
    description: "Heal the team for 180% ATK and grant Regen for 2 turns.",
    kind: "active",
    energyCost: 100,
    power: 1.8,
    aoe: true,
    target: "all-allies",
    status: { kind: "regen", duration: 2, magnitude: 0.1 },
  },
};

// Passive per class (flavor, read at combat start)
export const CLASS_PASSIVE: Record<ClassId, Skill> = {
  guardian: {
    id: "gd_pass",
    name: "Iron Resolve",
    description: "Takes 15% less damage from melee attacks.",
    kind: "passive",
  },
  berserker: {
    id: "bz_pass",
    name: "Bloodrage",
    description: "+1% ATK for every 1% missing HP.",
    kind: "passive",
  },
  mage: {
    id: "mg_pass",
    name: "Manaflow",
    description: "Gains +10 energy at start of each turn.",
    kind: "passive",
  },
  assassin: {
    id: "as_pass",
    name: "Killsight",
    description: "Crit chance doubled vs targets under 50% HP.",
    kind: "passive",
  },
  ranger: {
    id: "rg_pass",
    name: "Hawk's Eye",
    description: "Ignores 30% armor on basic attacks.",
    kind: "passive",
  },
  cleric: {
    id: "cl_pass",
    name: "Radiant Aegis",
    description: "Allies under 30% HP take 25% reduced damage.",
    kind: "passive",
  },
};
