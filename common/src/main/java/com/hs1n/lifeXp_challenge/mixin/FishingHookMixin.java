package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.util.ExperienceSourceHelper;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Inject(method = "retrieve", at = @At("HEAD"))
    private void lifeXp$beforeRetrieve(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ExperienceSourceHelper.IS_FISHING_XP.set(true);
    }

    @Inject(method = "retrieve", at = @At("RETURN"))
    private void lifeXp$afterRetrieve(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ExperienceSourceHelper.IS_FISHING_XP.set(false);
    }
}
