package com.hs1n.lifeXp_challenge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class LifeXpConfig {
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("life_xp_challenge.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static LifeXpConfig INSTANCE = new LifeXpConfig();

    private int maxLevel = 1000;
    private boolean showDeathCoordinates = true;
    private double deathXpTax = 1.0;
    private boolean enableCustomFog = false;
    private boolean disableFog = false;
    private double fogStartDistance = 15.0;
    private double fogMidDistance = 64.0;
    private double fogEndDistance = 128.0;

    private final Map<String, AttributeConfigNode> nodes = new LinkedHashMap<>();

    public LifeXpConfig() {
        initDefaults();
    }

    private void initDefaults() {
        nodes.put("max_health",               new AttributeConfigNode(0.0, 20.0,  40.0,  DifficultyDirection.DIRECT));
        nodes.put("movement_speed",           new AttributeConfigNode(0.0, 0.015, 0.03,  DifficultyDirection.DIRECT));
        nodes.put("attack_damage",            new AttributeConfigNode(0.0, 2.0,   5.0,   DifficultyDirection.DIRECT));
        nodes.put("attack_speed",             new AttributeConfigNode(0.0, 1.0,   2.0,   DifficultyDirection.DIRECT));
        nodes.put("armor_toughness",          new AttributeConfigNode(0.0, 4.0,   8.0,   DifficultyDirection.DIRECT));
        nodes.put("knockback_resistance",     new AttributeConfigNode(0.0, 0.3,   0.6,   DifficultyDirection.DIRECT));
        nodes.put("block_interaction_range",  new AttributeConfigNode(0.0, 1.0,   2.0,   DifficultyDirection.DIRECT));
        nodes.put("entity_interaction_range", new AttributeConfigNode(0.0, 1.0,   2.0,   DifficultyDirection.DIRECT));
        nodes.put("block_break_speed",        new AttributeConfigNode(0.0, 0.3,   0.6,   DifficultyDirection.DIRECT));
        nodes.put("sneaking_speed",           new AttributeConfigNode(0.0, 0.15,  0.3,   DifficultyDirection.DIRECT));
        nodes.put("submerged_mining_speed",   new AttributeConfigNode(0.0, 1.5,   3.0,   DifficultyDirection.DIRECT));
        nodes.put("sweeping_damage_ratio",    new AttributeConfigNode(0.0, 0.3,   0.6,   DifficultyDirection.DIRECT));
        nodes.put("safe_fall_distance",       new AttributeConfigNode(0.0, 2.0,   5.0,   DifficultyDirection.DIRECT));
        nodes.put("oxygen_bonus",             new AttributeConfigNode(0.0, 0.0,   0.0,   DifficultyDirection.DIRECT));
        nodes.put("burning_time",             new AttributeConfigNode(0.0, 0.0,   0.0,   DifficultyDirection.INVERSE));
    }

    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = Math.max(1, maxLevel); }

    public boolean isShowDeathCoordinates() { return showDeathCoordinates; }
    public void setShowDeathCoordinates(boolean showDeathCoordinates) { this.showDeathCoordinates = showDeathCoordinates; }

    public double getDeathXpTax() { return deathXpTax; }
    public void setDeathXpTax(double deathXpTax) { this.deathXpTax = Math.max(0.0, Math.min(1.0, deathXpTax)); }

    public boolean isEnableCustomFog() { return enableCustomFog; }
    public void setEnableCustomFog(boolean enableCustomFog) { this.enableCustomFog = enableCustomFog; }

    public boolean isDisableFog() { return disableFog; }
    public void setDisableFog(boolean disableFog) { this.disableFog = disableFog; }

    public double getFogStartDistance() { return fogStartDistance; }
    public void setFogStartDistance(double fogStartDistance) { this.fogStartDistance = fogStartDistance; }

    public double getFogMidDistance() { return fogMidDistance; }
    public void setFogMidDistance(double fogMidDistance) { this.fogMidDistance = fogMidDistance; }

    public double getFogEndDistance() { return fogEndDistance; }
    public void setFogEndDistance(double fogEndDistance) { this.fogEndDistance = fogEndDistance; }

    public AttributeConfigNode getNode(String key) { return nodes.get(key); }
    public Map<String, AttributeConfigNode> getNodes() { return nodes; }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                JsonObject root = GSON.fromJson(json, JsonObject.class);
                if (root != null) {
                    LifeXpConfig config = new LifeXpConfig();
                    if (root.has("maxLevel")) config.setMaxLevel(root.get("maxLevel").getAsInt());
                    if (root.has("showDeathCoordinates")) config.setShowDeathCoordinates(root.get("showDeathCoordinates").getAsBoolean());
                    if (root.has("deathXpTax")) config.setDeathXpTax(root.get("deathXpTax").getAsDouble());
                    if (root.has("enableCustomFog")) config.setEnableCustomFog(root.get("enableCustomFog").getAsBoolean());
                    if (root.has("disableFog")) config.setDisableFog(root.get("disableFog").getAsBoolean());
                    if (root.has("fogStartDistance")) config.setFogStartDistance(root.get("fogStartDistance").getAsDouble());
                    if (root.has("fogMidDistance")) config.setFogMidDistance(root.get("fogMidDistance").getAsDouble());
                    if (root.has("fogEndDistance")) config.setFogEndDistance(root.get("fogEndDistance").getAsDouble());
                    if (root.has("attributes")) {
                        JsonObject attrs = root.getAsJsonObject("attributes");
                        for (Map.Entry<String, AttributeConfigNode> entry : config.nodes.entrySet()) {
                            String key = entry.getKey();
                            if (attrs.has(key)) {
                                JsonObject nj = attrs.getAsJsonObject(key);
                                AttributeConfigNode node = entry.getValue();
                                if (nj.has("startValue")) node.setStartValue(nj.get("startValue").getAsDouble());
                                if (nj.has("midValue"))   node.setMidValue(nj.get("midValue").getAsDouble());
                                if (nj.has("endValue"))   node.setEndValue(nj.get("endValue").getAsDouble());
                            }
                        }
                    }
                    INSTANCE = config;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        save();
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("maxLevel", INSTANCE.maxLevel);
        root.addProperty("showDeathCoordinates", INSTANCE.showDeathCoordinates);
        root.addProperty("deathXpTax", INSTANCE.deathXpTax);
        root.addProperty("enableCustomFog", INSTANCE.enableCustomFog);
        root.addProperty("disableFog", INSTANCE.disableFog);
        root.addProperty("fogStartDistance", INSTANCE.fogStartDistance);
        root.addProperty("fogMidDistance", INSTANCE.fogMidDistance);
        root.addProperty("fogEndDistance", INSTANCE.fogEndDistance);

        JsonObject attrs = new JsonObject();
        for (Map.Entry<String, AttributeConfigNode> entry : INSTANCE.nodes.entrySet()) {
            JsonObject nj = new JsonObject();
            AttributeConfigNode node = entry.getValue();
            nj.addProperty("startValue", node.getStartValue());
            nj.addProperty("midValue",   node.getMidValue());
            nj.addProperty("endValue",   node.getEndValue());
            nj.addProperty("direction",  node.getDirection().name());
            attrs.add(entry.getKey(), nj);
        }
        root.add("attributes", attrs);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String exportToJsonString() {
        return GSON.toJson(INSTANCE);
    }
}
