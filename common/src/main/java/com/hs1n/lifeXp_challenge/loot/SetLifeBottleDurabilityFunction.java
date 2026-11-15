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
//? if >= 26.3 {
    public static final MapCodec<SetLifeBottleDurabilityFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetLifeBottleDurabilityFunction::new)
    );

    public SetLifeBottleDurabilityFunction(java.util.Optional<net.minecraft.core.Holder<LootItemCondition>> predicates) {
        super(predicates);
    }
//?} else {
    /*public static final MapCodec<SetLifeBottleDurabilityFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetLifeBottleDurabilityFunction::new)
    );

    public SetLifeBottleDurabilityFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }*/
//?}

//? if >= 26.1 {
    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }
//?} else {
  /*@Override
    public net.minecraft.world.level.storage.loot.functions.LootItemFunctionType<SetLifeBottleDurabilityFunction> getType() {
        return com.hs1n.lifeXp_challenge.registry.ModLootRegistries.SET_LIFE_BOTTLE_DURABILITY.get();
    }*/
//?}

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
