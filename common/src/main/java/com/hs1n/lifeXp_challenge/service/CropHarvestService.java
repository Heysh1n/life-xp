package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.util.ExperienceOrbHelper;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CropHarvestService {

    private CropHarvestService() {}

    /**
     * Architectury InteractionEvent.RIGHT_CLICK_BLOCK handler.
     */
    public static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (player.level().isClientSide()) return EventResult.pass();
        if (hand != InteractionHand.MAIN_HAND) return EventResult.pass();
        if (!(player.level() instanceof ServerLevel serverLevel)) return EventResult.pass();
        if (!(player instanceof ServerPlayer serverPlayer)) return EventResult.pass();

        BlockState state = serverLevel.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop)) return EventResult.pass();
        if (!crop.isMaxAge(state)) return EventResult.pass();

        // Harvest the clicked crop
        harvestCrop(serverLevel, serverPlayer, pos, state, crop);

        // AoE: if holding a hoe with XP Harvester level >= 2
        ItemStack tool = serverPlayer.getMainHandItem();
        if (tool.is(ItemTags.HOES) && XpHarvesterService.getHarvesterLevel(tool) >= 2) {
            tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
            int extraHarvested = 0;
            int maxExtra = 3;
            // Scan 3x1x3 area centered on the clicked block
            for (int dx = -1; dx <= 1 && extraHarvested < maxExtra && !tool.isEmpty(); dx++) {
                for (int dz = -1; dz <= 1 && extraHarvested < maxExtra && !tool.isEmpty(); dz++) {
                    if (dx == 0 && dz == 0) continue; // skip original
                    BlockPos nearby = pos.offset(dx, 0, dz);
                    BlockState nearbyState = serverLevel.getBlockState(nearby);
                    if (nearbyState.getBlock() instanceof CropBlock nearbyCrop && nearbyCrop.isMaxAge(nearbyState)) {
                        harvestCrop(serverLevel, serverPlayer, nearby, nearbyState, nearbyCrop);
                        tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
                        extraHarvested++;
                    }
                }
            }
        }

        return EventResult.interruptTrue();
    }

    private static void harvestCrop(ServerLevel level, ServerPlayer player, BlockPos pos,
                                     BlockState state, CropBlock crop) {
        // Get drops
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, null, player, player.getMainHandItem()));

        // Identify the seed by getting drops from the crop at age 0 (which only drops the seed)
        BlockState youngState = crop.getStateForAge(0);
        List<ItemStack> seedDrops = Block.getDrops(youngState, level, pos, null, player, ItemStack.EMPTY);
        ItemStack seedStack = seedDrops.isEmpty() ? ItemStack.EMPTY : seedDrops.getFirst();

        // Remove one seed from drops
        boolean seedRemoved = false;
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (!seedRemoved && ItemStack.isSameItem(drop, seedStack)) {
                if (drop.getCount() > 1) {
                    drop.shrink(1);
                } else {
                    drops.remove(i);
                }
                seedRemoved = true;
                break;
            }
        }

        // Spawn remaining drops
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        // Reset crop age to 0
        level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);

        // Play sound
        level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);

        // Spawn small XP if holding XP Harvester enchant
        int enchLevel = XpHarvesterService.getHarvesterLevel(player.getMainHandItem());
        if (enchLevel > 0) {
            ExperienceOrbHelper.spawnClumpedOrb(level, Vec3.atCenterOf(pos), 1 + level.getRandom().nextInt(2));
        }
    }
}
