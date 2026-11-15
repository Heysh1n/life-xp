package com.hs1n.lifeXp_challenge.loot;

import com.hs1n.lifeXp_challenge.registry.ModDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Устанавливает кастомный компонент STORED_XP со случайным значением 5-40.
 */
public class SetDynamicXpBottleStoredXpFunction extends LootItemConditionalFunction {
//? if >= 26.3 {
    public static final MapCodec<SetDynamicXpBottleStoredXpFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetDynamicXpBottleStoredXpFunction::new)
    );

    public SetDynamicXpBottleStoredXpFunction(java.util.Optional<net.minecraft.core.Holder<LootItemCondition>> predicates) {
        super(predicates);
    }
//?} else {
    /*public static final MapCodec<SetDynamicXpBottleStoredXpFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, SetDynamicXpBottleStoredXpFunction::new)
    );

    public SetDynamicXpBottleStoredXpFunction(List<LootItemCondition> predicates) {
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
    public net.minecraft.world.level.storage.loot.functions.LootItemFunctionType<SetDynamicXpBottleStoredXpFunction> getType() {
        return com.hs1n.lifeXp_challenge.registry.ModLootRegistries.SET_DYNAMIC_XP_BOTTLE_STORED_XP.get();
    }*/
//?}

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int storedXp = context.getRandom().nextIntBetweenInclusive(5, 40);
        stack.set(ModDataComponents.STORED_XP.get(), storedXp);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(SetDynamicXpBottleStoredXpFunction::new);
    }
}
