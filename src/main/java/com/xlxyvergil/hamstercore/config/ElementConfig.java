package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 元素属性配置系统
 * 负责管理武器的基础属性和元素配比
 */
public class ElementConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "weapon_config.json";
    
    // 武器配置映射
    private Map<String, WeaponConfig> weaponConfigs = new HashMap<>();
    
    // 默认武器配置
    private WeaponConfig defaultConfig = new WeaponConfig();
    
    private static ElementConfig instance;
    
    public static ElementConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    public static ElementConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        ElementConfig config = new ElementConfig();
        
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                config.deserialize(json);
            } catch (Exception e) {
                System.err.println("Error loading weapon config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(configFolderPath);
                config.createDefaultConfig(configPath);
            } catch (IOException e) {
                System.err.println("Failed to create weapon config directory or file: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return config;
    }
    
    /**
     * 创建默认配置文件
     * 遍历所有武器和工具，生成默认配置
     */
    private void createDefaultConfig(Path configPath) {
        try {
            JsonObject json = new JsonObject();
            
            // 添加默认配置
            JsonObject defaultConfigJson = new JsonObject();
            defaultConfigJson.addProperty("criticalChance", defaultConfig.getCriticalChance());
            defaultConfigJson.addProperty("criticalDamage", defaultConfig.getCriticalDamage());
            defaultConfigJson.addProperty("triggerChance", defaultConfig.getTriggerChance());
            
            // 添加默认元素配比
            JsonObject elementRatios = new JsonObject();
            for (Map.Entry<String, Double> entry : defaultConfig.getElementRatios().entrySet()) {
                elementRatios.addProperty(entry.getKey(), entry.getValue());
            }
            defaultConfigJson.add("elementRatios", elementRatios);
            json.add("default", defaultConfigJson);
            
            // 为每个武器和工具生成配置
            JsonObject weaponsJson = new JsonObject();
            
            // 遍历所有物品
            for (Item item : ForgeRegistries.ITEMS) {
                ResourceLocation itemRegistryName = ForgeRegistries.ITEMS.getKey(item);
                if (itemRegistryName == null) {
                    continue;
                }
                
                String itemId = itemRegistryName.toString();
                ItemStack stack = new ItemStack(item);
                
                // 检查是否为武器或工具
                if (isWeaponOrTool(stack)) {
                    // 为每个武器/工具创建默认配置
                    WeaponConfig weaponConfig = WeaponConfig.createWeaponConfig(stack);
                    
                    // 应用默认的属性值
                    weaponConfig.setCriticalChance(defaultConfig.getCriticalChance());
                    weaponConfig.setCriticalDamage(defaultConfig.getCriticalDamage());
                    weaponConfig.setTriggerChance(defaultConfig.getTriggerChance());
                    
                    // 添加到JSON
                    JsonObject weaponConfigJson = new JsonObject();
                    weaponConfigJson.addProperty("criticalChance", weaponConfig.getCriticalChance());
                    weaponConfigJson.addProperty("criticalDamage", weaponConfig.getCriticalDamage());
                    weaponConfigJson.addProperty("triggerChance", weaponConfig.getTriggerChance());
                    
                    JsonObject weaponElementRatios = new JsonObject();
                    for (Map.Entry<String, Double> entry : weaponConfig.getElementRatios().entrySet()) {
                        weaponElementRatios.addProperty(entry.getKey(), entry.getValue());
                    }
                    weaponConfigJson.add("elementRatios", weaponElementRatios);
                    
                    weaponsJson.add(itemId, weaponConfigJson);
                    
                    // 存储到内存
                    weaponConfigs.put(itemId, weaponConfig);
                }
            }
            
            json.add("weapons", weaponsJson);
            
            // 写入配置文件
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
            
            System.out.println("Generated weapon config with " + weaponConfigs.size() + " weapons and tools");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 检查物品是否为武器或工具
     */
    private boolean isWeaponOrTool(ItemStack stack) {
        return com.xlxyvergil.hamstercore.element.ElementHelper.isWeaponOrTool(stack);
    }
    
    private void deserialize(JsonObject json) {
        // 解析默认配置
        if (json.has("default")) {
            JsonObject defaultJson = json.getAsJsonObject("default");
            defaultConfig = new WeaponConfig();
            
            if (defaultJson.has("criticalChance")) {
                defaultConfig.setCriticalChance(defaultJson.get("criticalChance").getAsDouble());
            }
            
            if (defaultJson.has("criticalDamage")) {
                defaultConfig.setCriticalDamage(defaultJson.get("criticalDamage").getAsDouble());
            }
            
            if (defaultJson.has("triggerChance")) {
                defaultConfig.setTriggerChance(defaultJson.get("triggerChance").getAsDouble());
            }
            
            if (defaultJson.has("elementRatios")) {
                JsonObject elementRatiosJson = defaultJson.getAsJsonObject("elementRatios");
                Map<String, Double> elementRatios = new HashMap<>();
                elementRatiosJson.entrySet().forEach(entry -> 
                    elementRatios.put(entry.getKey(), entry.getValue().getAsDouble())
                );
                defaultConfig.setElementRatios(elementRatios);
            }
        }
        
        // 解析武器配置
        if (json.has("weapons")) {
            JsonObject weaponsJson = json.getAsJsonObject("weapons");
            weaponsJson.entrySet().forEach(entry -> {
                String itemId = entry.getKey();
                JsonObject weaponJson = entry.getValue().getAsJsonObject();
                
                WeaponConfig weaponConfig = new WeaponConfig();
                
                if (weaponJson.has("criticalChance")) {
                    weaponConfig.setCriticalChance(weaponJson.get("criticalChance").getAsDouble());
                }
                
                if (weaponJson.has("criticalDamage")) {
                    weaponConfig.setCriticalDamage(weaponJson.get("criticalDamage").getAsDouble());
                }
                
                if (weaponJson.has("triggerChance")) {
                    weaponConfig.setTriggerChance(weaponJson.get("triggerChance").getAsDouble());
                }
                
                if (weaponJson.has("elementRatios")) {
                    JsonObject elementRatiosJson = weaponJson.getAsJsonObject("elementRatios");
                    Map<String, Double> elementRatios = new HashMap<>();
                    elementRatiosJson.entrySet().forEach(ratioEntry -> 
                        elementRatios.put(ratioEntry.getKey(), ratioEntry.getValue().getAsDouble())
                    );
                    weaponConfig.setElementRatios(elementRatios);
                }
                
                weaponConfigs.put(itemId, weaponConfig);
            });
        }
    }
    
    /**
     * 获取物品的武器配置
     * 如果没有特定配置，返回默认配置
     */
    public WeaponConfig getWeaponConfig(String itemId) {
        return weaponConfigs.getOrDefault(itemId, defaultConfig);
    }
    
    /**
     * 获取物品的武器配置
     */
    public WeaponConfig getWeaponConfig(ItemStack stack) {
        ResourceLocation itemRegistryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRegistryName != null) {
            return getWeaponConfig(itemRegistryName.toString());
        }
        return defaultConfig;
    }
    
    /**
     * 获取默认武器配置
     */
    public WeaponConfig getDefaultConfig() {
        return defaultConfig;
    }
    
    /**
     * 获取所有武器配置
     */
    public Map<String, WeaponConfig> getAllWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
    }
}