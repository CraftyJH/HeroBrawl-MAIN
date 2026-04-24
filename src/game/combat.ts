import type {
  BattleLog,
  BattleResult,
  BattleUnit,
  FactionId,
  GameState,
  HeroInstance,
  Skill,
  StatusEffect,
} from "../types";
import { CLASSES } from "./classes";
import { factionAdvantage, lineupAura } from "./factions";
import { CLASS_ACTIVE } from "./skills";
import { computeStats, templateFor } from "./stats";

// Build a battle unit from a hero instance.
export function unitFromInstance(
  inst: HeroInstance,
  side: "ally" | "enemy",
  slot: number,
  auraBonus?: { attack: number; health: number; speed: number }
): BattleUnit {
  const t = templateFor(inst);
  const stats = computeStats(inst);
  const mul = auraBonus ?? { attack: 0, health: 0, speed: 0 };
  const maxHp = Math.round(stats.health * (1 + mul.health));
  return {
    uid: `${side}-${slot}-${inst.instanceId}`,
    side,
    slot,
    name: t.name,
    emoji: t.emoji,
    faction: t.faction,
    class: t.class,
    stars: inst.stars,
    stats: {
      ...stats,
      attack: Math.round(stats.attack * (1 + mul.attack)),
      speed: Math.round(stats.speed * (1 + mul.speed)),
    },
    hp: maxHp,
    maxHp,
    energy: t.class === "mage" ? 30 : 0,
    skills: t.skills,
    statuses: [],
    portraitGradient: t.portraitGradient,
    signatureColor: t.signatureColor,
  };
}

export function buildAllyUnits(state: GameState): BattleUnit[] {
  const instIds = state.lineup.slots.filter(Boolean) as string[];
  if (!instIds.length) return [];
  const insts = instIds
    .map((id) => state.heroes.find((h) => h.instanceId === id))
    .filter(Boolean) as HeroInstance[];
  const factions: FactionId[] = insts.map((i) => templateFor(i).faction);
  const aura = lineupAura(factions);
  return state.lineup.slots.map((id, slot) => {
    if (!id) return null;
    const inst = state.heroes.find((h) => h.instanceId === id);
    if (!inst) return null;
    return unitFromInstance(inst, "ally", slot, aura.bonus);
  }).filter(Boolean) as BattleUnit[];
}

// Build scaling enemy units.
export function buildEnemyUnits(chapter: number, stage: number, seed = Math.random()): BattleUnit[] {
  const progress = (chapter - 1) * 10 + (stage - 1);
  const power = 1 + progress * 0.18;

  const archetypes: (keyof typeof CLASSES)[] = [
    "guardian",
    "berserker",
    "mage",
    "assassin",
    "ranger",
    "cleric",
  ];
  const factions: FactionId[] = [
    "vanguard",
    "horde",
    "wildwood",
    "arcane",
    "radiance",
    "abyss",
  ];

  const units: BattleUnit[] = [];
  const slotPicks: (keyof typeof CLASSES)[] = [
    "guardian",
    "guardian",
    "berserker",
    "mage",
    "ranger",
  ];

  for (let slot = 0; slot < 5; slot++) {
    const seeded = (seed * 1000 + slot * 17 + progress * 31) % 1;
    const cls = slotPicks[slot] ?? archetypes[Math.floor(seeded * archetypes.length)];
    const faction = factions[Math.floor(((seeded + slot * 0.13 + chapter * 0.07) % 1) * factions.length)];
    const base = CLASSES[cls].baseStats;

    const stars = Math.min(5, 2 + Math.floor(progress / 8)) as BattleUnit["stars"];
    const statMul = power * (0.85 + stars * 0.1);
    const maxHp = Math.round(base.health * statMul);
    const name = ENEMY_NAMES[slot % ENEMY_NAMES.length] + " " + (progress + 1);

    units.push({
      uid: `enemy-${slot}-${progress}`,
      side: "enemy",
      slot,
      name,
      emoji: ENEMY_EMOJI[cls],
      faction,
      class: cls,
      stars,
      stats: {
        attack: Math.round(base.attack * statMul),
        health: maxHp,
        armor: Math.round(base.armor * statMul * 0.8),
        speed: base.speed,
        willpower: base.willpower,
      },
      hp: maxHp,
      maxHp,
      energy: 0,
      skills: [CLASS_ACTIVE[cls]],
      statuses: [],
      portraitGradient: ["#2a2a36", "#4a4a5a"],
      signatureColor: "#8a8a9a",
    });
  }

  return units;
}

