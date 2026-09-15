package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.effect.XpShieldMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> XP_SHIELD =
            MOB_EFFECTS.register("xp_shield", XpShieldMobEffect::new);

    public static void init() {
        MOB_EFFECTS.register();
    }

    private ModMobEffects() {}
}
