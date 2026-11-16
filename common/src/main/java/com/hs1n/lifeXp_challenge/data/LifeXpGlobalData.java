package com.hs1n.lifeXp_challenge.data;

import com.hs1n.lifeXp_challenge.LifeXpChallenge;
import com.hs1n.lifeXp_challenge.util.ExperienceUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Глобальное хранилище данных мода LIFE-XP с системой "Двойного маппинга" (Dual-Key Identity Linking).
 * Решает проблему смены UUID на пиратских лаунчерах (Ely.by) и смены никнеймов на лицензии.
 * Файл сохранения: world/data/lifexp_players.dat
 */
public class LifeXpGlobalData extends SavedData {

    public static final String DATA_NAME = "lifexp_players";

    private final Map<String, CompoundTag> accounts = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuidLinker = new ConcurrentHashMap<>();
    private final Map<String, String> nameLinker = new ConcurrentHashMap<>();

    public LifeXpGlobalData() {
    }

    public static final Codec<LifeXpGlobalData> CODEC = CompoundTag.CODEC.xmap(
            LifeXpGlobalData::load,
            data -> data.save(new CompoundTag())
    );

//? if >= 26.1 {
    public static final SavedDataType<LifeXpGlobalData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(LifeXpChallenge.MOD_ID, DATA_NAME),
            LifeXpGlobalData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static LifeXpGlobalData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }
//?} else {
  /*public static final SavedData.Factory<LifeXpGlobalData> FACTORY = new SavedData.Factory<>(
            LifeXpGlobalData::new,
            (tag, provider) -> load(tag),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static LifeXpGlobalData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }*/
//?}

    public static LifeXpGlobalData get(MinecraftServer server) {
        return get(server.overworld());
    }

    public Map<String, CompoundTag> getAccounts() {
        return accounts;
    }

    public Map<UUID, String> getUuidLinker() {
        return uuidLinker;
    }

    public Map<String, String> getNameLinker() {
        return nameLinker;
    }

    public synchronized CompoundTag getPlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String internalId = uuidLinker.get(uuid);
        if (internalId == null) {
            String name = player.getScoreboardName().toLowerCase(Locale.ROOT);
            internalId = nameLinker.get(name);
        }
        if (internalId == null) {
            internalId = resolveAccountId(uuid, player.getScoreboardName());
        }
        return accounts.computeIfAbsent(internalId, k -> new CompoundTag());
    }

    /**
     * Алгоритм двойного маппинга (Dual-Key Identity Linking):
     * 1. Проверяем uuidLinker -> если есть, обновляем nameLinker новым ником (смена ника на лицензии).
     * 2. Проверяем nameLinker -> если есть, обновляем uuidLinker новым UUID (скачущий UUID на пиратке).
     * 3. Новый игрок -> генерируем Internal_Account_ID, связываем в обоих мапах и создаём CompoundTag.
     */
    public synchronized String resolveAccountId(UUID uuid, String rawName) {
        String name = rawName.toLowerCase(Locale.ROOT);
        String internalId;

        // ШАГ 1: Проверяем uuidLinker
        if (uuidLinker.containsKey(uuid)) {
            internalId = uuidLinker.get(uuid);
            nameLinker.put(name, internalId);
            setDirty();
        }
        // ШАГ 2: Проверяем nameLinker
        else if (nameLinker.containsKey(name)) {
            internalId = nameLinker.get(name);
            uuidLinker.put(uuid, internalId);
            setDirty();
        }
        // ШАГ 3: Новый игрок
        else {
            internalId = UUID.randomUUID().toString();
            uuidLinker.put(uuid, internalId);
            nameLinker.put(name, internalId);
            accounts.put(internalId, new CompoundTag());
            setDirty();
        }

        return internalId;
    }

    /**
     * Принудительная загрузка данных мода при входе игрока (игнорируя сброс ванильного NBT).
     */
    public synchronized void onPlayerJoin(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String name = player.getScoreboardName();
        String internalId = resolveAccountId(uuid, name);

        CompoundTag accountData = accounts.computeIfAbsent(internalId, k -> new CompoundTag());

        // Восстановление опыта мода
        if (accountData.contains("TotalXp")) {
//? if >= 26.1 {
            long totalXp = accountData.getLongOr("TotalXp", 0L);
//?} else {
          /*long totalXp = accountData.getLong("TotalXp");*/
//?}
            ExperienceUtils.setPlayerTotalXp(player, totalXp);
        } else {
            accountData.putLong("TotalXp", ExperienceUtils.getPlayerTotalXp(player));
            setDirty();
        }

        // Восстановление тегов мода (килл-стрик, пресеты, флаги)
        if (accountData.contains("Tags")) {
//? if >= 26.1 {
            ListTag tagsTag = accountData.getListOrEmpty("Tags");
            for (Tag tag : tagsTag) {
                tag.asString().ifPresent(player::addTag);
            }
//?} else {
          /*ListTag tagsTag = accountData.getList("Tags", 8);
            for (int i = 0; i < tagsTag.size(); i++) {
                player.addTag(tagsTag.getString(i));
            }*/
//?}
        }
    }

    /**
     * Сохранение данных мода для игрока в глобальную базу.
     */
    public synchronized void savePlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String internalId = uuidLinker.get(uuid);
        if (internalId == null) {
            String name = player.getScoreboardName().toLowerCase(Locale.ROOT);
            internalId = nameLinker.get(name);
        }
        if (internalId == null) {
            internalId = resolveAccountId(uuid, player.getScoreboardName());
        }

        CompoundTag accountData = accounts.computeIfAbsent(internalId, k -> new CompoundTag());
        accountData.putLong("TotalXp", ExperienceUtils.getPlayerTotalXp(player));

        ListTag tagsTag = new ListTag();
