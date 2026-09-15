package com.hs1n.lifeXp_challenge.loot;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModLootFunctions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class HasAnomalyAnglerCondition implements LootItemCondition {
    public static final ResourceLocation ANOMALY_ANGLER_ID = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "anomaly_angler");
    public static final ResourceKey<Enchantment> ANOMALY_ANGLER_KEY = ResourceKey.create(Registries.ENCHANTMENT, ANOMALY_ANGLER_ID);

    public static final HasAnomalyAnglerCondition INSTANCE = new HasAnomalyAnglerCondition();
    public static final MapCodec<HasAnomalyAnglerCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public LootItemConditionType getType() {
        return ModLootFunctions.HAS_ANOMALY_ANGLER.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        ItemEnchantments enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(ANOMALY_ANGLER_KEY) || entry.getKey().is(ANOMALY_ANGLER_ID)) {
                return entry.getIntValue() > 0;
            }
        }
        return false;
    }

    public static LootItemCondition.Builder builder() {
        return () -> INSTANCE;
    }
}
