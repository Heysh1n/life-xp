package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.service.LifeBottleService;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин в Player для:
 * 1. Перехвата дропа инвентаря при наличии Пузырька Жизни.
 * 2. Идеально точного расчёта выпадающего опыта при смерти (Death XP Tax) с защитой от Integer Overflow.
 */
@Mixin(Player.class)
public abstract class MixinPlayer {

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void lifeXp$onDropEquipment(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player instanceof ServerPlayer serverPlayer) {
            if (LifeBottleService.handleDeathInventory(serverPlayer)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getBaseExperienceReward", at = @At("HEAD"), cancellable = true)
    private void lifeXp$getBaseExperienceReward(CallbackInfoReturnable<Integer> cir) {
        Player player = (Player) (Object) this;
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
            double tax = LifeXpConfig.INSTANCE.getDeathXpTax();
            if (player instanceof ServerPlayer serverPlayer) {
                tax = com.hs1n.lifeXp_challenge.service.DeathTaxService.calculateSmartTax(serverPlayer);
            }
            // Точный расчёт всего опыта в long с защитой от переполнения
            long totalXp = ExperienceUtils.getPlayerTotalXp(player);
            long xpToDrop = Math.round(totalXp * tax);
            cir.setReturnValue((int) Math.min((long) Integer.MAX_VALUE, Math.max(0L, xpToDrop)));
        } else {
            cir.setReturnValue(0);
        }
    }
}
