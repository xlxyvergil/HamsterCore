package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;

import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * TACZ武器配置管理器
 * 负责加载、保存和管理TACZ枪械的元素配置
 */
public class TacZWeaponConfig {
    
    private static final String TACZ_MOD_ID = "tacz";
    
    // 默认特殊属性值
    private static final double DEFAULT_CRITICAL_CHANCE = 0.05; // 5%暴击率
    private static final double DEFAULT_CRITICAL_DAMAGE = 1.5;  // 1.5倍暴击伤害
    private static final double DEFAULT_TRIGGER_CHANCE = 0.1;   // 10%触发率
    
    // 默认物理元素占比
    private static final double DEFAULT_SLASH = 0.3;
    private static final double DEFAULT_IMPACT = 0.3;
    private static final double DEFAULT_PUNCTURE = 0.4;
    
    // 配置文件路径
    private static final String CONFIG_DIR = "config/hamstercore/";
    private static final String WEAPON_DIR = CONFIG_DIR + "Weapon/";
    public static final String TACZ_WEAPONS_FILE = WEAPON_DIR + "tacz_weapons.json";
    
    // TACZ配置缓存
    private static Map<String, WeaponData> taczConfigCache = new HashMap<>();
    
    // TACZ gunId到配置的映射表
    private static Map<String, WeaponData> gunIdToConfigMap = new HashMap<>();
    
    /**
     * 生成TACZ武器配置文件（仅当文件不存在时）
     */
    public static void generateTacZWeaponsConfig() {
        // 只有当TACZ模组加载时才生成配置
        if (!ModList.get().isLoaded(TACZ_MOD_ID)) {
            return;
        }
        
        // 检查配置文件是否已经存在
        File configFile = new File(TACZ_WEAPONS_FILE);
        if (configFile.exists()) {
            return; // 配置文件已存在，不再生成
        }
        
        Map<String, Object> tacZConfigs = new HashMap<>();
        
        // 获取所有TACZ枪械ID
        Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
        List<JsonObject> weaponsList = new ArrayList<>();
        
        for (ResourceLocation gunId : tacZGunIDs) {
            // 注意：这里不应该创建WeaponData对象
            // 这些对象应该由TacZConfigApplier类在需要时创建
            
            // 创建一个临时的WeaponData用于生成配置文件
            WeaponData tempData = new WeaponData();
            
            // 基本信息
            tempData.modid = "tacz";
            tempData.itemId = "modern_kinetic_gun";
            
            // TACZ特殊信息
            tempData.gunId = gunId.toString(); // 具体的枪械ID
            
            // 添加初始属性
            addInitialModifiers(tempData);
            
            JsonObject itemJson = createTacZWeaponConfigJson(tempData);
            // 添加gunId字段到配置中
            itemJson.addProperty("gunId", gunId.toString());
            weaponsList.add(itemJson);
        }
        
        // 使用统一的物品ID作为键名，值为武器配置数组
        tacZConfigs.put("tacz:modern_kinetic_gun", weaponsList);
        
        // 保存TACZ武器配置文件
        saveWeaponConfigToFile(tacZConfigs, TACZ_WEAPONS_FILE);
    }
    
    /**
     * 创建TACZ武器配置的JSON对象
     * 适配新的两层数据结构
     */
    private static JsonObject createTacZWeaponConfigJson(WeaponData weaponData) {
        JsonObject itemJson = new JsonObject();
        
        // 添加gunId（如果存在）
        if (weaponData.gunId != null && !weaponData.gunId.isEmpty()) {
            itemJson.addProperty("gunId", weaponData.gunId);
        }
        
        // 添加elementData部分
        JsonObject elementDataJson = new JsonObject();
        
        // 添加空的Basic层
        JsonArray basicArray = new JsonArray();
        elementDataJson.add("Basic", basicArray);
        
        // 添加空的Usage层
        JsonArray usageArray = new JsonArray();
        elementDataJson.add("Usage", usageArray);
        
        // 添加初始属性属性数据（只包含名称和数值，UUID在应用阶段生成）
        JsonArray modifiersArray = new JsonArray();
        for (InitialModifierEntry modifierEntry : weaponData.getInitialModifiers()) {
            JsonObject modifierJson = new JsonObject();
            modifierJson.addProperty("name", modifierEntry.getName());
            modifierJson.addProperty("amount", modifierEntry.getAmount());
            modifierJson.addProperty("operation", modifierEntry.getOperation());
            modifiersArray.add(modifierJson);
        }
        elementDataJson.add("InitialModifiers", modifiersArray);
        
        itemJson.add("elementData", elementDataJson);
        
        return itemJson;
    }
    
