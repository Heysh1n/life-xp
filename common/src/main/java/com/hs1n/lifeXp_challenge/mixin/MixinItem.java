package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.item.DynamicXpBottleItem;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин в Item для поддержки зажатия стеклянной бутылочки (onUseTick) и переливания опыта в DYNAMIC_XP_BOTTLE.
 */
@Mixin(Item.class)
public abstract class MixinItem {

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void lifeXp$getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(UseAnim.BOW);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void lifeXp$getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void lifeXp$onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (stack.is(Items.GLASS_BOTTLE) && livingEntity instanceof Player player) {
            Item item = (Item) (Object) this;
            int ticksUsed = item.getUseDuration(stack, player) - remainingUseTicks;

            if (ticksUsed > 0 && ticksUsed % 10 == 0) {
                if (!level.isClientSide()) {
                    if (ExperienceUtils.getPlayerTotalXp(player) > 0) {
                        int targetLevel = Math.max(0, player.experienceLevel - 1);
                        int needed = ExperienceUtils.getXpNeededToLevelUp(targetLevel);
                        int deducted = ExperienceUtils.deductPlayerXp(player, needed);

                        if (deducted > 0) {
                            int stored = (int) Math.round(deducted * 0.9); // 10% налог
                            if (stored <= 0) stored = 1;

                            ItemStack dynamicBottle = new ItemStack(ModItems.DYNAMIC_XP_BOTTLE.get());
                            DynamicXpBottleItem.setStoredXp(dynamicBottle, stored);

                            player.stopUsingItem();
                            ItemStack result = ItemUtils.createFilledResult(stack, player, dynamicBottle);
                            player.setItemInHand(player.getUsedItemHand(), result);

                            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8f, 1.0f);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 1.2f);
                        }
                    } else {
                        player.stopUsingItem();
                    }
                }
            }
        }
    }
}
