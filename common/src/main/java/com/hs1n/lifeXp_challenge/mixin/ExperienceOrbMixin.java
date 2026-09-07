package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.service.AdvancementService;
import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow private int value;
    @Shadow private int count;
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifeXp$clumpNearbyOrbs(CallbackInfo ci) {
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        if (self.level().isClientSide || !self.isAlive() || self.isRemoved()) return;
        if (self.tickCount % 5 != 0) return;

        AABB aabb = self.getBoundingBox().inflate(2.0);
        List<ExperienceOrb> nearby = self.level().getEntitiesOfClass(
                ExperienceOrb.class, aabb,
                other -> other != self && other.isAlive() && !other.isRemoved()
        );

        for (ExperienceOrb other : nearby) {
            ExperienceOrbAccessor otherAccessor = (ExperienceOrbAccessor) other;
            this.value += other.getValue() * Math.max(1, otherAccessor.lifeXp$getCount());
            this.count = 1;
            if (other.getTags().contains("life_xp_ore")) {
                self.addTag("life_xp_ore");
            }
            if (other.getTags().contains("life_xp_fishing")) {
                self.addTag("life_xp_fishing");
            }
            other.discard();
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
