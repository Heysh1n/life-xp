package com.hs1n.lifeXp_challenge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Предмет "Ядро Щита" (Shield Core).
 * Служит катализатором в зельеварке для создания Зелья XP-Щита.
 */
public class ShieldCoreItem extends Item {

    public ShieldCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.life_xp_challenge.shield_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("lifexp.tooltip.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
