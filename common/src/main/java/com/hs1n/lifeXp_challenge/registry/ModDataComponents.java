package com.hs1n.lifeXp_challenge.registry;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(LifeXpChallenge.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<Integer>> STORED_XP =
            DATA_COMPONENTS.register("stored_xp", () -> DataComponentType.<Integer>builder()
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public record SavedSlot(int slot, ItemStack stack) {
        public static final Codec<SavedSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(SavedSlot::slot),
                ItemStack.CODEC.fieldOf("item").forGetter(SavedSlot::stack)
        ).apply(instance, SavedSlot::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SavedSlot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SavedSlot::slot,
                ItemStack.STREAM_CODEC, SavedSlot::stack,
                SavedSlot::new
        );
    }

    public record SavedInventory(List<SavedSlot> slots) {
        public static final SavedInventory EMPTY = new SavedInventory(List.of());

        public static final Codec<SavedInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SavedSlot.CODEC.listOf().fieldOf("slots").forGetter(SavedInventory::slots)
        ).apply(instance, SavedInventory::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SavedInventory> STREAM_CODEC = StreamCodec.composite(
                SavedSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), SavedInventory::slots,
                SavedInventory::new
        );

        public boolean isEmpty() {
            return slots == null || slots.isEmpty();
        }

        public int size() {
            return slots != null ? slots.size() : 0;
        }
    }

    public static final RegistrySupplier<DataComponentType<SavedInventory>> SAVED_INVENTORY =
            DATA_COMPONENTS.register("saved_inventory", () -> DataComponentType.<SavedInventory>builder()
                    .persistent(SavedInventory.CODEC)
                    .networkSynchronized(SavedInventory.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static void init() {
        DATA_COMPONENTS.register();
    }

    private ModDataComponents() {}
}