const ENEMY_NAMES = [
  "Rogue Sellsword",
  "Cursed Knight",
  "Wildfire Witch",
  "Night Stalker",
  "Ironclad",
  "Lost Pilgrim",
];
const ENEMY_EMOJI: Record<keyof typeof CLASSES, string> = {
  guardian: "🗿",
  berserker: "💢",
  mage: "🌀",
  assassin: "🕸️",
  ranger: "🎯",
  cleric: "🕯️",
};

// --- Combat simulator ---------------------------------------------------

export interface BattleOptions {
  manualBurst?: boolean; // if true, ally skills fire when energy full next chance
  seed?: number;
}

export function simulateBattle(
  allies: BattleUnit[],
  enemies: BattleUnit[],
  opts: BattleOptions = {}
): BattleResult {
  void opts;
  // Deep-copy units so callers' references remain clean.
  const A = allies.map((u) => cloneUnit(u));
  const E = enemies.map((u) => cloneUnit(u));
  const log: BattleLog[] = [];

  const pushLog = (entry: Omit<BattleLog, "at">) =>
    log.push({ ...entry, at: performance.now() });

  pushLog({ kind: "info", text: "Battle begins!" });

  let turn = 0;
  const maxTurns = 40;

  while (turn < maxTurns) {
    turn++;
    // Build turn order by speed, all alive units.
    const all = [...A, ...E].filter((u) => u.hp > 0);
    all.sort((a, b) => b.stats.speed - a.stats.speed);

    for (const actor of all) {
      if (actor.hp <= 0) continue;
      if (tickStatuses(actor, pushLog)) continue; // stunned

      const opponents = actor.side === "ally" ? E : A;
      if (opponents.every((u) => u.hp <= 0)) break;

      // Charge energy
      actor.energy = Math.min(100, actor.energy + 25 + (actor.class === "mage" ? 10 : 0));

      // Fire active skill when energy >= 100
      if (actor.energy >= 100 && actor.skills.some((s) => s.kind === "active")) {
        const skill = actor.skills.find((s) => s.kind === "active")!;
        actor.energy = 0;
        executeSkill(actor, skill, A, E, pushLog);
      } else {
        basicAttack(actor, opponents, pushLog);
      }

      if (A.every((u) => u.hp <= 0) || E.every((u) => u.hp <= 0)) break;
    }

    // End-of-turn statuses (burn/poison/regen)
    for (const u of [...A, ...E]) if (u.hp > 0) applyDots(u, pushLog);

    if (A.every((u) => u.hp <= 0) || E.every((u) => u.hp <= 0)) break;
  }

  const winner: "ally" | "enemy" = A.some((u) => u.hp > 0) ? "ally" : "enemy";
  pushLog({
    kind: winner === "ally" ? "victory" : "defeat",
    text: winner === "ally" ? "Victory!" : "Defeat.",
  });

  return { winner, turns: turn, log, survivors: [...A, ...E].filter((u) => u.hp > 0) };
}

function cloneUnit(u: BattleUnit): BattleUnit {
  return {
    ...u,
    stats: { ...u.stats },
    statuses: u.statuses.map((s) => ({ ...s })),
    skills: u.skills.map((s) => ({ ...s })),
  };
}

