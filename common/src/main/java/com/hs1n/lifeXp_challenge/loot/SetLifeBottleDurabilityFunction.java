package com.hs1n.lifeXp_challenge.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Устанавливает ванильный компонент DataComponents.DAMAGE с остатком 1-2 ед. прочности.
 */
public class SetLifeBottleDurabilityFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetLifeBottleDurabilityFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetLifeBottleDurabilityFunction::new)
    );

    public SetLifeBottleDurabilityFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int maxDamage = stack.getMaxDamage();
        int remainingDurability = context.getRandom().nextInt(2) + 1; // 1 или 2 ед. прочности
        int damage = Math.max(0, maxDamage - remainingDurability);
        stack.set(DataComponents.DAMAGE, damage);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(SetLifeBottleDurabilityFunction::new);
    }
}
