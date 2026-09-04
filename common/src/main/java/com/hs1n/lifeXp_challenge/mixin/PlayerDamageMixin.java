package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.service.XpShieldService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float lifeXp$absorbDamageWithXp(float amount, DamageSource source) {
        Player player = (Player) (Object) this;
        if (player instanceof ServerPlayer serverPlayer) {
            return XpShieldService.absorbDamage(serverPlayer, source, amount);
        }
        return amount;
    }
}
