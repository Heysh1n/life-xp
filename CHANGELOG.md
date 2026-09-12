# 📜 Changelog — LIFE-XP Challenge

All notable changes to this project will be documented in this file.

---

## [1.5.2] - Visuals & Shaders Overhaul (Fog Replacement)

### 🎨 Visuals & Immersion (Major Refactor)
- **Removed Legacy Fog System:**
  - Completely removed `FogRenderer` mixins and dynamic fog density alterations that caused chunk culling bugs, rendering distance truncation, and shader incompatibilities.
  - Removed obsolete configuration properties (`enableCustomFog`, `disableFog`, `fogStartDistance`, `fogMidDistance`, `fogEndDistance`) from `LifeXpConfig`, YACL GUI Screen, and commands (`/lifexp get fog`, `/lifexp set fog`).
- **Dynamic Client Vignette (HUD Overlay):**
  - Added new client handler [`LifeXpVisualsClient`](file:///home/heysh1n/IdeaProjects/LIFE-XP-Challenge-Architectury/common/src/main/java/com/hs1n/lifeXp_challenge/client/LifeXpVisualsClient.java).
  - Listens to Architectury's `ClientGuiEvent.RENDER_HUD` to blit `textures/gui/vignette.png` across the screen.
  - Smooth dynamic alpha calculation based on player experience:
    $$\text{alpha} = \max\left(0.0, 1.0 - \frac{\text{currentXp}}{\text{maxLevel} \times 0.5}\right)$$
  - Full transparency ($\alpha = 0$) at $\ge 50\%$ of `maxLevel`, darkening screen edges progressively as experience drops.
- **Ambient Ash Particles (`ParticleTypes.ASH`):**
  - Implemented on `ClientTickEvent.CLIENT_POST` in `LifeXpVisualsClient`.
  - When local player's experience falls below $10\%$ of `maxLevel`, spawns 1–2 ash particles in a 1.5-block radius with a 30% tick chance to convey imminent peril.

### ⚙️ Configuration & Commands
- Updated [YetAnotherConfigLib (YACL) Screen](file:///home/heysh1n/IdeaProjects/LIFE-XP-Challenge-Architectury/common/src/main/java/com/hs1n/lifeXp_challenge/client/config/LifeXpConfigScreen.java) to streamline the Visuals group, eliminating obsolete fog toggles and sliders.
- Cleaned up preset definitions in [LifeXpPresets](file:///home/heysh1n/IdeaProjects/LIFE-XP-Challenge-Architectury/common/src/main/java/com/hs1n/lifeXp_challenge/config/LifeXpPresets.java) across all difficulty profiles (`core`, `vanilla_plus`, `baby_mode`, `real_hardcore`, `purist`).
- Updated status reporting in `/lifexp status` to display active visual effects instead of fog parameters.

### 🌐 Localization & Documentation
- Updated `en_us.json`, `ru_ru.json`, and `tr_tr.json` language files with clean descriptions of the new visual effects.
- Added asset staging directories:
  - Repository / README: `.github/assets/crafts/` and `.github/assets/gifs/`
  - GitHub Wiki: `wiki/.assets/crafts/` and `wiki/.assets/gifs/`
- Updated `README.md` with visual effect descriptions, updated preset comparisons, and craft image slots.

---

## [1.5.0] - The Performance Overhaul
- Dynamic $O(1)$ attribute progression recalculation.
- Clumps-style XP merging to prevent server lag.
- Potion of XP-Shield alchemy and absorption tiers.
- Graphical HUD bubbles and color-shifting XP text.
- Life Bottle void rescue & inventory restoration.
- JEI & EMI recipe and manual integration.