//? if >= 26.1 {
        for (String tag : player.entityTags()) {
//?} else {
      /*for (String tag : player.getTags()) {*/
//?}
            if (tag.startsWith("lifexp_")) {
                tagsTag.add(StringTag.valueOf(tag));
            }
        }
        accountData.put("Tags", tagsTag);
        setDirty();
    }

    public synchronized void onPlayerQuit(ServerPlayer player) {
        savePlayerData(player);
    }

    public synchronized void saveAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            savePlayerData(player);
        }
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag accountsTag = new CompoundTag();
        for (Map.Entry<String, CompoundTag> entry : accounts.entrySet()) {
            accountsTag.put(entry.getKey(), entry.getValue().copy());
        }
        tag.put("Accounts", accountsTag);

        CompoundTag uuidTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : uuidLinker.entrySet()) {
            uuidTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("UuidLinker", uuidTag);

        CompoundTag nameTag = new CompoundTag();
        for (Map.Entry<String, String> entry : nameLinker.entrySet()) {
            nameTag.putString(entry.getKey(), entry.getValue());
        }
        tag.put("NameLinker", nameTag);

        return tag;
    }

    public CompoundTag save(CompoundTag tag) {
        return save(tag, null);
    }

    public static LifeXpGlobalData load(CompoundTag tag, HolderLookup.Provider registries) {
        LifeXpGlobalData data = new LifeXpGlobalData();

//? if >= 26.1 {
        CompoundTag accountsTag = tag.getCompoundOrEmpty("Accounts");
        for (String id : accountsTag.keySet()) {
            accountsTag.getCompound(id).ifPresent(accTag -> data.accounts.put(id, accTag.copy()));
        }

        CompoundTag uuidTag = tag.getCompoundOrEmpty("UuidLinker");
        for (String uuidStr : uuidTag.keySet()) {
            try {
                UUID u = UUID.fromString(uuidStr);
                uuidTag.getString(uuidStr).ifPresent(id -> data.uuidLinker.put(u, id));
            } catch (IllegalArgumentException ignored) {}
        }

        CompoundTag nameTag = tag.getCompoundOrEmpty("NameLinker");
        for (String name : nameTag.keySet()) {
            nameTag.getString(name).ifPresent(id -> data.nameLinker.put(name.toLowerCase(Locale.ROOT), id));
        }
//?} else {
      /*CompoundTag accountsTag = tag.getCompound("Accounts");
        for (String id : accountsTag.getAllKeys()) {
            data.accounts.put(id, accountsTag.getCompound(id).copy());
        }

        CompoundTag uuidTag = tag.getCompound("UuidLinker");
        for (String uuidStr : uuidTag.getAllKeys()) {
            try {
                UUID u = UUID.fromString(uuidStr);
                data.uuidLinker.put(u, uuidTag.getString(uuidStr));
            } catch (IllegalArgumentException ignored) {}
        }

        CompoundTag nameTag = tag.getCompound("NameLinker");
        for (String name : nameTag.getAllKeys()) {
            data.nameLinker.put(name.toLowerCase(Locale.ROOT), nameTag.getString(name));
        }*/
//?}

        return data;

    }

    public static LifeXpGlobalData load(CompoundTag tag) {
        return load(tag, null);
    }

}
