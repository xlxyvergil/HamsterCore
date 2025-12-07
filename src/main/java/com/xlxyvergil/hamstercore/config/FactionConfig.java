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

public class FactionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "factions.json";
    
    private Map<String, String> entityFactionMap = new HashMap<>();
    private Map<String, String> modDefaultFactions = new HashMap<>();
    private String defaultFaction = "OROKIN";
    
    public static FactionConfig load() {
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        FactionConfig config = new FactionConfig();
        
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
            
            // 添加默认派系说明
            json.addProperty("defaultFaction", defaultFaction);
            
            // 添加实体派系映射
            JsonObject entityMappings = new JsonObject();
            
            // 遍历所有实体类型并添加默认派系映射
            BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                String entityKey = entityId.toString();
                
                // 为每个实体添加默认派系映射
                if (!entityMappings.has(entityKey)) {
                    // 根据已有的规则设置派系
                    if (isInfestedEntity(entityType)) {
                        entityMappings.addProperty(entityKey, "INFESTED");
                    } else if (isSentientEntity(entityType)) {
                        entityMappings.addProperty(entityKey, "SENTIENT");
                    } else if (isMurmurEntity(entityType)) {
                        entityMappings.addProperty(entityKey, "MURMUR");
                    } else if (isCorpusEntity(entityType)) {
                        entityMappings.addProperty(entityKey, "CORPUS");
                    } else if (isGrineerEntity(entityType)) {
                        entityMappings.addProperty(entityKey, "GRINEER");
                    } else {
                        // 默认派系
                        entityMappings.addProperty(entityKey, defaultFaction);
                    }
                }
            });
            
            json.add("entityFactionMap", entityMappings);
            
            // 添加MOD默认派系映射
            JsonObject modMappings = new JsonObject();
            json.add("modDefaultFactions", modMappings);
            
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
        if (json.has("defaultFaction")) {
            defaultFaction = json.get("defaultFaction").getAsString();
        }
        
        if (json.has("entityFactionMap")) {
            JsonObject entityMappings = json.getAsJsonObject("entityFactionMap");
            entityMappings.entrySet().forEach(entry -> 
                entityFactionMap.put(entry.getKey(), entry.getValue().getAsString())
            );
        }
        
        if (json.has("modDefaultFactions")) {
            JsonObject modMappings = json.getAsJsonObject("modDefaultFactions");
            modMappings.entrySet().forEach(entry -> 
                modDefaultFactions.put(entry.getKey(), entry.getValue().getAsString())
            );
        }
    }
    
    public com.xlxyvergil.hamstercore.faction.Faction getFactionForEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        String modId = entityId.getNamespace();
        
        // 首先检查实体特定映射
        if (entityFactionMap.containsKey(entityKey)) {
            String factionName = entityFactionMap.get(entityKey);
            // 确保派系名称有效
            try {
                return com.xlxyvergil.hamstercore.faction.Faction.valueOf(factionName);
            } catch (IllegalArgumentException e) {
                return com.xlxyvergil.hamstercore.faction.Faction.valueOf(defaultFaction);
            }
        }
        
        // 然后检查mod级别的默认映射
        if (modDefaultFactions.containsKey(modId)) {
            String factionName = modDefaultFactions.get(modId);
            // 确保派系名称有效
            try {
                return com.xlxyvergil.hamstercore.faction.Faction.valueOf(factionName);
            } catch (IllegalArgumentException e) {
                return com.xlxyvergil.hamstercore.faction.Faction.valueOf(defaultFaction);
            }
        }
        
        // 返回默认派系
        try {
            return com.xlxyvergil.hamstercore.faction.Faction.valueOf(defaultFaction);
        } catch (IllegalArgumentException e) {
            return com.xlxyvergil.hamstercore.faction.Faction.OROKIN; // fallback to OROKIN
        }
    }
    
    // 判断是否为Infested派系实体
    private boolean isInfestedEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        
        return entityKey.equals("minecraft:zombie") ||
               entityKey.equals("minecraft:drowned") ||
               entityKey.equals("minecraft:husk") ||
               entityKey.equals("minecraft:zoglin") ||
               entityKey.equals("minecraft:zombified_piglin") ||
               entityKey.equals("minecraft:zombie_villager") ||
               entityKey.equals("minecraft:phantom");
    }
    
    // 判断是否为Sentient派系实体
    private boolean isSentientEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        
        return entityKey.equals("minecraft:endermite") ||
               entityKey.equals("minecraft:enderman") ||
               entityKey.equals("minecraft:shulker") ||
               entityKey.equals("minecraft:guardian") ||
               entityKey.equals("minecraft:elder_guardian");
    }
    
    // 判断是否为Murmur派系实体
    private boolean isMurmurEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        
        return entityKey.equals("minecraft:skeleton") ||
               entityKey.equals("minecraft:wither_skeleton") ||
               entityKey.equals("minecraft:stray") ||
               entityKey.equals("minecraft:vex") ||
               entityKey.equals("minecraft:ghast") ||
               entityKey.equals("minecraft:blaze") ||
               entityKey.equals("minecraft:hoglin") ||
               entityKey.equals("minecraft:piglin_brute") ||
               entityKey.equals("minecraft:piglin") ||
               entityKey.equals("minecraft:slime") ||
               entityKey.equals("minecraft:magma_cube");
    }
    
    // 判断是否为Corpus派系实体
    private boolean isCorpusEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        
        return entityKey.equals("minecraft:iron_golem") ||
               entityKey.equals("minecraft:snow_golem") ||
               entityKey.equals("minecraft:player");
    }
    
    // 判断是否为Grineer派系实体
    private boolean isGrineerEntity(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        String entityKey = entityId.toString();
        
        return entityKey.equals("minecraft:witch") ||
               entityKey.equals("minecraft:pillager") ||
               entityKey.equals("minecraft:evoker") ||
               entityKey.equals("minecraft:vindicator") ||
               entityKey.equals("minecraft:ravager");
    }
    
    // Getters
    public Map<String, String> getEntityFactionMap() {
        return entityFactionMap;
    }
    
    public Map<String, String> getModDefaultFactions() {
        return modDefaultFactions;
    }
    
    public String getDefaultFaction() {
        return defaultFaction;
    }
}