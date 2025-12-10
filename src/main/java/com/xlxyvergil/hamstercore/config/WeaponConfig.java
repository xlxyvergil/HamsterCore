package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 武器配置管理器
 * 负责加载、保存和管理所有武器的元素配置
 */
public class WeaponConfig {
    
    // 默认特殊属性值
    private static final double DEFAULT_CRITICAL_CHANCE = 0.05; // 5%暴击率
    private static final double DEFAULT_CRITICAL_DAMAGE = 1.5;  // 1.5倍暴击伤害
    private static final double DEFAULT_TRIGGER_CHANCE = 0.1;   // 10%触发率
    
    // 默认物理元素占比
    private static final double DEFAULT_SLASH = 0.33;
    private static final double DEFAULT_IMPACT = 0.33;
    private static final double DEFAULT_PUNCTURE = 0.34;
    
    // 配置文件路径
    private static final String CONFIG_DIR = "config/hamstercore/";
    private static final String WEAPON_DIR = CONFIG_DIR + "Weapon/";
    private static final String NORMAL_WEAPONS_FILE = WEAPON_DIR + "normal_weapons.json";
    private static final String TACZ_WEAPONS_FILE = WEAPON_DIR + "tacz_weapons.json";
    private static final String SLASHBLADE_WEAPONS_FILE = WEAPON_DIR + "slashblade_weapons.json";
    
    // 武器配置映射表
    private static Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    /**
     * 加载武器配置
     * 如果配置文件不存在，则生成默认配置
     */
    public static void load() {
        // 检查配置文件是否存在，如果不存在则生成默认配置
        File normalFile = new File(NORMAL_WEAPONS_FILE);
        File tacZFile = new File(TACZ_WEAPONS_FILE);
        File slashBladeFile = new File(SLASHBLADE_WEAPONS_FILE);
        
        if (!normalFile.exists() || !tacZFile.exists() || !slashBladeFile.exists()) {
            // 配置文件不存在，生成默认配置
            generateDefaultConfigs();
        } else {
            // 配置文件存在，加载配置
            loadWeaponConfigs();
        }
    }
    
    /**
     * 生成默认武器配置
     */
    private static void generateDefaultConfigs() {
        weaponConfigs.clear();
        
        // 生成三种类型的配置文件
        generateNormalWeaponsConfig();
        generateTacZWeaponsConfig();
        generateSlashBladeWeaponsConfig();
        
    }
    
    /**
     * 生成普通武器配置文件
     */
    private static void generateNormalWeaponsConfig() {
        Map<String, Object> normalConfigs = new HashMap<>();
        
        // 查找所有可应用元素属性的普通物品
        Set<ResourceLocation> applicableItems = com.xlxyvergil.hamstercore.util.WeaponApplicableItemsFinder.findApplicableItems();
        
        // 过滤掉MOD特殊物品
        Set<ResourceLocation> modSpecialItems = new HashSet<>();
        modSpecialItems.addAll(ModSpecialItemsFetcher.getTacZGunIDs());
        modSpecialItems.addAll(SlashBladeItemsFetcher.getSlashBladeIDs());
        
        // 为每个普通物品创建配置
        for (ResourceLocation itemKey : applicableItems) {
            if (!modSpecialItems.contains(itemKey)) {
                WeaponData weaponData = createNormalWeaponData(itemKey);
                if (weaponData != null) {
                    JsonObject itemJson = createWeaponConfigJson(weaponData);
                    normalConfigs.put(itemKey.toString(), itemJson);
                    
                    // 同时存储到内存映射表
                    weaponConfigs.put(itemKey, weaponData);
                }
            }
        }
        
        // 保存普通武器配置文件
        saveWeaponConfigToFile(normalConfigs, NORMAL_WEAPONS_FILE);
    }
    
