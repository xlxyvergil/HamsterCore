package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LevelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "levels.json";
    
    // 等级配置参数
    private boolean usePlayerDays = true; // true表示使用玩家生存天数，false表示使用服务器游戏天数
    private int baseLevelPerDay = 5; // 每天增加的基础等级
    private int distanceBaseLevel = 50; // 1000格内的基础等级
    private int distanceLevelPer100Blocks = 25; // 每100格增加的等级
    private int distanceThreshold = 1000; // 距离阈值
    
    public static LevelConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        LevelConfig config = new LevelConfig();
        
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                config.deserialize(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(configFolderPath);
                config.createDefaultConfig(configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return config;
    }
    
    private void createDefaultConfig(Path configPath) {
        try {
            JsonObject json = new JsonObject();
            
            // 添加配置说明
            json.addProperty("usePlayerDays", usePlayerDays); // true=使用玩家生存天数, false=使用服务器游戏天数
            json.addProperty("baseLevelPerDay", baseLevelPerDay); // 每天增加的基础等级
            json.addProperty("distanceBaseLevel", distanceBaseLevel); // 1000格内的基础等级
            json.addProperty("distanceLevelPer100Blocks", distanceLevelPer100Blocks); // 每100格增加的等级
            json.addProperty("distanceThreshold", distanceThreshold); // 距离阈值
            
            // 写入配置文件
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
            
            deserialize(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void deserialize(JsonObject json) {
        if (json.has("usePlayerDays")) {
            usePlayerDays = json.get("usePlayerDays").getAsBoolean();
        }
        
        if (json.has("baseLevelPerDay")) {
            baseLevelPerDay = json.get("baseLevelPerDay").getAsInt();
        }
        
        if (json.has("distanceBaseLevel")) {
            distanceBaseLevel = json.get("distanceBaseLevel").getAsInt();
        }
        
        if (json.has("distanceLevelPer100Blocks")) {
            distanceLevelPer100Blocks = json.get("distanceLevelPer100Blocks").getAsInt();
        }
        
        if (json.has("distanceThreshold")) {
            distanceThreshold = json.get("distanceThreshold").getAsInt();
        }
    }
    
    // Getters
    public boolean isUsePlayerDays() {
        return usePlayerDays;
    }
    
    public int getBaseLevelPerDay() {
        return baseLevelPerDay;
    }
    
    public int getDistanceBaseLevel() {
        return distanceBaseLevel;
    }
    
    public int getDistanceLevelPer100Blocks() {
        return distanceLevelPer100Blocks;
    }
    
    public int getDistanceThreshold() {
        return distanceThreshold;
    }
}