package com.hs1n.lifeXp_challenge.loot;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemCondition, проверяющий наличие чар anomaly_angler на удочке.
 */
public record AnomalyAnglerCondition() implements LootItemCondition {
//? if >= 26.1 {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "anomaly_angler");
//?} else {
  /*public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "anomaly_angler");*/
//?}
    public static final MapCodec<AnomalyAnglerCondition> CODEC = MapCodec.unit(new AnomalyAnglerCondition());

//? if >= 26.1 {
    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }
//?} else {
    /*@Override
    public net.minecraft.world.level.storage.loot.predicates.LootItemConditionType getType() {
        return com.hs1n.lifeXp_challenge.registry.ModLootRegistries.ANOMALY_ANGLER_CONDITION.get();
    }*/
//?}

    @Override
    public boolean test(LootContext context) {
        return testHook(context);
    }

//? if >= 26.3 {
    private static boolean testHook(LootContext context) {
        ItemStack tool = (ItemStack) context.getOptional(LootContextParams.TOOL);
        if (hasAnomalyAngler(tool)) return true;
        if (context.getOptional(LootContextParams.THIS_ENTITY) instanceof FishingHook hook && hook.getPlayerOwner() != null) {
            return hasAnomalyAngler(hook.getPlayerOwner().getMainHandItem())
                    || hasAnomalyAngler(hook.getPlayerOwner().getOffhandItem());
        }
        return false;
    }
//?}

//? if >= 1.21.4 && < 26.3 {
    /*private static boolean testHook(LootContext context) {
        ItemStack tool = (ItemStack) context.getOptionalParameter(LootContextParams.TOOL);
        if (hasAnomalyAngler(tool)) return true;
        if (context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof FishingHook hook && hook.getPlayerOwner() != null) {
            return hasAnomalyAngler(hook.getPlayerOwner().getMainHandItem())
                    || hasAnomalyAngler(hook.getPlayerOwner().getOffhandItem());
        }
        return false;
    }*/
//?}

//? if <= 1.21.1 {
    /*private static boolean testHook(LootContext context) {
        ItemStack tool = (ItemStack) context.getParamOrNull(LootContextParams.TOOL);
        if (hasAnomalyAngler(tool)) return true;
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook hook && hook.getPlayerOwner() != null) {
            return hasAnomalyAngler(hook.getPlayerOwner().getMainHandItem())
                    || hasAnomalyAngler(hook.getPlayerOwner().getOffhandItem());
        }
        return false;
    }*/
//?}

    private static boolean hasAnomalyAngler(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) return false;
        
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (holder.is(ID)) {
                return true;
            }
        }
        return false;
    }

    public static LootItemCondition.Builder builder() {
        return AnomalyAnglerCondition::new;
    }
}
