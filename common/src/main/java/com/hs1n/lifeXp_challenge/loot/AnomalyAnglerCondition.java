package com.hs1n.lifeXp_challenge.loot;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
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
    public static final Identifier ID = Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "anomaly_angler");
    public static final MapCodec<AnomalyAnglerCondition> CODEC = MapCodec.unit(new AnomalyAnglerCondition());

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        ItemStack tool = (ItemStack) context.getOptionalParameter(LootContextParams.TOOL);
        if (hasAnomalyAngler(tool)) {
            return true;
        }

        if (context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof FishingHook hook && hook.getPlayerOwner() != null) {
            return hasAnomalyAngler(hook.getPlayerOwner().getMainHandItem())
                    || hasAnomalyAngler(hook.getPlayerOwner().getOffhandItem());
        }

        return false;
    }

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
