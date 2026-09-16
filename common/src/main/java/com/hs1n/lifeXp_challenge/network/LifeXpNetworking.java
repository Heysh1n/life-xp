package com.hs1n.lifeXp_challenge.network;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.command.LifeXpCommand;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class LifeXpNetworking {

    public record SyncStreakPayload(int streak) implements CustomPacketPayload {
        public static final Type<SyncStreakPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "sync_streak"));
        public static final StreamCodec<ByteBuf, SyncStreakPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, SyncStreakPayload::streak,
                        SyncStreakPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenPresetScreenPayload() implements CustomPacketPayload {
        public static final Type<OpenPresetScreenPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "open_preset_screen"));
        public static final StreamCodec<ByteBuf, OpenPresetScreenPayload> STREAM_CODEC =
                StreamCodec.unit(new OpenPresetScreenPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ChoosePresetPayload(String presetName) implements CustomPacketPayload {
        public static final Type<ChoosePresetPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "choose_preset"));
        public static final StreamCodec<ByteBuf, ChoosePresetPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ChoosePresetPayload::presetName,
                        ChoosePresetPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ForceSyncAttributesPayload() implements CustomPacketPayload {
        public static final Type<ForceSyncAttributesPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "force_sync_attributes"));
        public static final StreamCodec<ByteBuf, ForceSyncAttributesPayload> STREAM_CODEC =
                StreamCodec.unit(new ForceSyncAttributesPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Вызывать на клиенте при инициализации */
    public static void registerClientReceiver() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C, SyncStreakPayload.TYPE, SyncStreakPayload.STREAM_CODEC, (payload, context) -> {
                    context.queue(() -> KillStreakService.setClientStreak(payload.streak()));
                });

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C, OpenPresetScreenPayload.TYPE, OpenPresetScreenPayload.STREAM_CODEC, (payload, context) -> {
                    context.queue(com.hs1n.lifeXp_challenge.client.ClientNetworkingHelper::openPresetScreen);
                });
    }

    /** Вызывать на сервере/общем модуле при инициализации */
    public static void registerServerReceiver() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S, ChoosePresetPayload.TYPE, ChoosePresetPayload.STREAM_CODEC, (payload, context) -> {
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer player) {
                            if (LifeXpPresets.apply(payload.presetName())) {
                                LifeXpConfig.save();
                                AttributeService.recalculate(player);
                            }
                            player.addTag(LifeXpCommand.PRESET_TAG);
                        }
                    });
                });

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S, ForceSyncAttributesPayload.TYPE, ForceSyncAttributesPayload.STREAM_CODEC, (payload, context) -> {
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer player) {
                            AttributeService.recalculate(player, true);
                            
                            java.util.List<net.minecraft.world.entity.ai.attributes.AttributeInstance> attributesToSync = new java.util.ArrayList<>();
                            var maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                            if (maxHealth != null) attributesToSync.add(maxHealth);
                            
                            if (!attributesToSync.isEmpty()) {
                                player.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(player.getId(), attributesToSync));
                            }
                        }
                    });
                });
    }

    /** Открыть окно выбора пресета у игрока */
    public static void sendOpenPresetScreen(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, new OpenPresetScreenPayload());
    }

    /** Отправить выбранный пресет на сервер (вызывается с клиента) */
    public static void sendChoosePreset(String presetName) {
        NetworkManager.sendToServer(new ChoosePresetPayload(presetName));
    }

    /** Принудительно запросить синхронизацию атрибутов с сервера (вызывается с клиента) */
    public static void sendForceSyncAttributes() {
        NetworkManager.sendToServer(new ForceSyncAttributesPayload());
    }

    /** Отправить текущий стрик клиенту */
    public static void syncStreakToClient(ServerPlayer player) {
        int streak = KillStreakService.getStreak(player);
        NetworkManager.sendToPlayer(player, new SyncStreakPayload(streak));
    }
}
