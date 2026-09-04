package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public class XpShieldService {

    // Сколько очков опыта сжигается за 1 единицу урона (1 единица урона = 0.5 сердечка)
    private static final int XP_PER_DAMAGE = 10; 

    public static float absorbDamage(ServerPlayer player, DamageSource source, float amount) {
        com.hs1n.lifeXp_challenge.config.LifeXpConfig cfg = com.hs1n.lifeXp_challenge.config.LifeXpConfig.INSTANCE;
        if (!cfg.isEnableXpShield() || amount <= 0 || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return amount;
        }

        int xpPerDamage = cfg.getXpShieldPointsPerDamage();
        int totalXp = ExperienceUtils.getPlayerTotalXp(player);
        if (totalXp <= 0) {
            return amount;
        }

        // Вычисляем, сколько урона мы можем впитать
        float maxAbsorbableDamage = (float) totalXp / xpPerDamage;
        float damageToAbsorb = Math.min(amount, maxAbsorbableDamage);

        if (damageToAbsorb > 0) {
            int xpToDeduct = (int) Math.ceil(damageToAbsorb * xpPerDamage);
            int newXp = Math.max(0, totalXp - xpToDeduct);
            
            // Устанавливаем новый опыт
            player.setExperienceLevels(0);
            player.setExperiencePoints(0);
            player.giveExperiencePoints(newXp);
            
            // Пересчитываем атрибуты после изменения опыта
            AttributeService.recalculate(player);

            // Воспроизводим звук энергетического щита (как ломающийся щит или магия)
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.7f, 1.2f);
            
            // Оставшийся урон, который пройдет по здоровью
            return amount - damageToAbsorb;
        }

        return amount;
    }
}
