package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.util.MessageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин в ServerPlayer для отправки координат гибели игроку в чат с золотым префиксом и красным текстом.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {

    @Inject(method = "die", at = @At("HEAD"))
    private void lifeXp$onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (LifeXpConfig.INSTANCE.isShowDeathCoordinates()) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            MessageUtils.sendError(player, "lifexp.message.death_coords",
                    player.getBlockX(),
                    player.getBlockY(),
                    player.getBlockZ()
            );
        }
    }
}
