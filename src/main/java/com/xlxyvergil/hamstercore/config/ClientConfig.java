package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "client.json";
    
    // 客户端显示选项配置
    private boolean showEntityShieldBar = true; // 是否在实体头顶显示护盾条
    
    private static ClientConfig instance;
    
    public static ClientConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    public static ClientConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        ClientConfig config = new ClientConfig();
        
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                config.deserialize(json);
            } catch (Exception e) {
                System.err.println("Error loading client config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(configFolderPath);
                config.createDefaultConfig(configPath);
            } catch (IOException e) {
                System.err.println("Failed to create client config directory or file: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return config;
    }
    
    private void createDefaultConfig(Path configPath) {
        try {
            JsonObject json = new JsonObject();
            
            // 添加默认配置
            json.addProperty("showEntityShieldBar", showEntityShieldBar);
            
            // 添加配置说明
            JsonObject comments = new JsonObject();
            comments.addProperty("showEntityShieldBar", "是否在实体头顶显示护盾条，默认开启");
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
        if (json.has("showEntityShieldBar")) {
            showEntityShieldBar = json.get("showEntityShieldBar").getAsBoolean();
        }
    }
    
    public void save() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        
        try {
            Files.createDirectories(configFolderPath);
            
            JsonObject json = new JsonObject();
            json.addProperty("showEntityShieldBar", showEntityShieldBar);
            
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save client config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getter
    public boolean isShowEntityShieldBar() {
        return showEntityShieldBar;
    }
    
    // Setter
    public void setShowEntityShieldBar(boolean showEntityShieldBar) {
        this.showEntityShieldBar = showEntityShieldBar;
    }
}