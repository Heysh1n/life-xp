# Changelog

All notable changes to the **LIFE-XP Challenge** mod will be documented in this file.

---

## [1.5.4] - Content Update: Data-Driven Enchantments & Loot Overhaul

### 🔮 Data-Driven Enchantments (Minecraft 1.21.1 JSON Format)
* **XP Harvester (`life_xp_challenge:xp_harvester`):**
  * Maximum level: **III**.
  * Applicable to **Pickaxes** and **Hoes** (`#minecraft:pickaxes`, `#minecraft:hoes`).
  * Added to the standard enchanting table tag (`#minecraft:enchantment/in_enchanting_table`).
  * **Mechanics:**
    * **Mature Crops:** Harvesting fully-grown crops (`CropBlock` with `isMaxAge`) drops **1–2 Experience Orbs**.
    * **Vanilla Ores:** Mining Iron, Copper, Gold, Lapis, Redstone, or Diamond ores yields scaled bonus experience based on enchantment level (`level * 2` to `level * 5` XP).
    * **Ancient Debris:** Yields a high-stakes reward equal to **1% of the player's total experience points** (capped at a maximum of 500 XP per block).
* **Anomaly Angler (`life_xp_challenge:anomaly_angler`):**
  * Maximum level: **I**.
  * Applicable to **Fishing Rods** (`#minecraft:enchantable/fishing`).
  * Added to the standard enchanting table tag (`#minecraft:enchantment/in_enchanting_table`).
  * **Mechanics:** Unlocks anomalous treasure drops when fishing.

### 🎣 Fishing & Loot Overhaul
* **Treasure Loot Injection:** Hooked into `minecraft:gameplay/fishing/treasure` via Architectury's `LootEvent.MODIFY_LOOT_TABLE`.
* **Custom Loot Condition:** Implemented `HasAnomalyAnglerCondition` (`life_xp_challenge:has_anomaly_angler`) checking the tool in hand for the `anomaly_angler` enchantment.
* **Custom Loot Function:** Implemented `AnomalyFishingLootFunction` (`life_xp_challenge:anomaly_fishing_loot`):
  * **Shield Core (`shield_core`):** Added to the anomalous treasure pool.
  * **Damaged Life Bottle (`life_bottle`):** Can be caught with pre-inflicted damage (only 1–2 durability points remaining) using vanilla `minecraft:damage` Data Component.
  * **Charged Dynamic XP Bottle (`dynamic_xp_bottle`):** Can be caught pre-filled with 5 to 40 stored experience points using `life_xp_challenge:stored_xp` Data Component.

### ⚙️ Modern Data Components (1.21.1)
* Registered custom Data Component `life_xp_challenge:stored_xp` (`DataComponentType<Integer>`) via Architectury registry.
* Fully migrated `DynamicXpBottleItem` from legacy NBT compound tags to modern Minecraft 1.21.1 `DataComponents`.

### 📖 JEI / EMI Integration & Localization
* Added dedicated JEI Information tabs (`addItemStackInfo`) for Enchanted Books containing `XP Harvester` and `Anomaly Angler`.
* Complete trilingual translations across all new content:
  * **English (`en_us`)**
  * **Russian (`ru_ru`)**
  * **Turkish (`tr_tr`)**
* Full Turkish parity: localized JEI descriptions, enchantment names, ModMenu metadata, and advancements.

### 🛠️ Toolchain & Compatibility
* **Java Toolchain:** Strictly pinned build and runtime toolchain to Java 21 (`JavaLanguageVersion.of(21)`) in root `build.gradle`.
* **Daemon & Run Tasks:** Configured `org.gradle.java.home` to target JDK 21 explicitly.
* **SpongePowered Mixin Compatibility:** Resolved runtime crash (`Unsupported class file major version 69`) when executing `:fabric:runClient` on machines with Java 25 installed by ensuring Mixin runs strictly within Java 21 bytecode limits.
