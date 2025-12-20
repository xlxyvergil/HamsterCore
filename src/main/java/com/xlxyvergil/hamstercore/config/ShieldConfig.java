package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ShieldConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "shields.json";
    
    private static ShieldConfig INSTANCE;
    
    private Map<String, Float> entityBaseShields = new HashMap<>();
    private Map<String, Float> factionDefaultShields = new HashMap<>();
    private float playerBaseShield = 200.0f; // 玩家基础护盾值
    
    // 默认启用护盾的派系
    private static final Map<String, Boolean> factionShieldEnabled = new HashMap<>();
    
    static {
        factionShieldEnabled.put("corpus", true);
        factionShieldEnabled.put("orokin", true);
        factionShieldEnabled.put("sentient", true);
    }

    public static ShieldConfig load() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        
        Path configFolderPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        Path configPath = configFolderPath.resolve(CONFIG_FILE_NAME);
        INSTANCE = new ShieldConfig();

        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                INSTANCE.deserialize(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(configFolderPath);
                INSTANCE.createDefaultConfig(configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return INSTANCE;
    }

    private void createDefaultConfig(Path configPath) {
        // 创建默认配置
        try {
            JsonObject json = new JsonObject();
            
            // 添加玩家基础护盾值
            json.addProperty("playerBaseShield", playerBaseShield);
            
            // 添加实体基础护盾映射
            JsonObject entityMappings = new JsonObject();
            
            // 为不同派系的实体添加默认护盾值
            // Corpus派系实体
            entityMappings.addProperty("minecraft:iron_golem", 60.0f);
            entityMappings.addProperty("minecraft:snow_golem", 30.0f);
            
            json.add("entityBaseShields", entityMappings);
            
            // 添加派系默认护盾映射（仅适用于敌对怪物）
            JsonObject factionMappings = new JsonObject();
            factionMappings.addProperty("CORPUS", 20.0f);
            factionMappings.addProperty("OROKIN", 15.0f);
            factionMappings.addProperty("SENTIENT", 10.0f);
            json.add("factionDefaultShields", factionMappings);
            
            // 遍历所有实体类型，添加mod实体到配置文件中
            BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                String entityKey = entityId.toString();
                String modId = entityId.getNamespace();
                
                // 只处理mod实体（非minecraft命名空间）和有生命属性的实体
                if (!entityMappings.has(entityKey) && !modId.equals("minecraft")) {
                    // 检查实体是否有生命属性（通过实体分类判断）
                    MobCategory classification = entityType.getCategory();
                    if (classification != MobCategory.MISC && classification != MobCategory.CREATURE && 
                        classification != MobCategory.AMBIENT && classification != MobCategory.WATER_CREATURE &&
                        classification != MobCategory.WATER_AMBIENT) {
                        // 获取实体的派系
                        String faction = getFactionForEntity(entityType);
                        
                        // 根据派系获取默认护盾值
                        float shieldValue = factionMappings.has(faction) ? factionMappings.get(faction).getAsFloat() : -1.0f;
                        
                        // 只有当派系有默认护盾值时才添加
                        if (shieldValue > 0) {
                            // 添加mod实体到配置文件
                            entityMappings.addProperty(entityKey, shieldValue);
                        }
                    }
                }
            });
            
            // 更新实体护盾映射到json
            json.add("entityBaseShields", entityMappings);
            
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
        if (json.has("playerBaseShield")) {
            playerBaseShield = json.get("playerBaseShield").getAsFloat();
        }
        
        if (json.has("entityBaseShields")) {
            JsonObject entityMappings = json.getAsJsonObject("entityBaseShields");
            entityMappings.entrySet().forEach(entry -> 
                entityBaseShields.put(entry.getKey(), entry.getValue().getAsFloat())
            );
        }
        
        if (json.has("factionDefaultShields")) {
            JsonObject factionMappings = json.getAsJsonObject("factionDefaultShields");
            factionMappings.entrySet().forEach(entry -> 
                factionDefaultShields.put(entry.getKey(), entry.getValue().getAsFloat())
            );
        }
    }

    public float getBaseShieldForEntity(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String entityName = entityId.toString();
        
        // 只检查是否有针对具体实体的配置
        if (entityBaseShields.containsKey(entityName)) {
            return entityBaseShields.get(entityName);
        }
        
        // 返回-1表示未找到配置
        return -1;
    }

    public float getBaseShieldForEntity(EntityType<?> entityType, String faction) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String entityName = entityId.toString();
        
        // 首先检查是否有针对具体实体的配置
        if (entityBaseShields.containsKey(entityName)) {
            return entityBaseShields.get(entityName);
        }
        
        // 然后检查是否有针对派系的默认配置（仅适用于敌对怪物）
        MobCategory classification = entityType.getCategory();
        if (factionDefaultShields.containsKey(faction) && 
            classification != MobCategory.MISC && 
            classification != MobCategory.CREATURE && 
            classification != MobCategory.AMBIENT && 
            classification != MobCategory.WATER_CREATURE &&
            classification != MobCategory.WATER_AMBIENT) {
            return factionDefaultShields.get(faction);
        }
        
        // 返回-1表示未找到配置
        return -1;
    }

    public static boolean isFactionShieldEnabled(String faction) {
        return factionShieldEnabled.getOrDefault(faction.toLowerCase(), false);
    }
    
    /**
     * 检查指定实体是否配置了护盾
     * @param entity 实体的ResourceLocation
     * @return 如果该实体配置了护盾则返回true，否则返回false
     */
    public boolean hasShieldConfigured(ResourceLocation entity) {
        return entityBaseShields.containsKey(entity.toString());
    }
    
    // 获取实体的派系
    private String getFactionForEntity(EntityType<?> entityType) {
        // 使用FactionConfig来获取实体的派系
        FactionConfig factionConfig = FactionConfig.load(); // 注意：这里可能需要调整为单例或其他方式获取
        Faction faction = factionConfig.getFactionForEntity(entityType);
        return faction.name();
    }
    
    // Getters
    public Map<String, Float> getEntityBaseShields() {
        return entityBaseShields;
    }
    
    public Map<String, Float> getFactionDefaultShields() {
        return factionDefaultShields;
    }
    
    public float getPlayerBaseShield() {
        return playerBaseShield;
    }
}