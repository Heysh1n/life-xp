package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.loot.AnomalyAnglerCondition;
import com.hs1n.lifeXp_challenge.loot.SetDynamicXpBottleStoredXpFunction;
import com.hs1n.lifeXp_challenge.loot.SetLifeBottleDurabilityFunction;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public final class FishingLootService {

    public static void init() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (BuiltInLootTables.FISHING_TREASURE.equals(key)) {
                LootPool.Builder pool = LootPool.lootPool()
                        .when(AnomalyAnglerCondition.builder())
                        .add(LootItem.lootTableItem(ModItems.LIFE_BOTTLE.get())
                                .setWeight(10)
                                .apply(SetLifeBottleDurabilityFunction.builder()))
                        .add(LootItem.lootTableItem(ModItems.DYNAMIC_XP_BOTTLE.get())
                                .setWeight(15)
                                .apply(SetDynamicXpBottleStoredXpFunction.builder()))
                        .add(LootItem.lootTableItem(ModItems.SHIELD_CORE.get())
                                .setWeight(10));
                context.addPool(pool);
            }
        });
    }

    private FishingLootService() {}
}
