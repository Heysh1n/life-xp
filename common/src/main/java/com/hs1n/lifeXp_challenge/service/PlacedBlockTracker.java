package com.hs1n.lifeXp_challenge.service;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class PlacedBlockTracker extends SavedData {

    public static final String TAG_POSITIONS = "placed_debris";

    private final Set<BlockPos> placedPositions = new HashSet<>();

    public PlacedBlockTracker() {
    }

    public static final Codec<PlacedBlockTracker> CODEC = CompoundTag.CODEC.xmap(
            PlacedBlockTracker::load,
            tracker -> tracker.save(new CompoundTag())
    );

//? if >= 26.1 {
    public static final SavedDataType<PlacedBlockTracker> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "placed_debris"),
            PlacedBlockTracker::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static PlacedBlockTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
//?} else {
  /*public static final SavedData.Factory<PlacedBlockTracker> FACTORY = new SavedData.Factory<>(
            PlacedBlockTracker::new,
            (tag, provider) -> load(tag),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static PlacedBlockTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "placed_debris");
    }*/
//?}

    public boolean contains(BlockPos pos) {
        return placedPositions.contains(pos);
    }

    public void add(BlockPos pos) {
        if (placedPositions.add(pos.immutable())) {
            setDirty();
        }
    }

    public boolean remove(BlockPos pos) {
        if (placedPositions.remove(pos)) {
            setDirty();
            return true;
        }
        return false;
    }

    public Set<BlockPos> getPositions() {
        return placedPositions;
    }

    public Set<BlockPos> getPlacedPositions() {
        return placedPositions;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BlockPos pos : placedPositions) {
            list.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put(TAG_POSITIONS, list);
        return tag;
    }

    public CompoundTag save(CompoundTag tag) {
        return save(tag, null);
    }

    public static PlacedBlockTracker load(CompoundTag tag, HolderLookup.Provider registries) {
        PlacedBlockTracker tracker = new PlacedBlockTracker();
//? if >= 26.1 {
        ListTag list = tag.getListOrEmpty(TAG_POSITIONS);
        for (Tag element : list) {
            if (element instanceof NumericTag numericTag) {
                tracker.placedPositions.add(BlockPos.of(numericTag.longValue()));
            } else if (element instanceof CompoundTag compound) {
                tracker.placedPositions.add(new BlockPos(
                        compound.getIntOr("x", 0),
                        compound.getIntOr("y", 0),
                        compound.getIntOr("z", 0)
                ));
            }
        }
//?} else {
      /*ListTag list = tag.contains(TAG_POSITIONS, 9) ? tag.getList(TAG_POSITIONS, 4) : new ListTag();
        for (int i = 0; i < list.size(); i++) {
            Tag element = list.get(i);
            if (element instanceof NumericTag numericTag) {
                tracker.placedPositions.add(BlockPos.of(numericTag.getAsLong()));
            } else if (element instanceof CompoundTag compound) {
                tracker.placedPositions.add(new BlockPos(
                        compound.getInt("x"),
                        compound.getInt("y"),
                        compound.getInt("z")
                ));
            }
        }*/
//?}
        return tracker;
    }

    public static PlacedBlockTracker load(CompoundTag tag) {
        return load(tag, null);
    }
}
