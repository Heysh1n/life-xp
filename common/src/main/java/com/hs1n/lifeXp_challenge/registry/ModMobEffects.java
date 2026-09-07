package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.effect.XpShieldMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class ModMobEffects {
    public static final Holder<MobEffect> XP_SHIELD = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "xp_shield"),
            new XpShieldMobEffect()
    );

    public static void init() {
        // Trigger classloading and registration into BuiltInRegistries
    }

    private ModMobEffects() {}
}