function tickStatuses(u: BattleUnit, log: (e: Omit<BattleLog, "at">) => void): boolean {
  let stunned = false;
  const next: StatusEffect[] = [];
  for (const s of u.statuses) {
    if (s.kind === "stun" && s.duration > 0) {
      stunned = true;
      log({ kind: "status", text: `${u.name} is stunned.`, actor: u.uid });
    }
    const d = s.duration - 1;
    if (d > 0) next.push({ ...s, duration: d });
  }
  u.statuses = next;
  return stunned;
}

function applyDots(u: BattleUnit, log: (e: Omit<BattleLog, "at">) => void) {
  for (const s of u.statuses) {
    if (s.kind === "burn" || s.kind === "poison") {
      const dmg = Math.round(u.maxHp * s.magnitude);
      u.hp = Math.max(0, u.hp - dmg);
      log({
        kind: "status",
        text: `${u.name} suffers ${dmg} ${s.kind} damage.`,
        actor: u.uid,
        amount: dmg,
      });
      if (u.hp <= 0) {
        log({ kind: "death", text: `${u.name} falls.`, actor: u.uid });
      }
    } else if (s.kind === "regen") {
      const heal = Math.round(u.maxHp * s.magnitude);
      u.hp = Math.min(u.maxHp, u.hp + heal);
      log({
        kind: "heal",
        text: `${u.name} regenerates ${heal} HP.`,
        actor: u.uid,
        amount: heal,
      });
    }
  }
}

function pickTarget(actor: BattleUnit, opponents: BattleUnit[]): BattleUnit | undefined {
  const alive = opponents.filter((u) => u.hp > 0);
  if (!alive.length) return undefined;

  // Assassins target lowest HP, rangers/mages prefer backline, others target frontline
  if (actor.class === "assassin") {
    return [...alive].sort((a, b) => a.hp - b.hp)[0];
  }
  if (actor.class === "ranger" || actor.class === "mage") {
    const back = alive.filter((u) => u.slot >= 3);
    if (back.length) return back[0];
  }
  const front = alive.filter((u) => u.slot <= 2);
  if (front.length) return front[0];
  return alive[0];
}

function basicAttack(
  actor: BattleUnit,
  opponents: BattleUnit[],
  log: (e: Omit<BattleLog, "at">) => void
) {
  const target = pickTarget(actor, opponents);
  if (!target) return;
  const { damage } = computeDamage(actor, target, 1.0);
  target.hp = Math.max(0, target.hp - damage);
  log({
    kind: "attack",
    text: `${actor.name} attacks ${target.name} for ${damage}.`,
    actor: actor.uid,
    target: target.uid,
    amount: damage,
  });
  if (target.hp <= 0) log({ kind: "death", text: `${target.name} falls.`, actor: target.uid });
}

function executeSkill(
  actor: BattleUnit,
  skill: Skill,
  allies: BattleUnit[],
  enemies: BattleUnit[],
  log: (e: Omit<BattleLog, "at">) => void
) {
  const opponents = actor.side === "ally" ? enemies : allies;
  const friends = actor.side === "ally" ? allies : enemies;

  log({
    kind: "skill",
    text: `${actor.name} unleashes ${skill.name}!`,
    actor: actor.uid,
  });

  const power = skill.power ?? 1;

  if (skill.target === "all-enemies" || skill.aoe) {
    for (const t of opponents.filter((u) => u.hp > 0)) {
      const { damage } = computeDamage(actor, t, power);
      t.hp = Math.max(0, t.hp - damage);
      log({
        kind: "attack",
        text: `→ hits ${t.name} for ${damage}.`,
        actor: actor.uid,
        target: t.uid,
        amount: damage,
      });
      if (skill.status) t.statuses.push({ ...skill.status });
      if (t.hp <= 0) log({ kind: "death", text: `${t.name} falls.`, actor: t.uid });
    }
  } else if (skill.target === "all-allies") {
    const heal = Math.round(actor.stats.attack * power);
    for (const f of friends.filter((u) => u.hp > 0)) {
      f.hp = Math.min(f.maxHp, f.hp + heal);
      log({
        kind: "heal",
        text: `→ restores ${heal} HP to ${f.name}.`,
        actor: actor.uid,
        target: f.uid,
        amount: heal,
      });
      if (skill.status) f.statuses.push({ ...skill.status });
    }
  } else {
    // single enemy
    const target = pickTarget(actor, opponents);
    if (!target) return;
    const { damage } = computeDamage(actor, target, power);
    target.hp = Math.max(0, target.hp - damage);
    log({
      kind: "attack",
      text: `→ strikes ${target.name} for ${damage}.`,
      actor: actor.uid,
      target: target.uid,
      amount: damage,
    });
    if (skill.status) target.statuses.push({ ...skill.status });
    if (target.hp <= 0) log({ kind: "death", text: `${target.name} falls.`, actor: target.uid });
    // Self-shield for guardians (Bulwark Strike secondary effect)
    if (skill.status?.kind === "shield") {
      actor.statuses.push({ ...skill.status });
    }
  }
}

