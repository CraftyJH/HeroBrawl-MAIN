import type { ClassId, HeroClass } from "../types";

export const CLASSES: Record<ClassId, HeroClass> = {
  guardian: {
    id: "guardian",
    name: "Guardian",
    role: "Tank",
    description: "Frontline wall. Taunts enemies and reduces incoming damage.",
    baseStats: { attack: 120, health: 2400, armor: 180, speed: 85, willpower: 60 },
  },
  berserker: {
    id: "berserker",
    name: "Berserker",
    role: "Bruiser",
    description: "Frontline DPS. Gains attack as HP drops.",
    baseStats: { attack: 210, health: 1800, armor: 110, speed: 95, willpower: 55 },
  },
  mage: {
    id: "mage",
    name: "Mage",
    role: "AoE Caster",
    description: "Backline caster with arena-wide spells and crowd control.",
    baseStats: { attack: 260, health: 1300, armor: 70, speed: 100, willpower: 40 },
  },
  assassin: {
    id: "assassin",
    name: "Assassin",
    role: "Burst",
    description: "Leaps to enemy backline to delete supports and squishies.",
    baseStats: { attack: 280, health: 1250, armor: 60, speed: 130, willpower: 50 },
  },
  ranger: {
    id: "ranger",
    name: "Ranger",
    role: "Sustain DPS",
    description: "Backline ranged dps with armor-piercing shots.",
    baseStats: { attack: 230, health: 1400, armor: 80, speed: 110, willpower: 50 },
  },
  cleric: {
    id: "cleric",
    name: "Cleric",
    role: "Support",
    description: "Heals, shields, and cleanses allies with sacred light.",
    baseStats: { attack: 140, health: 1500, armor: 90, speed: 105, willpower: 75 },
  },
};

export const CLASS_IDS: ClassId[] = [
  "guardian",
  "berserker",
  "mage",
  "assassin",
  "ranger",
  "cleric",
];
