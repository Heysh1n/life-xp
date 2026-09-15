package com.hs1n.lifeXp_challenge.event;

import com.hs1n.lifeXp_challenge.loot.AnomalyFishingLootFunction;
import com.hs1n.lifeXp_challenge.loot.HasAnomalyAnglerCondition;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class FishingLootHandler {

    public static void init() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (BuiltInLootTables.FISHING_TREASURE.equals(key)) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HasAnomalyAnglerCondition.builder())
                        .add(LootItem.lootTableItem(ModItems.SHIELD_CORE.get()).setWeight(10))
                        .add(LootItem.lootTableItem(ModItems.LIFE_BOTTLE.get()).setWeight(5))
                        .add(LootItem.lootTableItem(ModItems.DYNAMIC_XP_BOTTLE.get()).setWeight(10))
                        .apply(AnomalyFishingLootFunction.builder());

                context.addPool(pool);
            }
        });
    }

    private FishingLootHandler() {}
}
