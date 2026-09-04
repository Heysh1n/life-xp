package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.item.DynamicXpBottleItem;
import com.hs1n.lifeXp_challenge.item.LifeBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(LifeXpChallenge.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

    public static final RegistrySupplier<Item> DYNAMIC_XP_BOTTLE = ITEMS.register("dynamic_xp_bottle", () -> new DynamicXpBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistrySupplier<Item> LIFE_BOTTLE = ITEMS.register("life_bottle", () -> new LifeBottleItem(new Item.Properties().durability(3).fireResistant().stacksTo(1).rarity(Rarity.EPIC)));
}
