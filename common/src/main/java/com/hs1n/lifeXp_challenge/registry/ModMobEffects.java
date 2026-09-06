package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.effect.XpShieldMobEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> XP_SHIELD = MOB_EFFECTS.register("xp_shield", XpShieldMobEffect::new);
}
