package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.item.LifeBottleItem;
import com.hs1n.lifeXp_challenge.registry.ModItems;
import com.hs1n.lifeXp_challenge.util.MessageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис управления сохранением и восстановлением инвентаря через Пузырёк Жизни (Data Components 1.21.1).
 */
public final class LifeBottleService {

    private static final Map<UUID, ItemStack> VOID_SAVED_BOTTLES = new ConcurrentHashMap<>();

    private LifeBottleService() {}

    public static void storeVoidBottle(UUID playerUuid, ItemStack bottle) {
        VOID_SAVED_BOTTLES.put(playerUuid, bottle);
    }

    public static ItemStack retrieveVoidBottle(UUID playerUuid) {
        return VOID_SAVED_BOTTLES.remove(playerUuid);
    }

    /**
     * Обрабатывает гибель игрока при наличии пустого Пузырька Жизни.
     * @return true, если инвентарь был упакован в бутылёк и стандартный дроп отменяется.
     */
    public static boolean handleDeathInventory(ServerPlayer player) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return false;
        }

        Inventory inventory = player.getInventory();

        // 1. Поиск пустого бутылька жизни
        int bottleSlot = -1;
        boolean isArmor = false;
        boolean isOffhand = false;
        ItemStack bottleStack = ItemStack.EMPTY;

        // Основной инвентарь (0..35)
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (stack.is(ModItems.LIFE_BOTTLE.get()) && !LifeBottleItem.hasSavedInventory(stack)) {
                bottleSlot = i;
                bottleStack = stack;
                break;
            }
        }

        // Вторая рука (150)
        if (bottleStack.isEmpty()) {
            for (int i = 0; i < inventory.offhand.size(); i++) {
                ItemStack stack = inventory.offhand.get(i);
                if (stack.is(ModItems.LIFE_BOTTLE.get()) && !LifeBottleItem.hasSavedInventory(stack)) {
                    bottleSlot = i;
                    bottleStack = stack;
                    isOffhand = true;
                    break;
                }
            }
        }

        // Слоты брони (100..103)
        if (bottleStack.isEmpty()) {
            for (int i = 0; i < inventory.armor.size(); i++) {
                ItemStack stack = inventory.armor.get(i);
                if (stack.is(ModItems.LIFE_BOTTLE.get()) && !LifeBottleItem.hasSavedInventory(stack)) {
                    bottleSlot = i;
                    bottleStack = stack;
                    isArmor = true;
                    break;
                }
            }
        }

        if (bottleStack.isEmpty()) {
            return false;
        }

        // 2. Создаем заполненный экземпляр бутылька
        ItemStack filledBottle = bottleStack.copy();
        filledBottle.setCount(1);
        // Тратим 1 единицу прочности
        filledBottle.setDamageValue(filledBottle.getDamageValue() + 1);

        // 3. Сериализуем все 41 слот инвентаря через Data Components
        ListTag listTag = new ListTag();

        // Основные слоты (0..35)
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (stack.isEmpty()) continue;
            if (!isArmor && !isOffhand && i == bottleSlot) {
                if (stack.getCount() > 1) {
                    ItemStack remaining = stack.copy();
                    remaining.shrink(1);
                    CompoundTag tag = (CompoundTag) remaining.save(player.registryAccess());
                    tag.putInt("Slot", i);
                    listTag.add(tag);
                }
            } else {
                CompoundTag tag = (CompoundTag) stack.save(player.registryAccess());
                tag.putInt("Slot", i);
                listTag.add(tag);
            }
        }

        // Слоты брони (100..103)
        for (int i = 0; i < inventory.armor.size(); i++) {
            ItemStack stack = inventory.armor.get(i);
            if (stack.isEmpty()) continue;
            if (isArmor && i == bottleSlot) {
                if (stack.getCount() > 1) {
                    ItemStack remaining = stack.copy();
                    remaining.shrink(1);
                    CompoundTag tag = (CompoundTag) remaining.save(player.registryAccess());
                    tag.putInt("Slot", 100 + i);
                    listTag.add(tag);
                }
            } else {
                CompoundTag tag = (CompoundTag) stack.save(player.registryAccess());
                tag.putInt("Slot", 100 + i);
                listTag.add(tag);
            }
        }

        // Вторая рука (150)
        for (int i = 0; i < inventory.offhand.size(); i++) {
            ItemStack stack = inventory.offhand.get(i);
            if (stack.isEmpty()) continue;
            if (isOffhand && i == bottleSlot) {
                if (stack.getCount() > 1) {
                    ItemStack remaining = stack.copy();
                    remaining.shrink(1);
                    CompoundTag tag = (CompoundTag) remaining.save(player.registryAccess());
                    tag.putInt("Slot", 150 + i);
                    listTag.add(tag);
                }
            } else {
                CompoundTag tag = (CompoundTag) stack.save(player.registryAccess());
                tag.putInt("Slot", 150 + i);
                listTag.add(tag);
            }
        }

        // Сохраняем в Data Components (1.21.1)
        LifeBottleItem.setSavedInventory(filledBottle, listTag);

        // 4. Очищаем инвентарь игрока
        inventory.clearContent();

        // 5. Проверка защиты от падения в пустоту (Void Protection)
        DamageSource damageSource = player.getLastDamageSource();
        boolean isVoid = (damageSource != null && damageSource.is(DamageTypes.FELL_OUT_OF_WORLD))
                || player.getY() < player.level().getMinBuildHeight();

        if (isVoid) {
            storeVoidBottle(player.getUUID(), filledBottle);
            MessageUtils.sendInfo(player, "lifexp.message.bottle_void_saved");
        } else {
            double spawnY = Math.max(player.getY(), player.level().getMinBuildHeight() + 1.0);
            ItemEntity entity = new ItemEntity(player.level(), player.getX(), spawnY, player.getZ(), filledBottle);
            entity.setInvulnerable(true);
            entity.setUnlimitedLifetime();
            entity.setPickUpDelay(10);
            player.level().addFreshEntity(entity);
            MessageUtils.sendInfo(player, "lifexp.message.bottle_saved");
        }

        return true;
    }
}
