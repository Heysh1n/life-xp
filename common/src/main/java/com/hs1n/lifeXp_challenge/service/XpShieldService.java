package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.registry.ModMobEffects;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;

public class XpShieldService {

    public static float absorbDamage(ServerPlayer player, DamageSource source, float amount) {
        if (amount <= 0.0f || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return amount;
        }

        // Поглощение урона срабатывает ТОЛЬКО если на игроке висит эффект xp_shield
        MobEffectInstance effect = player.getEffect(ModMobEffects.XP_SHIELD);
        if (effect == null) {
            return amount;
        }

        long totalXp = ExperienceUtils.getPlayerTotalXp(player);
        if (totalXp <= 0L) {
            return amount;
        }

        // Математика скейлинга поглощения:
        // Уровень I   (amplifier 0):  25%
        // Уровень II  (amplifier 1):  50%
        // Уровень III+ (amplifier 2+): 75%
        int amplifier = effect.getAmplifier();
        float absorbPercent;
        if (amplifier <= 0) {
            absorbPercent = 0.25f;
        } else if (amplifier == 1) {
            absorbPercent = 0.50f;
        } else {
            absorbPercent = 0.75f;
        }

        float targetAbsorb = amount * absorbPercent;

        // Опыт сжигается эквивалентно поглощенному урону.
        // Если опыта не хватает, сгорает всё до 0, а остаток урона бьет по здоровью.
        float maxAbsorbable = (float) totalXp;
        float damageToAbsorb = Math.max(0.0f, Math.min(targetAbsorb, maxAbsorbable));

        long xpToDeduct = (long) Math.ceil((double) damageToAbsorb);
        xpToDeduct = Math.max(0L, Math.min(totalXp, xpToDeduct));

        if (damageToAbsorb > 0.0f && xpToDeduct > 0L) {
            long newXp = Math.max(0L, totalXp - xpToDeduct);
            newXp = Math.min(totalXp, newXp);

            ExperienceUtils.setPlayerTotalXp(player, newXp);

            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.7f, 1.2f);

            return Math.max(0.0f, amount - damageToAbsorb);
        }

        return amount;
    }
}
