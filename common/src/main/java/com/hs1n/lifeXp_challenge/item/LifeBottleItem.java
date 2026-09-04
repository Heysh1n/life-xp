package com.hs1n.lifeXp_challenge.item;

import com.hs1n.lifeXp_challenge.util.MessageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Пузырёк жизни с прочностью 3.
 * При гибели сохраняет инвентарь игрока через Data Components (1.21.1).
 * При Shift + ПКМ проверяет наличие свободного места в инвентаре и возвращает сохранённые вещи.
 */
public class LifeBottleItem extends Item {

    public static final String NBT_SAVED_INVENTORY = "SavedInventory";

    public LifeBottleItem(Item.Properties properties) {
        // Добавляем флаг скрытия дополнительных ванильных тултипов (включая ванильную прочность)
        super(properties.component(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    public static boolean hasSavedInventory(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.contains(NBT_SAVED_INVENTORY);
    }

    public static ListTag getSavedInventory(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.copyTag();
        return tag.getList(NBT_SAVED_INVENTORY, Tag.TAG_COMPOUND);
    }

    public static void setSavedInventory(ItemStack stack, ListTag listTag) {
        CompoundTag tag = new CompoundTag();
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            tag = existing.copyTag();
        }
        tag.put(NBT_SAVED_INVENTORY, listTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearSavedInventory(ItemStack stack) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            CompoundTag tag = existing.copyTag();
            tag.remove(NBT_SAVED_INVENTORY);
            if (tag.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    /**
     * Подсчитывает общее количество полностью свободных слотов во ВСЕМ инвентаре игрока.
     */
    public static int getEmptySlotCount(Inventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.items) {
            if (item.isEmpty()) count++;
        }
        for (ItemStack armor : inventory.armor) {
            if (armor.isEmpty()) count++;
        }
        for (ItemStack offhand : inventory.offhand) {
            if (offhand.isEmpty()) count++;
        }
        return count;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (hasSavedInventory(stack)) {
            if (player.isShiftKeyDown()) {
                ListTag listTag = getSavedInventory(stack);
                int savedCount = (listTag != null) ? listTag.size() : 0;

                // 1. Точно считаем пустые слоты во всем инвентаре
                int freeSlots = getEmptySlotCount(player.getInventory());

                // 2. Если пустых слотов меньше, чем предметов внутри бутылька
                if (freeSlots < savedCount) {
                    if (level.isClientSide()) {
                        // Жестко отменяем и выводим красное сообщение
                        MessageUtils.sendActionBarError(player, "lifexp.message.not_enough_space");
                    }
                    return InteractionResultHolder.fail(stack);
                }

                // 3. Только если места 100% хватает, выполняем распаковку
                if (!level.isClientSide()) {
                    unpackInventory(level, player, stack, listTag);
                }
                
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private void unpackInventory(Level level, Player player, ItemStack stack, ListTag listTag) {
        if (listTag == null || listTag.isEmpty()) return;

        // Очищаем NBT с инвентарем перед выдачей вещей
        clearSavedInventory(stack);

        Inventory inventory = player.getInventory();
        int restoredCount = 0;

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getInt("Slot");
            ItemStack item = ItemStack.parseOptional(player.registryAccess(), itemTag);
            
            if (!item.isEmpty()) {
                restoredCount++;
                boolean placed = false;
                
                // Шаг 1: Пытаемся вернуть предмет в его родной слот
                if (slot >= 0 && slot < inventory.items.size() && inventory.items.get(slot).isEmpty()) {
                    inventory.items.set(slot, item);
                    placed = true;
                } else if (slot >= 100 && slot < 100 + inventory.armor.size() && inventory.armor.get(slot - 100).isEmpty()) {
                    inventory.armor.set(slot - 100, item);
                    placed = true;
                } else if (slot >= 150 && slot < 150 + inventory.offhand.size() && inventory.offhand.get(slot - 150).isEmpty()) {
                    inventory.offhand.set(slot - 150, item);
                    placed = true;
                }

                // Шаг 2: Если родной слот занят, пробуем обычный add (занимает слоты основного инвентаря)
                if (!placed) {
                    if (inventory.add(item)) {
                        placed = true;
                    }
                }

                // Шаг 3: Если основной инвентарь переполнен, но есть пустые слоты брони/оффхенда
                // (ведь мы считали их как свободные при проверке freeSlots >= savedCount)
                if (!placed) {
                    for (int j = 0; j < inventory.armor.size(); j++) {
                        if (inventory.armor.get(j).isEmpty()) {
                            inventory.armor.set(j, item);
                            placed = true;
                            break;
                        }
                    }
                }
                if (!placed) {
                    for (int j = 0; j < inventory.offhand.size(); j++) {
                        if (inventory.offhand.get(j).isEmpty()) {
                            inventory.offhand.set(j, item);
                            placed = true;
                            break;
                        }
                    }
                }

                // Шаг 4: Экстренный сброс на землю (теоретически никогда не сработает из-за жесткой проверки)
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int usesLeft = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());

        if (hasSavedInventory(stack)) {
            ListTag listTag = getSavedInventory(stack);
            int itemCount = listTag != null ? listTag.size() : 0;
            tooltip.add(Component.translatable("lifexp.life_bottle.contains_items", itemCount)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("lifexp.life_bottle.shift_unpack")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("lifexp.life_bottle.empty_desc")
                    .withStyle(ChatFormatting.GRAY));
        }

        // Цветокоррекция (светофор) для одной кастомной строки прочности
        ChatFormatting durabilityColor;
        if (usesLeft >= 3) {
            durabilityColor = ChatFormatting.GREEN;
        } else if (usesLeft == 2) {
            durabilityColor = ChatFormatting.GOLD;
        } else {
            durabilityColor = ChatFormatting.RED;
        }

        tooltip.add(Component.translatable("lifexp.life_bottle.durability", usesLeft, stack.getMaxDamage())
                .withStyle(durabilityColor));
    }
}
