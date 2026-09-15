package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.loot.AnomalyFishingLootFunction;
import com.hs1n.lifeXp_challenge.loot.HasAnomalyAnglerCondition;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.LOOT_CONDITION_TYPE);

    public static final RegistrySupplier<LootItemFunctionType<AnomalyFishingLootFunction>> ANOMALY_FISHING_LOOT =
            LOOT_FUNCTIONS.register("anomaly_fishing_loot", () -> new LootItemFunctionType<>(AnomalyFishingLootFunction.CODEC));

    public static final RegistrySupplier<LootItemConditionType> HAS_ANOMALY_ANGLER =
            LOOT_CONDITIONS.register("has_anomaly_angler", () -> new LootItemConditionType(HasAnomalyAnglerCondition.CODEC));

    public static void init() {
        LOOT_FUNCTIONS.register();
        LOOT_CONDITIONS.register();
    }

    private ModLootFunctions() {}
}
