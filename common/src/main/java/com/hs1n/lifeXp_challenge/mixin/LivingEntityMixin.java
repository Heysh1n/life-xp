package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.service.KillStreakService;
import com.hs1n.lifeXp_challenge.util.ExperienceOrbHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract boolean wasExperienceConsumed();
    @Shadow public abstract boolean shouldDropExperience();
    @Shadow public abstract int getExperienceReward(ServerLevel level, Entity killer);
    @Shadow protected abstract boolean isAlwaysExperienceDropper();
    @Shadow public abstract int getLastHurtByPlayerMemoryTime();

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void lifeXp$clumpMobDropExperience(ServerLevel serverLevel, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!this.wasExperienceConsumed()
                && (this.isAlwaysExperienceDropper() || (this.getLastHurtByPlayerMemoryTime() > 0 && this.shouldDropExperience() && serverLevel.getGameRules().get(GameRules.MOB_DROPS)))) {
            int reward = this.getExperienceReward(serverLevel, entity);
            if (entity instanceof ServerPlayer player) {
                reward = KillStreakService.modifyExperience(player, reward);
            }
            if (reward > 0) {
                ExperienceOrbHelper.spawnClumpedOrb(serverLevel, self.position(), reward);
            }
            ci.cancel();
        }
    }
}
