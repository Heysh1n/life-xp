package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.POTION);

    // Зелье XP-Щита (3:00, Уровень I)
    public static final RegistrySupplier<Potion> XP_SHIELD = POTIONS.register("xp_shield",
            () -> new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 3600, 0)));

    // Долгое зелье XP-Щита (8:00, Уровень I)
    public static final RegistrySupplier<Potion> LONG_XP_SHIELD = POTIONS.register("long_xp_shield",
            () -> new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 9600, 0)));

    // Сильное зелье XP-Щита (1:30, Уровень II)
    public static final RegistrySupplier<Potion> STRONG_XP_SHIELD = POTIONS.register("strong_xp_shield",
            () -> new Potion("xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 1800, 1)));

    // Ультра зелье XP-Щита (1:00, Уровень III / 75% поглощения)
    public static final RegistrySupplier<Potion> ULTRA_XP_SHIELD = POTIONS.register("ultra_xp_shield",
            () -> new Potion("ultra_xp_shield", new MobEffectInstance(ModMobEffects.XP_SHIELD, 1200, 2)));
}