    /**
     * 生成TACZ武器配置文件
     */
    private static void generateTacZWeaponsConfig() {
        Map<String, Object> tacZConfigs = new HashMap<>();
        
        // 获取所有TACZ枪械ID
        Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
        List<JsonObject> weaponsList = new ArrayList<>();
        
        for (ResourceLocation gunId : tacZGunIDs) {
            WeaponData weaponData = createTacZWeaponData(gunId);
            if (weaponData != null) {
                JsonObject itemJson = createWeaponConfigJson(weaponData);
                weaponsList.add(itemJson);
                
                // 存储到内存映射表，使用gunId作为键以便后续查找
                weaponConfigs.put(gunId, weaponData);
            }
        }
        
        // 使用统一的物品ID作为键名，值为武器配置数组
        tacZConfigs.put("tacz:modern_kinetic_gun", weaponsList);
        
        // 保存TACZ武器配置文件
        saveWeaponConfigToFile(tacZConfigs, TACZ_WEAPONS_FILE);
    }
    
    /**
     * 生成拔刀剑武器配置文件
     */
    private static void generateSlashBladeWeaponsConfig() {
        Map<String, Object> slashBladeConfigs = new HashMap<>();
        
        // 获取所有拔刀剑translationKey
        Set<String> translationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys();
        List<JsonObject> weaponsList = new ArrayList<>();
        
        for (String translationKey : translationKeys) {
            WeaponData weaponData = createSlashBladeWeaponData(translationKey);
            if (weaponData != null) {
                JsonObject itemJson = createWeaponConfigJson(weaponData);
                weaponsList.add(itemJson);
            }
        }
        
        // 使用统一的物品ID作为键名，值为武器配置数组
        slashBladeConfigs.put("slashblade:slashblade", weaponsList);
        
        // 保存拔刀剑武器配置文件
        saveWeaponConfigToFile(slashBladeConfigs, SLASHBLADE_WEAPONS_FILE);
    }
    
    /**
     * 为TACZ枪械创建武器配置数据
     */
    private static WeaponData createTacZWeaponData(ResourceLocation gunId) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        data.modid = "tacz";
        data.itemId = "modern_kinetic_gun";
        
        // TACZ特殊信息
        data.gunId = gunId.toString(); // 具体的枪械ID
        
        // 添加默认特殊属性
        data.elementData.addBasicElement("critical_chance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("critical_damage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("trigger_chance", DEFAULT_TRIGGER_CHANCE);
        
        // 设置TACZ枪械默认元素占比
        setDefaultElementRatiosForTacZ(data.elementData, gunId);
        
        return data;
    }
    
    /**
     * 为拔刀剑创建武器配置数据
     */
    private static WeaponData createSlashBladeWeaponData(String translationKey) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        data.modid = "slashblade";
        data.itemId = "slashblade";
        
        // 拔刀剑特殊信息
        data.translationKey = translationKey; // 具体的translationKey
        
