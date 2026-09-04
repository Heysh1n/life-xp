package com.hs1n.lifeXp_challenge.mixin;

import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import com.hs1n.lifeXp_challenge.util.MessageUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин в BottleItem для запуска процесса забора опыта в стеклянную бутылочку при Shift + ПКМ.
 */
@Mixin(BottleItem.class)
public abstract class MixinBottleItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void lifeXp$onUse(Level level, Player player, InteractionHand hand,
                              CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (player.isShiftKeyDown()) {
            if (ExperienceUtils.getPlayerTotalXp(player) > 0) {
                player.startUsingItem(hand);
                cir.setReturnValue(InteractionResultHolder.consume(player.getItemInHand(hand)));
            } else {
                if (level.isClientSide()) {
                    MessageUtils.sendActionBarError(player, "lifexp.message.not_enough_xp");
                }
                cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
            }
        }
    }
}
