package com.hs1n.lifeXp_challenge.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект XP-Щита.
 * Наличие этого эффекта на игроке активирует поглощение урона за счет опыта:
 * - Уровень I: 25% урона
 * - Уровень II: 50% урона
 * - Уровень III+: 75% урона
 */
public class XpShieldMobEffect extends MobEffect {

    public XpShieldMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55E2E9);
    }
}
