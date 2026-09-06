package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Перехватывает изменения опыта игрока и запускает пересчёт атрибутов.
 * Оптимизировано: пересчёт срабатывает ТОЛЬКО при реальном изменении experienceLevel.
 * Изменение experienceProgress (подбор сфер без апа уровня) полностью игнорируется.
 */
@Mixin(Player.class)
public abstract class ExperienceMixin {

    @Unique
    private int lifeXp$preLevel;

    @ModifyVariable(method = "giveExperiencePoints", at = @At("HEAD"), argsOnly = true)
    private int lifeXp$modifyExperiencePoints(int amount) {
        Player player = (Player) (Object) this;
        if (player instanceof ServerPlayer serverPlayer && amount > 0) {
            return KillStreakService.modifyExperience(serverPlayer, amount);
        }
        return amount;
    }

    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    private void lifeXp$onGiveExperienceLevels(int levels, CallbackInfo ci) {
        AttributeService.recalculate((Player) (Object) this);
    }

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"))
    private void lifeXp$onGiveExperiencePointsHead(int amount, CallbackInfo ci) {
        this.lifeXp$preLevel = ((Player) (Object) this).experienceLevel;
    }

    @Inject(method = "giveExperiencePoints", at = @At("TAIL"))
    private void lifeXp$onGiveExperiencePointsTail(int amount, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        // Пересчёт статов срабатывает ТОЛЬКО при изменении experienceLevel!
        if (player.experienceLevel != this.lifeXp$preLevel) {
            AttributeService.recalculate(player);
        }
    }
}
