package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.item.DynamicXpBottleItem;
import com.hs1n.lifeXp_challenge.item.LifeBottleItem;
import com.hs1n.lifeXp_challenge.item.ShieldCoreItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.ITEM);

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, name));
    }

    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(itemKey(name));
    }

    public static final RegistrySupplier<Item> DYNAMIC_XP_BOTTLE = ITEMS.register("dynamic_xp_bottle",
            () -> new DynamicXpBottleItem(itemProps("dynamic_xp_bottle").stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistrySupplier<Item> LIFE_BOTTLE = ITEMS.register("life_bottle",
            () -> new LifeBottleItem(itemProps("life_bottle").durability(3).fireResistant().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistrySupplier<Item> SHIELD_CORE = ITEMS.register("shield_core",
            () -> new ShieldCoreItem(itemProps("shield_core").stacksTo(16).rarity(Rarity.RARE)));
}
