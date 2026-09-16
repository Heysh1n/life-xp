package com.hs1n.lifeXp_challenge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Предмет "Ядро Щита" (Shield Core).
 * Служит катализатором в зельеварке для создания Зелья XP-Щита.
 */
public class ShieldCoreItem extends Item {

    public ShieldCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.life_xp_challenge.shield_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("lifexp.tooltip.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
