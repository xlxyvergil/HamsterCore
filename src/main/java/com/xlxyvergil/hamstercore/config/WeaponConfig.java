package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;

import java.io.File;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
 * 已适配新的两层NBT数据结构
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
    private static final String ADDITIONAL_NORMAL_WEAPONS_FILE = WEAPON_DIR + "additional_normal_weapons.json";
    
    // 武器配置映射表
    private static Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    // TACZ配置缓存
    private static Map<String, WeaponData> taczConfigCache = new HashMap<>();
    
    // 拔刀剑配置缓存
    private static Map<String, WeaponData> slashBladeConfigCache = new HashMap<>();
    
    // TACZ gunId到配置的映射表
    private static Map<String, WeaponData> gunIdToConfigMap = new HashMap<>();
    
    // 拔刀剑translationKey到配置的映射表
    private static Map<String, WeaponData> translationKeyToConfigMap = new HashMap<>();
    
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
        
        // 生成额外的配置文件
        createDefaultAdditionalNormalWeaponsConfig();
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
                // 添加gunId字段到配置中
                itemJson.addProperty("gunId", gunId.toString());
                weaponsList.add(itemJson);
                
                // 存储到内存映射表，使用gunId作为键以便后续查找
                weaponConfigs.put(gunId, weaponData);
                gunIdToConfigMap.put(gunId.toString(), weaponData);
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
                // 添加translationKey字段到配置中
                itemJson.addProperty("translationKey", translationKey);
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
        data.addBasicElement("critical_chance", "CONFIG", 0);
        data.addBasicElement("critical_damage", "CONFIG", 1);
        data.addBasicElement("trigger_chance", "CONFIG", 2);
        
        // 设置TACZ枪械默认元素占比
        setDefaultElementRatiosForTacZ(data, gunId);
        
        // 添加初始修饰符
        addInitialModifiers(data);
        
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
        data.addBasicElement("critical_chance", "CONFIG", 0);
        data.addBasicElement("critical_damage", "CONFIG", 1);
        data.addBasicElement("trigger_chance", "CONFIG", 2);
        
        // 设置拔刀剑默认元素占比
        setDefaultElementRatiosForSlashBlade(data, translationKey);
        
        // 添加初始修饰符
        addInitialModifiers(data);
        
        return data;
    }

    
    /**
     * 为TACZ枪械设置默认元素占比
     */
    private static void setDefaultElementRatiosForTacZ(WeaponData data, ResourceLocation gunId) {
        // 根据具体枪械类型设置不同的元素占比
        String gunType = gunId.getPath().toLowerCase();
        
        if (gunType.contains("pistol") || gunType.contains("handgun")) {
            // 手枪类：平衡型
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.5);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.3);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.SLASH.getName(), 0.2);
        } else if (gunType.contains("rifle") || gunType.contains("assault")) {
            // 步枪类：穿刺为主
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.7);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.2);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.SLASH.getName(), 0.1);
        } else if (gunType.contains("sniper")) {
            // 狙击枪：高穿刺
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.8);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.15);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.SLASH.getName(), 0.05);
        } else if (gunType.contains("shotgun")) {
            // 霰弹枪：冲击为主
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.6);
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.3);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.SLASH.getName(), 0.1);
        } else {
            // 默认枪械：穿刺和冲击为主
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.6);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.3);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.SLASH.getName(), 0.1);
        }
    }
    
    /**
     * 为拔刀剑设置默认元素占比（统一配置）
     */
    private static void setDefaultElementRatiosForSlashBlade(WeaponData data, String translationKey) {
        // 拔刀剑统一使用默认元素占比：切割70% 冲击20% 穿刺10%
        data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 0);
        data.setUsageElement(ElementType.SLASH.getName(), 0.7);
        data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
        data.setUsageElement(ElementType.IMPACT.getName(), 0.2);
        data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 2);
        data.setUsageElement(ElementType.PUNCTURE.getName(), 0.1);
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
        data.addBasicElement("critical_chance", "CONFIG", 0);
        data.addBasicElement("critical_damage", "CONFIG", 1);
        data.addBasicElement("trigger_chance", "CONFIG", 2);
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatiosForNormalItem(data, itemKey);
        
        // 添加初始修饰符
        addInitialModifiers(data);
        
        return data;
    }
    
    /**
     * 为普通物品设置默认元素占比
     */
    private static void setDefaultElementRatiosForNormalItem(WeaponData data, ResourceLocation itemKey) {
        // 根据物品名称判断类型并设置不同的默认元素占比
        if ("netherite_sword".equals(itemKey.getPath()) || 
            "diamond_sword".equals(itemKey.getPath()) ||
            "iron_sword".equals(itemKey.getPath()) ||
            "golden_sword".equals(itemKey.getPath()) ||
            "stone_sword".equals(itemKey.getPath()) ||
            "wooden_sword".equals(itemKey.getPath())) {
            // 剑类：主要是切割
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.SLASH.getName(), 0.6);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.2);
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.2);
        } else if ("netherite_axe".equals(itemKey.getPath()) ||
                   "diamond_axe".equals(itemKey.getPath()) ||
                   "iron_axe".equals(itemKey.getPath()) ||
                   "golden_axe".equals(itemKey.getPath()) ||
                   "stone_axe".equals(itemKey.getPath()) ||
                   "wooden_axe".equals(itemKey.getPath())) {
            // 斧类：主要是冲击和切割
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.IMPACT.getName(), 0.5);
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.SLASH.getName(), 0.4);
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.PUNCTURE.getName(), 0.1);
        } else {
            // 其他物品：默认物理元素占比
            data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 0);
            data.setUsageElement(ElementType.SLASH.getName(), DEFAULT_SLASH);
            data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
            data.setUsageElement(ElementType.IMPACT.getName(), DEFAULT_IMPACT);
            data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 2);
            data.setUsageElement(ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
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
            
            
        } catch (IOException e) {
        }
    }
    
    /**
     * 创建武器配置的JSON对象
     * 适配新的两层数据结构
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
        
        // 添加Basic层 - 记录元素名称、来源和添加顺序
        JsonArray basicArray = new JsonArray();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : weaponData.getBasicElements().entrySet()) {
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                JsonArray elementArray = new JsonArray();
                elementArray.add(basicEntry.getType());
                elementArray.add(basicEntry.getSource());
                elementArray.add(basicEntry.getOrder());
                basicArray.add(elementArray);
            }
        }
        elementDataJson.add("Basic", basicArray);
        
        // 添加Usage层 - 元素复合后的元素类型和数值
        JsonArray usageArray = new JsonArray();
        for (Map.Entry<String, Double> entry : weaponData.getUsageElements().entrySet()) {
            JsonArray elementArray = new JsonArray();
            elementArray.add(entry.getKey());
            elementArray.add(entry.getValue());
            usageArray.add(elementArray);
        }
        elementDataJson.add("Usage", usageArray);
        
        // 添加Def层 - 默认元素数据
        JsonArray defArray = new JsonArray();
        for (Map.Entry<String, Double> entry : weaponData.getDefElements().entrySet()) {
            JsonArray elementArray = new JsonArray();
            elementArray.add(entry.getKey());
            elementArray.add(entry.getValue());
            defArray.add(elementArray);
        }
        elementDataJson.add("Def", defArray);
        
        // 添加初始属性修饰符数据（只包含名称和数值，UUID在应用阶段生成）
        JsonArray modifiersArray = new JsonArray();
        for (WeaponData.AttributeModifierEntry modifierEntry : weaponData.getInitialModifiers()) {
            JsonObject modifierJson = new JsonObject();
            modifierJson.addProperty("name", modifierEntry.getName());
            modifierJson.addProperty("amount", modifierEntry.getModifier().getAmount());
            modifierJson.addProperty("operation", modifierEntry.getModifier().getOperation().toString());
            modifiersArray.add(modifierJson);
        }
        elementDataJson.add("InitialModifiers", modifiersArray);
        
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
            
            
        } catch (Exception e) {
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
            
            
        } catch (Exception e) {
        }
    }
    
    /**
     * 处理普通武器配置（数组格式）
     * 适配新的两层数据结构
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
                JsonArray basicArray = elementDataJson.getAsJsonArray("Basic");
                for (JsonElement element : basicArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    String source = elementArray.get(1).getAsString();
                    int order = elementArray.size() > 2 ? elementArray.get(2).getAsInt() : 0; // 顺序，默认为0
                    
                    // 直接添加，保持原有顺序
                    weaponData.getBasicElements().computeIfAbsent(type, k -> new ArrayList<>())
                        .add(new WeaponData.BasicEntry(type, source, order));
                }
            }
            
            // 读取Usage层
            if (elementDataJson.has("Usage")) {
                JsonArray usageArray = elementDataJson.getAsJsonArray("Usage");
                for (JsonElement element : usageArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    double value = elementArray.get(1).getAsDouble();
                    weaponData.setUsageElement(type, value);
                }
            }
            
            // 读取Def层
            if (elementDataJson.has("Def")) {
                JsonArray defArray = elementDataJson.getAsJsonArray("Def");
                for (JsonElement element : defArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    double value = elementArray.get(1).getAsDouble();
                    weaponData.setDefElement(type, value);
                }
            }
            
            // 读取初始属性修饰符数据
            if (elementDataJson.has("InitialModifiers")) {
                JsonArray modifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                for (JsonElement modifierElement : modifiersArray) {
                    JsonObject modifierJson = modifierElement.getAsJsonObject();
                    
                    String name = modifierJson.get("name").getAsString();
                    double amount = modifierJson.get("amount").getAsDouble();
                    String operationStr = modifierJson.get("operation").getAsString();
                    
                    // 解析操作类型
                    AttributeModifier.Operation operation;
                    switch (operationStr) {
                        case "ADDITION":
                            operation = AttributeModifier.Operation.ADDITION;
                            break;
                        case "MULTIPLY_BASE":
                            operation = AttributeModifier.Operation.MULTIPLY_BASE;
                            break;
                        case "MULTIPLY_TOTAL":
                            operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                            break;
                        default:
                            operation = AttributeModifier.Operation.ADDITION;
                    }
                    
                    // UUID将在应用阶段生成
                    UUID uuid = ElementUUIDManager.getElementUUID(name);
                    
                    // 创建修饰符
                    AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
                    weaponData.addInitialModifier(new WeaponData.AttributeModifierEntry(name, modifier));
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
     * 适配新的两层数据结构
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
                JsonArray basicArray = elementDataJson.getAsJsonArray("Basic");
                for (JsonElement element : basicArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    String source = elementArray.get(1).getAsString();
                    int order = elementArray.size() > 2 ? elementArray.get(2).getAsInt() : 0; // 顺序，默认为0
                    
                    // 直接添加，保持原有顺序
                    weaponData.getBasicElements().computeIfAbsent(type, k -> new ArrayList<>())
                        .add(new WeaponData.BasicEntry(type, source, order));
                }
            }
            
            // 读取Usage层
            if (elementDataJson.has("Usage")) {
                JsonArray usageArray = elementDataJson.getAsJsonArray("Usage");
                for (JsonElement element : usageArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    double value = elementArray.get(1).getAsDouble();
                    weaponData.setUsageElement(type, value);
                }
            }
            
            // 读取Def层
            if (elementDataJson.has("Def")) {
                JsonArray defArray = elementDataJson.getAsJsonArray("Def");
                for (JsonElement element : defArray) {
                    JsonArray elementArray = element.getAsJsonArray();
                    String type = elementArray.get(0).getAsString();
                    double value = elementArray.get(1).getAsDouble();
                    weaponData.setDefElement(type, value);
                }
            }
            
            // 读取初始属性修饰符数据
            if (elementDataJson.has("InitialModifiers")) {
                JsonArray modifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                for (JsonElement modifierElement : modifiersArray) {
                    JsonObject modifierJson = modifierElement.getAsJsonObject();
                    
                    String name = modifierJson.get("name").getAsString();
                    double amount = modifierJson.get("amount").getAsDouble();
                    String operationStr = modifierJson.get("operation").getAsString();
                    
                    // 解析操作类型
                    AttributeModifier.Operation operation;
                    switch (operationStr) {
                        case "ADDITION":
                            operation = AttributeModifier.Operation.ADDITION;
                            break;
                        case "MULTIPLY_BASE":
                            operation = AttributeModifier.Operation.MULTIPLY_BASE;
                            break;
                        case "MULTIPLY_TOTAL":
                            operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                            break;
                        default:
                            operation = AttributeModifier.Operation.ADDITION;
                    }
                    
                    // UUID将在应用阶段生成
                    UUID uuid = ElementUUIDManager.getElementUUID(name);
                    
                    // 创建修饰符
                    AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
                    weaponData.addInitialModifier(new WeaponData.AttributeModifierEntry(name, modifier));
                }
            }
        }
        
        // 根据配置类型决定内存映射的键名
        ResourceLocation itemKey = null;
        if (isTacZ && weaponData.gunId != null) {
            // TACZ武器使用gunId作为键
            itemKey = ResourceLocation.tryParse(weaponData.gunId);
        } else if (!isTacZ && weaponData.translationKey != null) {
            // 拔刀剑使用统一的slashblade:slashblade作为键
            itemKey = new ResourceLocation("slashblade", "slashblade");
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
     * 获取额外武器配置（与getAllWeaponConfigs相同）
     */
    public static Map<ResourceLocation, WeaponData> getAdditionalWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
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
        
        // 回退到原来的查找方式
        ResourceLocation gunIdKey = ResourceLocation.tryParse(gunId);
        if (gunIdKey != null) {
            WeaponData data = weaponConfigs.get(gunIdKey);
            if (data != null) {
                taczConfigCache.put(gunId, data); // 添加到缓存
                gunIdToConfigMap.put(gunId, data); // 添加到映射表
                return data;
            }
        }
        
        return null;
    }
    
    /**
     * 根据translationKey获取武器配置（用于拔刀剑）
     */
    public static WeaponData getWeaponConfigByTranslationKey(String translationKey) {
        // 首先检查缓存
        if (slashBladeConfigCache.containsKey(translationKey)) {
            return slashBladeConfigCache.get(translationKey);
        }
        
        // 在映射表中查找
        if (translationKeyToConfigMap.containsKey(translationKey)) {
            WeaponData data = translationKeyToConfigMap.get(translationKey);
            slashBladeConfigCache.put(translationKey, data); // 添加到缓存
            return data;
        }
        
        // 回退到原来的查找方式
        // 遍历所有配置，查找匹配的translationKey
        for (WeaponData weaponData : weaponConfigs.values()) {
            if (translationKey.equals(weaponData.translationKey)) {
                slashBladeConfigCache.put(translationKey, weaponData); // 添加到缓存
                translationKeyToConfigMap.put(translationKey, weaponData); // 添加到映射表
                return weaponData;
            }
        }
        return null;
    }
    
    /**
     * 获取TACZ武器配置（根据具体的枪械ID）
     */
    public static WeaponData getTacZWeaponConfig(ItemStack stack) {
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
    public static WeaponData getSlashBladeWeaponConfig(ItemStack stack) {
        // 获取拔刀剑的translationKey
        String translationKey = ModCompat.getSlashBladeTranslationKey(stack);
        if (translationKey != null) {
            // 查找匹配的translationKey配置
            return getWeaponConfigByTranslationKey(translationKey);
        }
        // 如果没找到具体配置，返回统一的slashblade:slashblade配置
        return weaponConfigs.get(new ResourceLocation("slashblade", "slashblade"));
    }
    
    /**
     * 为武器添加初始修饰符
     */
    private static void addInitialModifiers(WeaponData data) {
        // 为每个基础元素添加初始修饰符
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : data.getBasicElements().entrySet()) {
            String elementType = entry.getKey();
            double defaultValue = 1.0; // 默认值
            
            // 根据元素类型设置默认值
            if ("critical_chance".equals(elementType)) {
                defaultValue = DEFAULT_CRITICAL_CHANCE;
            } else if ("critical_damage".equals(elementType)) {
                defaultValue = DEFAULT_CRITICAL_DAMAGE;
            } else if ("trigger_chance".equals(elementType)) {
                defaultValue = DEFAULT_TRIGGER_CHANCE;
            }
            // 注意：派系增伤和特殊属性不应该从Usage层获取默认值，因为Usage层不存储这些数据
            
            // 为每种元素类型使用固定的UUID
            UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementType).getBytes());
            
            // 创建属性修饰符
            AttributeModifier modifier = new AttributeModifier(
                modifierUuid, 
                elementType, 
                defaultValue, 
                AttributeModifier.Operation.ADDITION
            );
            
            // 添加到初始修饰符列表
            data.addInitialModifier(new WeaponData.AttributeModifierEntry(elementType, modifier));
        }
        
        // 为Def层元素也添加初始修饰符
        for (Map.Entry<String, Double> entry : data.getDefElements().entrySet()) {
            String elementType = entry.getKey();
            double defaultValue = entry.getValue(); // 使用Def层的值作为默认值
            
            // 检查是否已经在Basic层中添加过了
            if (!data.getBasicElements().containsKey(elementType)) {
                // 为每种元素类型使用固定的UUID
                UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementType).getBytes());
                
                // 创建属性修饰符
                AttributeModifier modifier = new AttributeModifier(
                    modifierUuid, 
                    elementType, 
                    defaultValue, 
                    AttributeModifier.Operation.ADDITION
                );
                
                // 添加到初始修饰符列表
                data.addInitialModifier(new WeaponData.AttributeModifierEntry(elementType, modifier));
            }
        }
    }

    /**
     * 创建默认的额外普通武器配置文件
     */
    private static void createDefaultAdditionalNormalWeaponsConfig() {
        try {
            // 确保目录存在
            Path weaponDir = Paths.get(WEAPON_DIR);
            if (!Files.exists(weaponDir)) {
                Files.createDirectories(weaponDir);
            }
            
            File configFile = new File(ADDITIONAL_NORMAL_WEAPONS_FILE);
            if (configFile.exists()) {
                return; // 文件已存在，不需要重新创建
            }
            
            // 创建默认配置内容（只包含注释和示例）
            JsonObject defaultConfig = new JsonObject();
            defaultConfig.addProperty("_comment", "在此添加您想要应用元素属性的额外普通物品，格式如下:");
            defaultConfig.addProperty("_example_full", "{\n"
                    + "  \"minecraft:diamond_sword\": {\n"
                    + "    \"elementData\": {\n"
                    + "      \"Basic\": [\n"
                    + "        [\"SLASH\", \"CONFIG\", 0],\n"
                    + "        [\"CRITICAL_CHANCE\", \"CONFIG\", 1]\n"
                    + "      ],\n"
                    + "      \"Usage\": [\n"
                    + "        [\"SLASH\", 5.0],\n"
                    + "        [\"CRITICAL_CHANCE\", 0.1]\n"
                    + "      ],\n"
                    + "      \"Def\": [\n"
                    + "        [\"SLASH\", 3.0],\n"
                    + "        [\"IMPACT\", 2.0]\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
            
            // 保存文件
            saveWeaponConfigToFile(Collections.singletonMap("_comment", defaultConfig), ADDITIONAL_NORMAL_WEAPONS_FILE);
            
        } catch (Exception e) {
        }
    }
}