        // 添加默认特殊属性
        data.elementData.addBasicElement("critical_chance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("critical_damage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("trigger_chance", DEFAULT_TRIGGER_CHANCE);
        
        // 设置拔刀剑默认元素占比
        setDefaultElementRatiosForSlashBlade(data.elementData, translationKey);
        
        return data;
    }
    

    
    /**
     * 为TACZ枪械设置默认元素占比
     */
    private static void setDefaultElementRatiosForTacZ(WeaponElementData elementData, ResourceLocation gunId) {
        // 根据具体枪械类型设置不同的元素占比
        String gunType = gunId.getPath().toLowerCase();
        
        if (gunType.contains("pistol") || gunType.contains("handgun")) {
            // 手枪类：平衡型
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.5);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.3);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.2);
        } else if (gunType.contains("rifle") || gunType.contains("assault")) {
            // 步枪类：穿刺为主
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.7);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        } else if (gunType.contains("sniper")) {
            // 狙击枪：高穿刺
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.8);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.15);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.05);
        } else if (gunType.contains("shotgun")) {
            // 霰弹枪：冲击为主
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.6);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.3);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        } else {
            // 默认枪械：穿刺和冲击为主
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.6);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.3);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        }
    }
    
    /**
     * 为拔刀剑设置默认元素占比（统一配置）
     */
    private static void setDefaultElementRatiosForSlashBlade(WeaponElementData elementData, String translationKey) {
        // 拔刀剑统一使用默认元素占比：切割70% 冲击20% 穿刺10%
        elementData.addBasicElement(ElementType.SLASH.getName(), 0.7);
        elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
        elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.1);
    }
    

    
    /**
     * 为普通物品创建武器配置数据
     */
    private static WeaponData createNormalWeaponData(ResourceLocation itemKey) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        if (itemKey != null) {
            data.modid = itemKey.getNamespace();
            data.itemId = itemKey.getPath();
        }
        
        // 添加默认特殊属性
        data.elementData.addBasicElement("critical_chance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("critical_damage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("trigger_chance", DEFAULT_TRIGGER_CHANCE);
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatiosForNormalItem(data.elementData, itemKey);
        
        return data;
    }
    
    /**
     * 为普通物品设置默认元素占比
     */
    private static void setDefaultElementRatiosForNormalItem(WeaponElementData elementData, ResourceLocation itemKey) {
        // 根据物品名称判断类型并设置不同的默认元素占比
        if ("netherite_sword".equals(itemKey.getPath()) || 
            "diamond_sword".equals(itemKey.getPath()) ||
            "iron_sword".equals(itemKey.getPath()) ||
            "golden_sword".equals(itemKey.getPath()) ||
            "stone_sword".equals(itemKey.getPath()) ||
            "wooden_sword".equals(itemKey.getPath())) {
            // 剑类：主要是切割
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.6);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.2);
        } else if ("netherite_axe".equals(itemKey.getPath()) ||
                   "diamond_axe".equals(itemKey.getPath()) ||
                   "iron_axe".equals(itemKey.getPath()) ||
                   "golden_axe".equals(itemKey.getPath()) ||
                   "stone_axe".equals(itemKey.getPath()) ||
                   "wooden_axe".equals(itemKey.getPath())) {
            // 斧类：主要是冲击和切割
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.5);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.4);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.1);
        } else {
            // 其他物品：默认物理元素占比
            elementData.addBasicElement(ElementType.SLASH.getName(), DEFAULT_SLASH);
            elementData.addBasicElement(ElementType.IMPACT.getName(), DEFAULT_IMPACT);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        }
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
            
            DebugLogger.log("配置已保存到: %s, 共 %d 个配置项", filePath, configJson.size());
            
        } catch (IOException e) {
            DebugLogger.log("保存配置文件失败: %s, 错误: %s", filePath, e.getMessage());
        }
    }
    
    /**
     * 创建武器配置的JSON对象
     */
    private static JsonObject createWeaponConfigJson(WeaponData weaponData) {
        JsonObject itemJson = new JsonObject();
        
        // 添加gunId或translationKey（如果存在）
        if (weaponData.gunId != null && !weaponData.gunId.isEmpty()) {
            itemJson.addProperty("gunId", weaponData.gunId);
        }
        
        if (weaponData.translationKey != null && !weaponData.translationKey.isEmpty()) {
            itemJson.addProperty("translationKey", weaponData.translationKey);
        }
        
        // 添加elementData部分
        JsonObject elementDataJson = new JsonObject();
        
        // 添加Basic层
        JsonObject basicJson = new JsonObject();
        for (Map.Entry<String, BasicEntry> basicEntry : weaponData.getElementData().getAllBasicElements().entrySet()) {
            String elementType = basicEntry.getKey();
            BasicEntry elementValue = basicEntry.getValue();
            
            JsonObject elementJson = new JsonObject();
            elementJson.addProperty("type", elementValue.getType());
            elementJson.addProperty("value", elementValue.getValue());
            elementJson.addProperty("source", elementValue.getSource());
            
            basicJson.add(elementType, elementJson);
        }
        elementDataJson.add("Basic", basicJson);
        
        // 添加其他层（空的）
        elementDataJson.add("Computed", new JsonObject());
        elementDataJson.add("Usage", new JsonObject());
        elementDataJson.add("Extra", new JsonObject());
        
        itemJson.add("elementData", elementDataJson);
        
        return itemJson;
    }
    

    
    /**
     * 从三个配置文件加载武器配置
     */
    private static void loadWeaponConfigs() {
        weaponConfigs.clear();
        
        try {
            // 加载普通武器配置（使用对象格式）
            loadNormalWeaponsConfig();
            
            // 加载TACZ武器配置（使用数组格式）
            loadConfigFile(TACZ_WEAPONS_FILE, true);
            
            // 加载拔刀剑配置（使用数组格式）
            loadConfigFile(SLASHBLADE_WEAPONS_FILE, false);
            
        } catch (Exception e) {
            DebugLogger.log("加载武器配置失败: %s", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载普通武器配置文件（对象格式）
     */
    private static void loadNormalWeaponsConfig() {
        File configFile = new File(NORMAL_WEAPONS_FILE);
        if (!configFile.exists()) {
            return;
        }
        
        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject loadedConfigs = gson.fromJson(reader, JsonObject.class);
                
                if (loadedConfigs != null) {
                    for (Map.Entry<String, JsonElement> entry : loadedConfigs.entrySet()) {
                        String itemKey = entry.getKey();
                        JsonObject itemJson = entry.getValue().getAsJsonObject();
                        
                        // 直接处理对象格式的配置
                        processNormalWeaponConfig(itemJson, itemKey);
                    }
                }
            }
            
            DebugLogger.log("已加载配置文件: %s", NORMAL_WEAPONS_FILE);
            
        } catch (Exception e) {
            DebugLogger.log("加载配置文件失败: %s, 错误: %s", NORMAL_WEAPONS_FILE, e.getMessage());
        }
    }
    
    /**
     * 加载单个配置文件（数组格式，用于TACZ和拔刀剑）
     */
    private static void loadConfigFile(String filePath, boolean isTacZ) {
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            return;
        }
        
        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject loadedConfigs = gson.fromJson(reader, JsonObject.class);
                
                if (loadedConfigs != null) {
                    for (Map.Entry<String, JsonElement> entry : loadedConfigs.entrySet()) {
                        String configKey = entry.getKey();
                        JsonElement configValue = entry.getValue();
                        
                        // 遍历数组中的每个配置
                        for (JsonElement arrayElement : configValue.getAsJsonArray()) {
                            JsonObject itemJson = arrayElement.getAsJsonObject();
                            processWeaponConfig(itemJson, configKey, isTacZ);
                        }
                    }
                }
            }
            
            DebugLogger.log("已加载配置文件: %s", filePath);
            
        } catch (Exception e) {
            DebugLogger.log("加载配置文件失败: %s, 错误: %s", filePath, e.getMessage());
        }
    }
    
    /**
     * 处理普通武器配置（对象格式）
     */
    private static void processNormalWeaponConfig(JsonObject itemJson, String itemKey) {
        // 创建WeaponData对象
        WeaponData weaponData = new WeaponData();
        
        // 解析物品ID
        ResourceLocation itemResourceKey = ResourceLocation.tryParse(itemKey);
        if (itemResourceKey != null) {
            weaponData.modid = itemResourceKey.getNamespace();
            weaponData.itemId = itemResourceKey.getPath();
        }
        
        // 读取elementData
        if (itemJson.has("elementData")) {
            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
            
            // 读取Basic层
            if (elementDataJson.has("Basic")) {
                JsonObject basicJson = elementDataJson.getAsJsonObject("Basic");
                for (Map.Entry<String, JsonElement> basicEntry : basicJson.entrySet()) {
                    JsonObject elementJson = basicEntry.getValue().getAsJsonObject();
                    
                    String type = elementJson.get("type").getAsString();
                    double value = elementJson.get("value").getAsDouble();
                    String source = elementJson.get("source").getAsString();
                    
                    weaponData.getElementData().addBasicElement(type, value, source);
                }
            }
        }
        
        // 使用物品ID作为键
        if (itemResourceKey != null) {
            weaponConfigs.put(itemResourceKey, weaponData);
        }
    }
    
    /**
     * 处理单个武器配置（数组格式，用于TACZ和拔刀剑）
     */
    private static void processWeaponConfig(JsonObject itemJson, String configKey, boolean isTacZ) {
        // 创建WeaponData对象
        WeaponData weaponData = new WeaponData();
        
        // 读取gunId和translationKey（如果存在）
        if (itemJson.has("gunId")) {
            weaponData.gunId = itemJson.get("gunId").getAsString();
        }
        
        if (itemJson.has("translationKey")) {
            weaponData.translationKey = itemJson.get("translationKey").getAsString();
        }
        
        // 读取elementData
        if (itemJson.has("elementData")) {
            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
            
            // 读取Basic层
            if (elementDataJson.has("Basic")) {
                JsonObject basicJson = elementDataJson.getAsJsonObject("Basic");
                for (Map.Entry<String, JsonElement> basicEntry : basicJson.entrySet()) {
                    JsonObject elementJson = basicEntry.getValue().getAsJsonObject();
                    
                    String type = elementJson.get("type").getAsString();
                    double value = elementJson.get("value").getAsDouble();
                    String source = elementJson.get("source").getAsString();
                    
                    weaponData.getElementData().addBasicElement(type, value, source);
                }
            }
        }
        
        // 根据配置类型决定内存映射的键名
        ResourceLocation itemKey;
        if (isTacZ && weaponData.gunId != null) {
            // TACZ武器使用gunId作为键
            itemKey = ResourceLocation.tryParse(weaponData.gunId);
        } else if (configKey.equals("slashblade:slashblade")) {
            // 拔刀剑使用统一的slashblade:slashblade作为键
            itemKey = new ResourceLocation("slashblade", "slashblade");
        } else {
            // 其他使用configKey
            itemKey = ResourceLocation.tryParse(configKey);
        }
        
        if (itemKey != null) {
            weaponConfigs.put(itemKey, weaponData);
        }
    }
    
    /**
     * 获取物品的武器配置
     */
    public static WeaponData getWeaponConfig(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) return null;
        
        // 对于TACZ和拔刀剑，使用统一的物品ID
        if (itemKey.toString().equals("tacz:modern_kinetic_gun")) {
            return getTacZWeaponConfig(stack);
        } else if (itemKey.toString().equals("slashblade:slashblade")) {
            return getSlashBladeWeaponConfig(stack);
        }
        
        return weaponConfigs.get(itemKey);
    }
    
    /**
     * 检查物品是否有武器配置
     */
    public static boolean hasWeaponConfig(ItemStack stack) {
        return getWeaponConfig(stack) != null;
    }
    
    /**
     * 获取所有武器配置
     */
    public static Map<ResourceLocation, WeaponData> getAllWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
    }
    
    /**
     * 根据gunId获取武器配置（用于TACZ枪械）
     */
    public static WeaponData getWeaponConfigByGunId(String gunId) {
        ResourceLocation gunIdKey = ResourceLocation.tryParse(gunId);
        if (gunIdKey != null) {
            return weaponConfigs.get(gunIdKey);
        }
        return null;
    }
    
    /**
     * 根据translationKey获取武器配置（用于拔刀剑）
     */
    public static WeaponData getWeaponConfigByTranslationKey(String translationKey) {
        // 遍历所有配置，查找匹配的translationKey
        for (WeaponData weaponData : weaponConfigs.values()) {
            if (translationKey.equals(weaponData.translationKey)) {
                return weaponData;
            }
        }
        return null;
    }
    
    /**
     * 获取TACZ武器配置（根据具体的枪械ID）
     */
    private static WeaponData getTacZWeaponConfig(ItemStack stack) {
        // 获取枪械的gunId
        String gunId = ModCompat.getGunId(stack);
        if (gunId != null) {
            return getWeaponConfigByGunId(gunId);
        }
        return null;
    }
    
    /**
     * 获取拔刀剑武器配置（根据具体的刀）
     */
    private static WeaponData getSlashBladeWeaponConfig(ItemStack stack) {
        // 获取拔刀剑的translationKey
        String translationKey = ModCompat.getSlashBladeTranslationKey(stack);
        if (translationKey != null) {
            // 查找匹配的translationKey配置
            for (WeaponData weaponData : weaponConfigs.values()) {
                if (translationKey.equals(weaponData.translationKey)) {
                    return weaponData;
                }
            }
        }
        // 如果没找到具体配置，返回统一的slashblade:slashblade配置
        return weaponConfigs.get(new ResourceLocation("slashblade", "slashblade"));
    }
}
