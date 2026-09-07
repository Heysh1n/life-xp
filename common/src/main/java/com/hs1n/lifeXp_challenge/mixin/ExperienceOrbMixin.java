package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.service.AdvancementService;
import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow public abstract int getValue();

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("RETURN"))
    private void lifeXp$onOrbConstructed(Level level, double x, double y, double z, int value, CallbackInfo ci) {
        ExperienceOrb orb = (ExperienceOrb) (Object) this;
        if (ExperienceSourceHelper.IS_ORE_XP.get()) {
            orb.addTag("life_xp_ore");
        }
        if (ExperienceSourceHelper.IS_FISHING_XP.get()) {
            orb.addTag("life_xp_fishing");
        }
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private void lifeXp$onPlayerTouchOrb(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ExperienceOrb orb = (ExperienceOrb) (Object) this;
            if (orb.getTags().contains("life_xp_ore")) {
                AdvancementService.addOreXp(serverPlayer, this.getValue());
            } else if (orb.getTags().contains("life_xp_fishing")) {
                AdvancementService.addFishingXp(serverPlayer, this.getValue());
            }
        }
    }
}
