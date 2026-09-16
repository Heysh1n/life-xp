package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.loot.AnomalyAnglerCondition;
import com.hs1n.lifeXp_challenge.loot.SetDynamicXpBottleStoredXpFunction;
import com.hs1n.lifeXp_challenge.loot.SetLifeBottleDurabilityFunction;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class ModLootRegistries {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.LOOT_CONDITION_TYPE);

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> FUNCTIONS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    public static final RegistrySupplier<MapCodec<AnomalyAnglerCondition>> ANOMALY_ANGLER_CONDITION =
            CONDITIONS.register("anomaly_angler", () -> AnomalyAnglerCondition.CODEC);

    public static final RegistrySupplier<MapCodec<SetLifeBottleDurabilityFunction>> SET_LIFE_BOTTLE_DURABILITY =
            FUNCTIONS.register("set_life_bottle_durability", () -> SetLifeBottleDurabilityFunction.CODEC);

    public static final RegistrySupplier<MapCodec<SetDynamicXpBottleStoredXpFunction>> SET_DYNAMIC_XP_BOTTLE_STORED_XP =
            FUNCTIONS.register("set_dynamic_xp_bottle_stored_xp", () -> SetDynamicXpBottleStoredXpFunction.CODEC);

    public static void init() {
        CONDITIONS.register();
        FUNCTIONS.register();
    }

    private ModLootRegistries() {}
}
