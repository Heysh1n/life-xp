package com.hs1n.lifeXp_challenge.loot;

import com.hs1n.lifeXp_challenge.registry.ModDataComponents;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.registry.ModLootFunctions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AnomalyFishingLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<AnomalyFishingLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, AnomalyFishingLootFunction::new)
    );

    public AnomalyFishingLootFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public LootItemFunctionType<AnomalyFishingLootFunction> getType() {
        return ModLootFunctions.ANOMALY_FISHING_LOOT.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();

        // а) Для life_bottle: оставляем предмету 1-2 единицы прочности до поломки
        if (stack.is(ModItems.LIFE_BOTTLE.get())) {
            int maxDamage = stack.getMaxDamage();
            int remaining = random.nextBoolean() ? 1 : 2;
            int damage = Math.max(1, maxDamage - remaining);
            stack.set(DataComponents.DAMAGE, damage);
        }

        // б) Для dynamic_xp_bottle: случайный stored_xp от 5 до 40
        if (stack.is(ModItems.DYNAMIC_XP_BOTTLE.get())) {
            int randomXp = random.nextInt(5, 41);
            stack.set(ModDataComponents.STORED_XP.get(), randomXp);
        }

        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(AnomalyFishingLootFunction::new);
    }
}
