<div align="center">

# 🌟 LIFE-XP Challenge

[![Mod Loaders](https://img.shields.io/badge/Mod%20Loaders-Fabric%20%7C%20NeoForge-orange?logo=minecraft&logoColor=white)](https://modrinth.com)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-green?logo=minecraft&logoColor=white)](https://minecraft.net)
[![Mod Version](https://img.shields.io/badge/Version-1.4.1-blue?logo=semver&logoColor=white)](https://github.com/Heysh1n/life-xp/releases)
[![Wiki](https://img.shields.io/badge/Documentation-GitHub%20Wiki-purple?logo=gitbook&logoColor=white)](https://github.com/Heysh1n/life-xp/wiki)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

*Survive, scale, and conquer. Turn your experience points into your most vital survival resource.*

</div>

---

## 📖 What is LIFE-XP Challenge?

**LIFE-XP Challenge** is a hardcore progression and survival overhaul mod for **Minecraft 1.21.1**, developed on a unified **Architectury** monorepo supporting both **Fabric** and **NeoForge** with 100% feature parity.

In vanilla Minecraft, experience levels are trivial—used merely for enchanting or anvil repairs, and mostly forgotten after reaching level 30. **LIFE-XP binds your entire physical existence to your XP bar:**
* You begin vulnerable and fragile (starting with as little as **0.5 hearts** in hardcore mode).
* Every single experience level dynamically enhances **15 player attributes** (Health, Speed, Attack Damage, Attack Speed, Toughness, Reach, Mining Speed, and more).
* Your experience functions as an active **Energy Shield**, absorbing incoming attacks before they damage your health.
* Combat streaks grant exponential XP multipliers, but deaths impose a **Smart Death Tax** that heavily penalizes reckless mistakes while honoring heroic boss encounters.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| **Cross-Platform Parity** | 100% unified feature set on both **Fabric** and **NeoForge** 1.21.1 via Architectury. |
| **Dynamic Attribute Scaling** | 15 player attributes smoothly scale using piecewise linear mathematical interpolation. |
| **Energy XP Shield** | Damage drains XP before health. Includes a 10% chance for a **100% Perfect Block** costing 10% total XP. |
| **Smart Death Tax** | Adaptive death penalty: clumsy deaths (lava, fall) lose up to 100% XP; boss battles lose only 10%. |
| **Kill Streak Multiplier** | Up to **+100% XP (2x multiplier)** gained from killing consecutive mobs without dying. |
| **On-Screen HUD Overlay** | Real-time top-left interface displaying level cap progress, progress %, and active streak. |
| **Atmospheric Level Fog** | Dense, pitch-black fog blinds beginners at level 0 and gradually clears as experience rises. |
| **Life Bottle Artifact** | Retains all 41 inventory slots on death (immune to lava/void) with 3 durability charges. |
| **Dynamic XP Bottle** | Stores player XP with Shift+RMB and features dynamic liquid tinting based on fill ratio. |
| **Milestone Advancements** | Custom advancements, sound effects, and item rewards for reaching levels 100, 250, and 500. |
| **In-Game YACL Config** | Rich GUI settings screen with sliders, presets, and live reload in Mod Menu / Mods list. |
| **Preset Exporter & Top** | One-click JSON export to clipboard (`/lifexp export`) and server leaderboard (`/lifexp top`). |
| **Tri-Lingual Localization** | Complete translations for English (`en_us`), Russian (`ru_ru`), and Turkish (`tr_tr`). |

---

## 📦 Installation & Requirements

### System Requirements
* **Minecraft:** `1.21.1`
* **Java:** `21+`
* **Mod Loaders:** `Fabric Loader` or `NeoForge`

### Fabric Installation
1. Install [Fabric Loader 1.21.1](https://fabricmc.net/).
2. Place required dependencies into your `.minecraft/mods/` folder:
   * [Architectury API (Fabric)](https://modrinth.com/mod/architectury-api)
   * [Fabric API](https://modrinth.com/mod/fabric-api)
   * [YetAnotherConfigLib (YACL) v3](https://modrinth.com/mod/yacl)
   * *(Optional)* [Mod Menu](https://modrinth.com/mod/modmenu)
3. Drop `life_xp_challenge-1.4.1-fabric.jar` into `mods/`.

### NeoForge Installation
1. Install [NeoForge 1.21.1](https://neoforged.net/) (`21.1.77+`).
2. Place required dependencies into your `.minecraft/mods/` folder:
   * [Architectury API (NeoForge)](https://modrinth.com/mod/architectury-api)
   * [YetAnotherConfigLib (YACL) v3](https://modrinth.com/mod/yacl)
3. Drop `life_xp_challenge-1.4.1-neoforge.jar` into `mods/`.

---

## 🧬 Core Progression & Mechanics

### Mathematical Scaling Model
Attributes are recalculated dynamically across three anchor points: `startValue` (Lvl 0), `midValue` (Lvl `maxLevel / 2`), and `endValue` (Lvl `maxLevel`):

$$\text{Value}(L) = \begin{cases} 
\text{start} + (\text{mid} - \text{start}) \times \frac{L}{\text{maxLevel} / 2}, & 0 \le L \le \frac{\text{maxLevel}}{2} \\ 
\text{mid} + (\text{end} - \text{mid}) \times \frac{L - \text{maxLevel}/2}{\text{maxLevel}/2}, & \frac{\text{maxLevel}}{2} < L \le \text{maxLevel} \\
\text{end}, & L > \text{maxLevel}
\end{cases}$$

### Scaled Attributes
* **Combat:** Max Health (`max_health`), Attack Damage (`attack_damage`), Attack Speed (`attack_speed`), Sweeping Damage Ratio (`sweeping_damage_ratio`).
* **Defense & Agility:** Movement Speed (`movement_speed`), Sneak Speed (`sneaking_speed`), Armor Toughness (`armor_toughness`), Knockback Resistance (`knockback_resistance`), Safe Fall Distance (`safe_fall_distance`).
* **Utility & Survival:** Block Reach (`block_interaction_range`), Entity Reach (`entity_interaction_range`), Mining Speed (`block_break_speed`), Submerged Mining (`submerged_mining_speed`), Underwater Oxygen (`oxygen_bonus`), Fire Burning Duration (`burning_time`).

---

## ⚔️ Combat & Survival

### 🛡️ Energy XP Shield
* Damage is absorbed *after* armor calculations directly from your experience pool.
* **Partial Absorption:** Defrays **50%** of damage (costing 10 XP points per 1 HP absorbed).
* **Perfect Block (10% Chance):** Deflects **100% of damage**, burning **10% of total XP** with an amethyst chime chime effect.
* Fully configurable via `/lifexp set xpShield...` or in-game GUI.

### 🔥 Kill Streak Multiplier
* Slain mobs increase your streak by **+1** (up to 100 kills).
* Each streak point grants **+1% extra experience** on all collected XP (up to **2x / +100%**).
* Notification toasts display every 10 kills; player death resets streak to 0.

### 💀 Smart Death Tax
* **Clumsy Deaths (Lava, Falling, Drowning, Starvation):** Tax penalty is multiplied by **5x** (up to 100% loss).
* **Heroic Boss Battles (Wither, Warden, Ender Dragon):** Tax penalty is divided by **10x** (lose as little as 10% XP).
* **Standard Deaths (Mobs, PvP):** Standard configured tax (`deathXpTax`).

---

## 🖥️ Client HUD Overlay

A lightweight, non-intrusive HUD renders in the top-left corner (suppressed on `F1` or `F3`):

```text
┌──────────────────────────────────────────┐
│ Lvl 48 / 1000                            │
│ [████████████░░░░░░░░░░░░░░░░░░░░░░░░░░] │  <-- Level cap progress (Turns Gold at cap)
│ XP: 82%                                  │  <-- Next level progress
│ 🔥 Streak: 35 (+35%)                     │  <-- Active kill streak
└──────────────────────────────────────────┘
```

---

## 🧪 Utility Items & Recipes

### 1. Life Bottle (`life_bottle`)
* Saves all **41 inventory slots** upon death into an invulnerable ground entity.
* **Void Protection:** Automatically rescues inventory even if you fall into the End Void.
* Hold **Shift + Right-Click** to unpack equipment directly into their original slots.

```text
[ Gold Ingot ] [ Amethyst Shard ] [ Gold Ingot ]
[   Glass    ] [Totem of Undying] [   Glass    ]
[  Obsidian  ] [  Glass Bottle  ] [  Obsidian  ]
```

### 2. Dynamic XP Bottle (`dynamic_xp_bottle`)
* Hold **Shift + Right-Click** to siphon player XP into the bottle (10% storage tax).
* Press **Right-Click** to instantly reclaim stored XP.
* Fluid tint dynamically shifts from bright lime green to deep emerald/teal as it fills.

```text
[ Amethyst Shard ] [  Lapis Lazuli  ] [ Amethyst Shard ]
[  Lapis Lazuli  ] [Bottle o' Ench. ] [  Lapis Lazuli  ]
[  Redstone Dust ] [  Glass Bottle  ] [  Redstone Dust ]
```

---

## 🎯 Commands Reference

All commands require OP Level 2 or enabled cheats:

| Command | Description |
|---|---|
| `/lifexp get <param>` | Display current configuration parameter value. |
| `/lifexp set <param> <val>` | Change setting and trigger live recalculation for all players. |
| `/lifexp set <attr> <start\|mid\|end> <val>` | Fine-tune interpolation points for any attribute. |
| `/lifexp preset <core\|vanilla_plus\|baby_mode\|real_hardcore>` | Instant difficulty preset application. |
| `/lifexp export` | Generates a one-click copyable JSON preset directly in chat. |
| `/lifexp top` | Displays the online server leaderboard sorted by experience level. |
| `/lifexp reload` | Reloads `config/life_xp_challenge.json` from disk without restarting. |
| `/lifexp status` | Prints a full diagnostic summary of active settings. |

---

## 🎚️ Difficulty Presets

| Setting | ☠ `core` | 💚 `vanilla_plus` | 🍼 `baby_mode` | 🔥 `real_hardcore` |
|---|:---:|:---:|:---:|:---:|
| **Starting Health** | **1.0 HP (0.5 heart)** | **6.0 HP (3 hearts)** | **20.0 HP (10 hearts)** | **1.0 HP (0.5 heart)** |
| **Max Health** | 40.0 HP (20 hearts) | 24.0 HP (12 hearts) | 30.0 HP (15 hearts) | 40.0 HP (20 hearts) |
| **maxLevel Cap** | 1,000 | 500 | 200 | 10,000 |
| **Atmospheric Fog** | Enabled (20-256m) | Disabled | Disabled | Enabled (15-180m) |
| **Base Death Tax** | 100% | 50% | 10% | 100% |
| **Starting Speed** | 0.05 (Sluggish) | 0.08 | 0.1 (Vanilla) | 0.04 (Crawl) |

---

## 🏗️ Architecture

```text
LIFE-XP-Challenge-Architectury/
├── common/                  # Unified multi-loader core logic
│   ├── client/              # In-game HUD overlay & YACL config screen
│   ├── command/             # Brigadier /lifexp command tree
│   ├── config/              # JSON configuration engine & presets
│   ├── item/                # Life Bottle & Dynamic XP Bottle items
│   ├── mixin/               # Damage interception & death tax mixins
│   ├── network/             # Architectury S2C packet networking
│   ├── registry/            # Deferred item and creative tab registries
│   └── service/             # Attribute scaling, XP shield, streaks, milestones
├── fabric/                  # Fabric Loader entrypoints & ModMenu integration
└── neoforge/                # NeoForge entrypoints & IConfigScreenFactory
```

---

## 📚 Documentation

For exhaustive technical documentation, developer internals, mixin injection pipelines, and math references, visit our official **[GitHub Wiki](https://github.com/Heysh1n/life-xp/wiki)**!

---

## 📜 License

[MIT](LICENSE) — © 2026 [Heysh1n](https://github.com/Heysh1n)

<p align="center">
  Made with ❤️ by Heysh1n
</p>