    /**
     * 保存配置到指定文件
     */
    private static void saveWeaponConfigToFile(Map<String, Object> configJson, String filePath) {
        try {
            // 使用Gson格式化输出
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // 确保Weapon目录存在
            Path weaponDir = Paths.get(WEAPON_DIR);
            if (!Files.exists(weaponDir)) {
                Files.createDirectories(weaponDir);
            }
            
            Path configPath = Paths.get(filePath);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                gson.toJson(configJson, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载TACZ武器配置（数组格式）
     */
    public static void loadTacZConfigFile() {
        File configFile = new File(TACZ_WEAPONS_FILE);
        if (!configFile.exists()) {
            return;
        }
        
        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject loadedConfigs = gson.fromJson(reader, JsonObject.class);
                
                // 注意：这里不应该创建WeaponData对象
                // 这些对象应该由TacZConfigApplier类在需要时创建
                
                if (loadedConfigs != null) {
                    for (Map.Entry<String, JsonElement> entry : loadedConfigs.entrySet()) {
                        String configKey = entry.getKey();
                        JsonElement configValue = entry.getValue();
                        
                        // 遍历数组中的每个配置
                        for (JsonElement arrayElement : configValue.getAsJsonArray()) {
                            JsonObject itemJson = arrayElement.getAsJsonObject();
                            // 注意：这里不应该处理配置
                            // 这些配置应该由TacZConfigApplier类在需要时处理
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取TACZ武器配置（根据具体的枪械ID）
     */
    public static WeaponData getWeaponConfig(ItemStack stack) {
        // 获取枪械的gunId - 使用ModSpecialItemsFetcher
        String gunId = com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher.getTacZGunId(stack);
        if (gunId != null) {
            return getWeaponConfigByGunId(gunId);
        }
        return null;
    }
    
    /**
     * 根据gunId获取武器配置（用于TACZ枪械）
     */
    public static WeaponData getWeaponConfigByGunId(String gunId) {
        // 首先检查缓存
        if (taczConfigCache.containsKey(gunId)) {
            return taczConfigCache.get(gunId);
        }
        
        // 在映射表中查找
        if (gunIdToConfigMap.containsKey(gunId)) {
            WeaponData data = gunIdToConfigMap.get(gunId);
            taczConfigCache.put(gunId, data); // 添加到缓存
            return data;
        }
        
        return null;
    }
    
    /**
     * 获取TACZ枪械配置
     */
    public static Map<String, List<WeaponData>> getTacZGunConfigs() {
        // 构建TACZ枪械配置映射
        Map<String, List<WeaponData>> tacZConfigs = new HashMap<>();
        
        // 遍历gunIdToConfigMap，将配置按照gunId分组
        for (Map.Entry<String, WeaponData> entry : gunIdToConfigMap.entrySet()) {
            String gunId = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 每个gunId对应一个WeaponData列表（尽管通常只有一个）
            tacZConfigs.computeIfAbsent(gunId, k -> new ArrayList<>()).add(weaponData);
        }
        
        return tacZConfigs;
    }
    
    /**
     * 缓存TACZ枪械配置到全局映射
     */
    public static void cacheTacZGunConfig(String gunId, WeaponData weaponData) {
        if (gunId != null && weaponData != null) {
            gunIdToConfigMap.put(gunId, weaponData);
        }
    }
    
    /**
     * 为武器添加初始属性
     */
    private static void addInitialModifiers(WeaponData data) {
        // 直接添加默认的初始属性，不依赖Basic层数据
        
        // 添加默认物理元素属性
        addDefaultModifier(data, ElementType.SLASH.getName(), DEFAULT_SLASH);
        addDefaultModifier(data, ElementType.IMPACT.getName(), DEFAULT_IMPACT);
        addDefaultModifier(data, ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        
        // 添加默认特殊属性属性
        addDefaultModifier(data, "critical_chance", DEFAULT_CRITICAL_CHANCE);
        addDefaultModifier(data, "critical_damage", DEFAULT_CRITICAL_DAMAGE);
        addDefaultModifier(data, "trigger_chance", DEFAULT_TRIGGER_CHANCE);
    }
    
    /**
     * 添加默认属性
     */
    private static void addDefaultModifier(WeaponData data, String elementType, double defaultValue) {
        // 为每种元素类型使用固定的UUID
        UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementType).getBytes());
        
        // 只有基础元素和复合元素才添加到Basic层
        ElementType type = ElementType.byName(elementType);
        if (type != null && (type.getTypeCategory() == ElementType.TypeCategory.BASIC || type.getTypeCategory() == ElementType.TypeCategory.COMPLEX)) {
            data.addBasicElement(elementType, "def", 0);
        }
        
        // 添加到初始属性列表
        data.addInitialModifier(new InitialModifierEntry(elementType, elementType, defaultValue, "ADDITION", modifierUuid, "def"));
    }
}