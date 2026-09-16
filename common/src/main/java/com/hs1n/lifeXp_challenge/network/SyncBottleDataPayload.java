package com.hs1n.lifeXp_challenge.network;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.registry.ModDataComponents.SavedInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record SyncBottleDataPayload(
        int slotIndex,
        ItemStack itemStack,
        SavedInventory savedInventory
) implements CustomPacketPayload {

    public static final Type<SyncBottleDataPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "sync_bottle_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBottleDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncBottleDataPayload::slotIndex,
                    ItemStack.STREAM_CODEC, SyncBottleDataPayload::itemStack,
                    SavedInventory.STREAM_CODEC, SyncBottleDataPayload::savedInventory,
                    SyncBottleDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
