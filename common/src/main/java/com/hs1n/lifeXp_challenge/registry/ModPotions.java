package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public final class ModPotions {
//? if >= 26.1 {
    private static Identifier potionId(String name) {
        return Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, name);
    }
//?} else {
  /*private static ResourceLocation potionId(String name) {
        return ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, name);
    }*/
//?}

    // Зелье XP-Щита (3:00, Уровень I)
    public static final Holder<Potion> XP_SHIELD = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            potionId("xp_shield"),
            new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 3600, 0))
    );

    // Долгое зелье XP-Щита (8:00, Уровень I)
    public static final Holder<Potion> LONG_XP_SHIELD = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            potionId("long_xp_shield"),
            new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 9600, 0))
    );

    // Сильное зелье XP-Щита (1:30, Уровень II)
    public static final Holder<Potion> STRONG_XP_SHIELD = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            potionId("strong_xp_shield"),
            new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 1800, 1))
    );

    // Ультра зелье XP-Щита (1:00, Уровень III / 75% поглощения)
    public static final Holder<Potion> ULTRA_XP_SHIELD = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            potionId("ultra_xp_shield"),
            new Potion("ultra_xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 1200, 2))
    );

    public static void init() {
        // Trigger classloading and registration into BuiltInRegistries
    }

    private ModPotions() {}
}
