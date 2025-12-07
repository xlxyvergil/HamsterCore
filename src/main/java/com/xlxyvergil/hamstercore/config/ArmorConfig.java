package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ArmorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "armors.json";
    
    private Map<String, Double> entityArmorMap = new HashMap<>();
    private Map<String, Double> modDefaultArmors = new HashMap<>();
    private Map<String, Double> factionDefaultArmors = new HashMap<>();
    private double defaultArmor = 10.0;
    
    public static ArmorConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        ArmorConfig config = new ArmorConfig();
        
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
        // 创建默认配置
        try {
            JsonObject json = new JsonObject();
            
            // 添加默认护甲值说明
            json.addProperty("defaultArmor", defaultArmor);
            
            // 添加实体护甲映射
            JsonObject entityMappings = new JsonObject();
            
            // 为不同派系的实体添加默认护甲值
            // Grineer派系实体
            entityMappings.addProperty("minecraft:witch", 15.0);
            entityMappings.addProperty("minecraft:pillager", 12.0);
            entityMappings.addProperty("minecraft:evoker", 8.0);
            entityMappings.addProperty("minecraft:vindicator", 20.0);
            entityMappings.addProperty("minecraft:ravager", 25.0);
            
            // Infested派系实体
            entityMappings.addProperty("minecraft:zombie", 8.0);
            entityMappings.addProperty("minecraft:drowned", 8.0);
            entityMappings.addProperty("minecraft:husk", 8.0);
            entityMappings.addProperty("minecraft:zoglin", 12.0);
            entityMappings.addProperty("minecraft:zombified_piglin", 8.0);
            entityMappings.addProperty("minecraft:zombie_villager", 8.0);
            entityMappings.addProperty("minecraft:phantom", 4.0);
            
            // Corpus派系实体
            entityMappings.addProperty("minecraft:iron_golem", 50.0);
            entityMappings.addProperty("minecraft:snow_golem", 5.0);
            
            // Sentient派系实体
            entityMappings.addProperty("minecraft:endermite", 2.0);
            entityMappings.addProperty("minecraft:enderman", 10.0);
            entityMappings.addProperty("minecraft:shulker", 20.0);
            entityMappings.addProperty("minecraft:guardian", 15.0);
            entityMappings.addProperty("minecraft:elder_guardian", 25.0);
            
            // Murmur派系实体
            entityMappings.addProperty("minecraft:skeleton", 6.0);
            entityMappings.addProperty("minecraft:wither_skeleton", 10.0);
            entityMappings.addProperty("minecraft:stray", 6.0);
            entityMappings.addProperty("minecraft:vex", 2.0);
            entityMappings.addProperty("minecraft:ghast", 8.0);
            entityMappings.addProperty("minecraft:blaze", 12.0);
            entityMappings.addProperty("minecraft:hoglin", 15.0);
            entityMappings.addProperty("minecraft:piglin_brute", 18.0);
            entityMappings.addProperty("minecraft:piglin", 8.0);
            entityMappings.addProperty("minecraft:slime", 4.0);
            entityMappings.addProperty("minecraft:magma_cube", 4.0);
            
            json.add("entityArmorMap", entityMappings);
            
            // 添加MOD默认护甲映射
            JsonObject modMappings = new JsonObject();
            modMappings.addProperty("minecraft", 10.0);
            json.add("modDefaultArmors", modMappings);
            
            // 添加派系默认护甲映射
            JsonObject factionMappings = new JsonObject();
            factionMappings.addProperty("GRINEER", 15.0);
            factionMappings.addProperty("INFESTED", 8.0);
            factionMappings.addProperty("CORPUS", 20.0);
            factionMappings.addProperty("OROKIN", 30.0);
            factionMappings.addProperty("SENTIENT", 25.0);
            factionMappings.addProperty("MURMUR", 12.0);
            json.add("factionDefaultArmors", factionMappings);
            
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
        if (json.has("defaultArmor")) {
            defaultArmor = json.get("defaultArmor").getAsDouble();
        }
        
        if (json.has("entityArmorMap")) {
            JsonObject entityMappings = json.getAsJsonObject("entityArmorMap");
            entityMappings.entrySet().forEach(entry -> 
                entityArmorMap.put(entry.getKey(), entry.getValue().getAsDouble())
            );
        }
        
        if (json.has("modDefaultArmors")) {
            JsonObject modMappings = json.getAsJsonObject("modDefaultArmors");
            modMappings.entrySet().forEach(entry -> 
                modDefaultArmors.put(entry.getKey(), entry.getValue().getAsDouble())
            );
        }
        
        if (json.has("factionDefaultArmors")) {
            JsonObject factionMappings = json.getAsJsonObject("factionDefaultArmors");
            factionMappings.entrySet().forEach(entry -> 
                factionDefaultArmors.put(entry.getKey(), entry.getValue().getAsDouble())
            );
        }
    }
    
    public double getArmorForEntity(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String entityName = entityId.toString();
        String modId = entityId.getNamespace();
        
        // 首先检查是否有针对具体实体的配置
        if (entityArmorMap.containsKey(entityName)) {
            return entityArmorMap.get(entityName);
        }
        
        // 然后检查是否有针对MOD的默认配置
        if (modDefaultArmors.containsKey(modId)) {
            return modDefaultArmors.get(modId);
        }
        
        // 最后使用全局默认值
        return defaultArmor;
    }
    
    public double getArmorForEntity(EntityType<?> entityType, String faction) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String entityName = entityId.toString();
        String modId = entityId.getNamespace();
        
        // 首先检查是否有针对具体实体的配置
        if (entityArmorMap.containsKey(entityName)) {
            return entityArmorMap.get(entityName);
        }
        
        // 然后检查是否有针对MOD的默认配置
        if (modDefaultArmors.containsKey(modId)) {
            return modDefaultArmors.get(modId);
        }
        
        // 然后检查是否有针对派系的默认配置
        if (factionDefaultArmors.containsKey(faction)) {
            return factionDefaultArmors.get(faction);
        }
        
        // 最后使用全局默认值
        return defaultArmor;
    }
    
    public double getDefaultArmor() {
        return defaultArmor;
    }
    
    public Map<String, Double> getEntityArmorMap() {
        return entityArmorMap;
    }
    
    public Map<String, Double> getModDefaultArmors() {
        return modDefaultArmors;
    }
    
    public Map<String, Double> getFactionDefaultArmors() {
        return factionDefaultArmors;
    }
}