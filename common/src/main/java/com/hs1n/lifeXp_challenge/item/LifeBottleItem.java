package com.hs1n.lifeXp_challenge.item;

import com.hs1n.lifeXp_challenge.registry.ModDataComponents;
import com.hs1n.lifeXp_challenge.registry.ModDataComponents.SavedInventory;
import com.hs1n.lifeXp_challenge.registry.ModDataComponents.SavedSlot;
import com.hs1n.lifeXp_challenge.util.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Пузырёк жизни с прочностью 3.
 * При гибели сохраняет инвентарь игрока через Data Components.
 * При Shift + ПКМ проверяет наличие свободного места в инвентаре и возвращает сохранённые вещи.
 */
public class LifeBottleItem extends Item {

    public LifeBottleItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean hasSavedInventory(ItemStack stack) {
        SavedInventory inv = stack.get(ModDataComponents.SAVED_INVENTORY.get());
        return inv != null && !inv.isEmpty();
    }

    public static SavedInventory getSavedInventory(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SAVED_INVENTORY.get(), SavedInventory.EMPTY);
    }

    public static void setSavedInventory(ItemStack stack, SavedInventory inventory) {
        if (inventory == null || inventory.isEmpty()) {
            clearSavedInventory(stack);
        } else {
            stack.set(ModDataComponents.SAVED_INVENTORY.get(), inventory);
        }
    }

    public static void clearSavedInventory(ItemStack stack) {
        stack.remove(ModDataComponents.SAVED_INVENTORY.get());
    }

    /**
     * Подсчитывает общее количество полностью свободных слотов во ВСЕМ инвентаре игрока.
     */
    public static int getEmptySlotCount(Inventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getNonEquipmentItems()) {
            if (item.isEmpty()) count++;
        }
        for (int i = 0; i < 4; i++) {
            if (inventory.getItem(36 + i).isEmpty()) count++;
        }
        for (int i = 0; i < 1; i++) {
            if (inventory.getItem(40 + i).isEmpty()) count++;
        }
        return count;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hasSavedInventory(stack)) {
            if (player.isShiftKeyDown()) {
                SavedInventory savedInv = getSavedInventory(stack);
                int savedCount = savedInv.size();

                // 1. Точно считаем пустые слоты во всем инвентаре
                int freeSlots = getEmptySlotCount(player.getInventory());

                // 2. Если пустых слотов меньше, чем предметов внутри бутылька
                if (freeSlots < savedCount) {
                    if (level.isClientSide()) {
                        // Жестко отменяем и выводим красное сообщение
                        MessageUtils.sendActionBarError(player, "lifexp.message.not_enough_space");
                    }
                    return InteractionResult.FAIL;
                }

                // 3. Только если места 100% хватает, выполняем распаковку
                if (!level.isClientSide()) {
                    unpackInventory(level, player, stack, savedInv);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void unpackInventory(Level level, Player player, ItemStack stack, SavedInventory savedInv) {
        if (savedInv.isEmpty()) return;

        // Очищаем компонент с инвентарем перед выдачей вещей
        clearSavedInventory(stack);

        Inventory inventory = player.getInventory();
        int restoredCount = 0;

        for (SavedSlot savedSlot : savedInv.slots()) {
            int slot = savedSlot.slot();
            ItemStack item = savedSlot.stack().copy();

            if (!item.isEmpty()) {
                restoredCount++;
                boolean placed = false;

                // Шаг 1: Пытаемся вернуть предмет в его родной слот
                if (slot >= 0 && slot < 36 && inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, item);
                    placed = true;
                } else if (slot >= 100 && slot < 100 + 4 && inventory.getItem(36 + slot - 100).isEmpty()) {
                    inventory.setItem(36 + slot - 100, item);
                    placed = true;
                } else if (slot >= 150 && slot < 150 + 1 && inventory.getItem(40 + slot - 150).isEmpty()) {
                    inventory.setItem(40 + slot - 150, item);
                    placed = true;
                }

                // Шаг 2: Если родной слот занят, пробуем обычный add
                if (!placed) {
                    if (inventory.add(item)) {
                        placed = true;
                    }
                }

                // Шаг 3: Если основной инвентарь переполнен, но есть пустые слоты брони/оффхенда
                if (!placed) {
                    for (int j = 0; j < 4; j++) {
                        if (inventory.getItem(36 + j).isEmpty()) {
                            inventory.setItem(36 + j, item);
                            placed = true;
                            break;
                        }
                    }
                }
                if (!placed) {
                    for (int j = 0; j < 1; j++) {
                        if (inventory.getItem(40 + j).isEmpty()) {
                            inventory.setItem(40 + j, item);
                            placed = true;
                            break;
                        }
                    }
                }

                // Шаг 4: Экстренный сброс на землю
                if (!placed) {
                    player.drop(item, false);
                }
            }
        }

        // Звуковые эффекты
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);

        MessageUtils.sendInfo(player, "lifexp.message.bottle_restored", restoredCount);

        // Если бутылек исчерпал прочность (>= 3 урона), ломаем его
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
            stack.shrink(1);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasSavedInventory(stack) || super.isFoil(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        int usesLeft = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        
        ChatFormatting durabilityColor = ChatFormatting.GREEN;
        float ratio = (float) usesLeft / stack.getMaxDamage();
        if (ratio <= 0.25f) {
            durabilityColor = ChatFormatting.RED;
        } else if (ratio <= 0.5f) {
            durabilityColor = ChatFormatting.YELLOW;
        }

        if (hasSavedInventory(stack)) {
            int itemCount = getSavedInventory(stack).size();
            tooltip.accept(Component.translatable("lifexp.life_bottle.contains_items", itemCount)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.life_xp_challenge.life_bottle.desc")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.accept(Component.translatable("lifexp.tooltip.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.accept(Component.translatable("lifexp.life_bottle.durability", usesLeft, stack.getMaxDamage())
                .withStyle(durabilityColor));
    }
}