function computeDamage(
  attacker: BattleUnit,
  defender: BattleUnit,
  powerMul: number
): { damage: number; crit: boolean } {
  const adv = factionAdvantage(attacker.faction, defender.faction);
  let dmg = attacker.stats.attack * powerMul;

  // Class passives
  if (attacker.class === "berserker") {
    const missing = 1 - attacker.hp / Math.max(1, attacker.maxHp);
    dmg *= 1 + missing; // Bloodrage
  }
  if (attacker.class === "ranger") {
    // ignore 30% armor
    dmg *= 1.15;
  }

  // Armor mitigation
  const armor = defender.stats.armor * (attacker.class === "ranger" ? 0.7 : 1);
  const mitigation = armor / (armor + 500);
  dmg *= 1 - mitigation;

  // Faction advantage
  dmg *= 1 + adv.damage;

  // Crit
  const critChance = attacker.class === "assassin" && defender.hp < defender.maxHp * 0.5
    ? 0.5
    : 0.2;
  const crit = Math.random() < critChance;
  if (crit) dmg *= 1.8;

  // Guardian passive: 15% less from melee
  if (defender.class === "guardian" && (attacker.class === "berserker" || attacker.class === "assassin" || attacker.class === "guardian")) {
    dmg *= 0.85;
  }

  // Defender shield
  const shield = defender.statuses.find((s) => s.kind === "shield");
  if (shield) {
    dmg *= 1 - Math.min(0.4, shield.magnitude);
  }

  // Cleric Radiant Aegis
  const sameSide = attacker.side !== defender.side;
  if (sameSide) {
    const cleric = (defender.side === "ally")
      ? allyHasCleric(defender, dmg)
      : false;
    if (cleric && defender.hp / defender.maxHp < 0.3) dmg *= 0.75;
  }

  return { damage: Math.max(1, Math.round(dmg)), crit };
}

function allyHasCleric(u: BattleUnit, dmg: number) {
  void u;
  void dmg;
  // Simplified: assume absence; full implementation would scan friends.
  return false;
}

// --- Rewards -------------------------------------------------------------

export interface BattleRewards {
  gold: number;
  spirit: number;
  xp: number;
  brawlPoints: number;
  firstClear?: { gems: number };
}

export function rewardsForCampaign(chapter: number, stage: number, firstClear: boolean): BattleRewards {
  const progress = (chapter - 1) * 10 + (stage - 1);
  const baseGold = 800 + progress * 120;
  const baseSpirit = 30 + progress * 6;
  const xp = 40 + progress * 8;
  return {
    gold: baseGold,
    spirit: baseSpirit,
    xp,
    brawlPoints: 20 + progress * 3,
    firstClear: firstClear ? { gems: 20 } : undefined,
  };
}
