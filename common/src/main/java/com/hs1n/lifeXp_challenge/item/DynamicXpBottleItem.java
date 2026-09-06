package com.hs1n.lifeXp_challenge.item;

import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Динамический пузырёк опыта, хранящий точное количество очков опыта в Data Components.
 * При Shift + ПКМ переливает опыт из игрока в пузырёк (с налогом 10%).
 * При обычном ПКМ мгновенно возвращает все очки опыта игроку и возвращает пустую бутылочку.
 */
public class DynamicXpBottleItem extends Item {

    public static final String NBT_STORED_XP = "StoredXp";

    public DynamicXpBottleItem(Item.Properties properties) {
        super(properties);
    }

    public static int getStoredXp(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0;
        CompoundTag tag = customData.copyTag();
        return tag.getInt(NBT_STORED_XP);
    }

    public static void setStoredXp(ItemStack stack, int points) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(NBT_STORED_XP, Math.max(0, points));
        });
    }

    public static void addStoredXp(ItemStack stack, int points) {
        if (points <= 0) return;
        int current = getStoredXp(stack);
        setStoredXp(stack, current + points);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (ExperienceUtils.getPlayerTotalXp(player) > 0) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        } else {
            // Обычный ПКМ — поглощение опыта
            int stored = getStoredXp(stack);
            if (stored > 0) {
                if (!level.isClientSide()) {
                    player.giveExperiencePoints(stored);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.2f);

                    stack.shrink(1);
                    ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(emptyBottle)) {
                        player.drop(emptyBottle, false);
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof Player player)) return;

        int ticksUsed = getUseDuration(stack, livingEntity) - remainingUseTicks;
        if (ticksUsed > 0 && ticksUsed % 10 == 0) {
            if (!level.isClientSide()) {
                if (ExperienceUtils.getPlayerTotalXp(player) > 0) {
                    // Забираем эквивалент 1 уровня
                    int targetLevel = Math.max(0, player.experienceLevel - 1);
                    int needed = ExperienceUtils.getXpNeededToLevelUp(targetLevel);
                    int deducted = ExperienceUtils.deductPlayerXp(player, needed);

                    if (deducted > 0) {
                        int stored = (int) Math.round(deducted * 0.9); // 10% налог в пустоту
                        if (stored <= 0) stored = 1;
                        addStoredXp(stack, stored);

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8f, 1.0f + (float) (Math.random() * 0.2));
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 1.2f);
                    }
                } else {
                    player.stopUsingItem();
                }
            }
        }
    }

    /**
     * Динамический цвет жидкости от светло-зелёного (мало опыта) до глубокого тёмно-зелёного (много опыта).
     */
    public static int getXpColor(ItemStack stack) {
        int xpPoints = getStoredXp(stack);
        float t = Math.min(1.0f, (float) xpPoints / 3000.0f);
        // Интерполяция от яркого салатового (160, 255, 60) к насыщенному тёмно-зелёному (10, 80, 20)
        int r = (int) (160 + t * (10 - 160));
        int g = (int) (255 + t * (80 - 255));
        int b = (int) (60 + t * (20 - 60));
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredXp(stack) > 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int storedXp = getStoredXp(stack);
        int approxLevels = ExperienceUtils.calculateLevelFromXp(storedXp);

        tooltip.add(Component.translatable("lifexp.xp_bottle.stored_points", storedXp, approxLevels)
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.life_xp_challenge.dynamic_xp_bottle.desc")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("lifexp.tooltip.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
