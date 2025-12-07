package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DisplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "display.json";
    
    // 显示选项配置
    private boolean showEntityInfoOnDamage = false; // 实体信息显示（攻击时）
    private boolean showNameTagInfo = false; // 名称标签信息显示
    
    private static DisplayConfig instance;
    
    public static DisplayConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    public static DisplayConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        DisplayConfig config = new DisplayConfig();
        
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                config.deserialize(json);
            } catch (Exception e) {
                System.err.println("Error loading display config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(configFolderPath);
                config.createDefaultConfig(configPath);
            } catch (IOException e) {
                System.err.println("Failed to create display config directory or file: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return config;
    }
    
    private void createDefaultConfig(Path configPath) {
        try {
            JsonObject json = new JsonObject();
            
            // 添加默认配置
            json.addProperty("showEntityInfoOnDamage", showEntityInfoOnDamage);
            json.addProperty("showNameTagInfo", showNameTagInfo);
            
            // 添加配置说明
            JsonObject comments = new JsonObject();
            comments.addProperty("showEntityInfoOnDamage", "是否在攻击实体时显示实体信息（等级、护甲、派系等），默认关闭");
            comments.addProperty("showNameTagInfo", "是否在实体名称标签上显示实体信息（等级、派系、护甲等），默认关闭");
            json.add("_comments", comments);
            
            // 写入配置文件
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void deserialize(JsonObject json) {
        if (json.has("showEntityInfoOnDamage")) {
            showEntityInfoOnDamage = json.get("showEntityInfoOnDamage").getAsBoolean();
        }
        
        if (json.has("showNameTagInfo")) {
            showNameTagInfo = json.get("showNameTagInfo").getAsBoolean();
        }
    }
    
    public void save() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        
        try {
            Files.createDirectories(configFolderPath);
            
            JsonObject json = new JsonObject();
            json.addProperty("showEntityInfoOnDamage", showEntityInfoOnDamage);
            json.addProperty("showNameTagInfo", showNameTagInfo);
            
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save display config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters and setters
    public boolean isShowEntityInfoOnDamage() {
        return showEntityInfoOnDamage;
    }
    
    public void setShowEntityInfoOnDamage(boolean showEntityInfoOnDamage) {
        this.showEntityInfoOnDamage = showEntityInfoOnDamage;
    }
    
    public boolean isShowNameTagInfo() {
        return showNameTagInfo;
    }
    
    public void setShowNameTagInfo(boolean showNameTagInfo) {
        this.showNameTagInfo = showNameTagInfo;
    }
}