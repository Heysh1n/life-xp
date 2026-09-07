package com.hs1n.lifeXp_challenge.network;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.command.LifeXpCommand;
import com.hs1n.lifeXp_challenge.config.LifeXpConfig;
import com.hs1n.lifeXp_challenge.config.LifeXpPresets;
import com.hs1n.lifeXp_challenge.service.AttributeService;
import com.hs1n.lifeXp_challenge.service.KillStreakService;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class LifeXpNetworking {
    public static final ResourceLocation SYNC_STREAK_ID =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "sync_streak");
    public static final ResourceLocation OPEN_PRESET_SCREEN_ID =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "open_preset_screen");
    public static final ResourceLocation CHOOSE_PRESET_ID =
            ResourceLocation.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, "choose_preset");

    /** Вызывать на клиенте при инициализации */
    public static void registerClientReceiver() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C, SYNC_STREAK_ID, (buf, context) -> {
                    int streak = buf.readVarInt();
                    context.queue(() -> KillStreakService.setClientStreak(streak));
                });

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C, OPEN_PRESET_SCREEN_ID, (buf, context) -> {
                    context.queue(com.hs1n.lifeXp_challenge.client.ClientNetworkingHelper::openPresetScreen);
                });
    }

    /** Вызывать на сервере/общем модуле при инициализации */
    public static void registerServerReceiver() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S, CHOOSE_PRESET_ID, (buf, context) -> {
                    String presetName = buf.readUtf();
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer player) {
                            if (LifeXpPresets.apply(presetName)) {
                                LifeXpConfig.save();
                                AttributeService.recalculate(player);
                            }
                            player.addTag(LifeXpCommand.PRESET_TAG);
                        }
                    });
                });
    }

    /** Открыть окно выбора пресета у игрока */
    public static void sendOpenPresetScreen(ServerPlayer player) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        NetworkManager.sendToPlayer(player, OPEN_PRESET_SCREEN_ID, buf);
    }

    /** Отправить выбранный пресет на сервер (вызывается с клиента) */
    public static void sendChoosePreset(String presetName) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), mc.getConnection().registryAccess());
        buf.writeUtf(presetName);
        NetworkManager.sendToServer(CHOOSE_PRESET_ID, buf);
    }

    /** Отправить текущий стрик клиенту */
    public static void syncStreakToClient(ServerPlayer player) {
        int streak = KillStreakService.getStreak(player);
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        buf.writeVarInt(streak);
        NetworkManager.sendToPlayer(player, SYNC_STREAK_ID, buf);
    }
}
