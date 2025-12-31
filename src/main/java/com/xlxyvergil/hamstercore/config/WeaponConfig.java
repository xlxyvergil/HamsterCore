package com.xlxyvergil.hamstercore.config;

import com.google.gson.*;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsFinder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 武器配置管理器
 * 负责加载、保存和管理普通武器的元素配置
 * 生成的配置文件允许用户自由修改
 */
public class WeaponConfig {
    
    // 默认特殊属性值 - 与ApothicAttributes的默认值保持一致
    private static final double DEFAULT_CRITICAL_CHANCE = 0.2; // 20%暴击率 (与ApothicAttributes默认值一致)
    private static final double DEFAULT_CRITICAL_DAMAGE = 0.5;  // 0.5倍暴击伤害 (与ApothicAttributes默认值一致)
    private static final double DEFAULT_TRIGGER_CHANCE = 0.2;   // 20%触发率
    
    // 默认物理元素占比
    private static final double DEFAULT_SLASH = 0.3;
    private static final double DEFAULT_IMPACT = 0.3;
    private static final double DEFAULT_PUNCTURE = 0.4;
    
    // 配置文件路径 - 与TacZWeaponConfig保持一致
    private static final String CONFIG_DIR = "config/hamstercore/";
    private static final String WEAPON_DIR = CONFIG_DIR + "Weapon/";
    public static final String DEFAULT_WEAPONS_FILE = WEAPON_DIR + "default_weapons.json";
    public static final String ADDITIONAL_NORMAL_WEAPONS_FILE = WEAPON_DIR + "additional_normal_weapons.json";
    
    // 武器配置映射
    private static final Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    // 配置好的物品堆映射
    private static final Map<ResourceLocation, ItemStack> configuredItemStacks = new HashMap<>();
    
    /**
     * 初始化武器配置系统
     */
    public static void init() {
        // 检查配置文件是否存在，如果不存在则生成默认配置
        File defaultFile = new File(DEFAULT_WEAPONS_FILE);
        File additionalFile = new File(ADDITIONAL_NORMAL_WEAPONS_FILE);
        
        if (!defaultFile.exists() || !additionalFile.exists()) {
            // 配置文件不存在，生成默认配置
            generateDefaultConfigs();
        } else {
            // 配置文件存在，加载配置
            loadWeaponConfigs();
        }
    }
    
    /**
     * 获取所有武器配置
     */
    public static Map<ResourceLocation, WeaponData> getAllWeaponConfigs() {
        return Collections.unmodifiableMap(weaponConfigs);
    }
    
    /**
     * 获取指定物品的武器配置
     */
    public static WeaponData getWeaponConfig(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) return null;
        
        return weaponConfigs.get(itemKey);
    }
    
    /**
     * 检查物品是否有武器配置
     */
    public static boolean hasWeaponConfig(ItemStack stack) {
        return getWeaponConfig(stack) != null;
    }
    
    /**
     * 获取额外武器配置（与getAllWeaponConfigs不同，这个只返回额外配置）
     */
    public static Map<ResourceLocation, WeaponData> getAdditionalWeaponConfigs() {
        // 查找在additional_normal_weapons.json中定义的额外配置
        Map<ResourceLocation, WeaponData> additionalConfigs = new HashMap<>();
        
        try {
            File configFile = new File(ADDITIONAL_NORMAL_WEAPONS_FILE);
            if (configFile.exists()) {
                Gson gson = new Gson();
                try (FileReader reader = new FileReader(configFile)) {
                    JsonObject config = gson.fromJson(reader, JsonObject.class);
                    
                    // 遍历所有物品配置
                    for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
                        String itemName = entry.getKey();
                        
                        // 跳过注释和示例
                        if (itemName.startsWith("_")) {
                            continue;
                        }
                        
                        ResourceLocation itemKey = ResourceLocation.tryParse(itemName);
                        if (itemKey == null) {
                            continue;
                        }
                        
                        // 只添加在额外配置文件中定义的配置
                        if (weaponConfigs.containsKey(itemKey)) {
                            additionalConfigs.put(itemKey, weaponConfigs.get(itemKey));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return additionalConfigs;
    }
    
    /**
     * 缓存额外武器配置到全局映射中
     */
    public static void cacheAdditionalWeaponConfig(ResourceLocation itemKey, WeaponData weaponData) {
        if (itemKey != null && weaponData != null) {
            weaponConfigs.put(itemKey, weaponData);
        }
    }
    
    /**
     * 缓存武器配置到全局映射中
     */
    public static void cacheWeaponConfig(ResourceLocation itemKey, WeaponData weaponData) {
        if (itemKey != null && weaponData != null) {
            weaponConfigs.put(itemKey, weaponData);
        }
    }
    
    /**
     * 缓存配置好的物品堆到全局映射中
     */
    public static void cacheConfiguredItemStack(ResourceLocation itemKey, ItemStack stack) {
        if (itemKey != null && stack != null) {
            configuredItemStacks.put(itemKey, stack);
        }
    }
    
    /**
     * 为武器添加初始属性
     */
    public static void addInitialModifiers(WeaponData data) {
        // 直接添加默认的初始属性，不依赖Basic层数据
        
        // 添加默认物理元素初始属性
        addDefaultModifier(data, ElementType.SLASH.getName(), DEFAULT_SLASH);
        addDefaultModifier(data, ElementType.IMPACT.getName(), DEFAULT_IMPACT);
        addDefaultModifier(data, ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        
        // 添加默认特殊属性初始属性
        addDefaultModifier(data, "critical_chance", DEFAULT_CRITICAL_CHANCE);
        addDefaultModifier(data, "critical_damage", DEFAULT_CRITICAL_DAMAGE);
        addDefaultModifier(data, "trigger_chance", DEFAULT_TRIGGER_CHANCE);
    }
    
    /**
     * 添加默认属性
     */
    public static void addDefaultModifier(WeaponData data, String elementType, double defaultValue) {
        // 确保elementType包含命名空间，name保持原始名称
        String namespacedElementType = elementType.contains(":") ? elementType : "hamstercore:" + elementType;
        
        // 为每种元素类型使用固定的UUID
        UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementType).getBytes());
        
        // 只有基础元素和复合元素才添加到Basic层
        ElementType type = ElementType.byName(elementType);
        if (type != null && (type.getTypeCategory() == ElementType.TypeCategory.BASIC || type.getTypeCategory() == ElementType.TypeCategory.COMPLEX)) {
            data.addBasicElement(elementType, "def", 0);
        }
        
        // 添加到初始属性列表：name是原始名称，elementType是带命名空间的完整名称
        data.addInitialModifier(new InitialModifierEntry(elementType, namespacedElementType, defaultValue, "ADDITION", modifierUuid, "def"));
    }
    
    /**
     * 创建武器配置的JSON对象
     */
    private static JsonObject createWeaponConfigJson(WeaponData weaponData) {
        JsonObject itemJson = new JsonObject();
        
        // 添加elementData部分
        JsonObject elementDataJson = new JsonObject();
        
        // 添加空的Basic层
        JsonArray basicArray = new JsonArray();
        elementDataJson.add("Basic", basicArray);
        
        // 添加空的Usage层
        JsonArray usageArray = new JsonArray();
        elementDataJson.add("Usage", usageArray);
        
        // 添加初始属性
        JsonArray initialModifiersArray = new JsonArray();
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            JsonObject modifierJson = new JsonObject();
            modifierJson.addProperty("name", entry.getName());
            modifierJson.addProperty("amount", entry.getAmount());
            modifierJson.addProperty("operation", entry.getOperation());
            
            initialModifiersArray.add(modifierJson);
        }
        elementDataJson.add("InitialModifiers", initialModifiersArray);
        
        itemJson.add("elementData", elementDataJson);
        
        return itemJson;
    }
    
    /**
     * 生成默认武器配置
     */
    private static void generateDefaultConfigs() {
        weaponConfigs.clear();
        
        try {
            // 确保目录存在
            Path weaponDir = Paths.get(WEAPON_DIR);
            if (!Files.exists(weaponDir)) {
                Files.createDirectories(weaponDir);
            }
            
            // 生成默认武器配置文件
            generateDefaultWeaponConfigFile();
            
            // 生成额外普通武器配置文件
            createDefaultAdditionalNormalWeaponsConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 生成默认武器配置文件
     */
    private static void generateDefaultWeaponConfigFile() throws IOException {
        File configFile = new File(DEFAULT_WEAPONS_FILE);
        
        // 获取所有可应用元素属性的物品
        Set<ResourceLocation> applicableItems = WeaponApplicableItemsFinder.findApplicableItems();
        
        // 创建默认配置内容
        JsonObject defaultConfig = new JsonObject();
        
        // 为每个适用物品生成默认配置
        for (ResourceLocation itemKey : applicableItems) {
            // 创建武器数据
            WeaponData weaponData = new WeaponData();
            
            // 添加默认初始属性
            addInitialModifiers(weaponData);
            
            // 创建配置JSON
            JsonObject itemJson = createWeaponConfigJson(weaponData);
            
            // 添加到配置对象
            defaultConfig.add(itemKey.toString(), itemJson);
            
        }
        
        // 写入配置文件
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(defaultConfig, writer);
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
            defaultConfig.addProperty("_example", "{\n  \"minecraft:diamond_sword\": {\n    \"elementData\": {\n      \"InitialModifiers\": [\n        {\"name\": \"SLASH\", \"amount\": 5.0, \"operation\": \"ADDITION\"},\n        {\"name\": \"CRITICAL_CHANCE\", \"amount\": 0.1, \"operation\": \"ADDITION\"}\n      ]\n    }\n  }\n}");
            
            // 写入配置文件
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(defaultConfig, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载武器配置
     */
    private static void loadWeaponConfigs() {
        weaponConfigs.clear();
        
        try {
            // 加载默认武器配置
            loadDefaultWeaponConfigsFromFile();
            
            // 加载额外普通武器配置
            loadAdditionalNormalWeaponConfigs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载默认武器配置
     */
    public static void loadDefaultWeaponConfigsFromFile() {
        try {
            File configFile = new File(DEFAULT_WEAPONS_FILE);
            if (!configFile.exists()) {
                return;
            }
            
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject config = gson.fromJson(reader, JsonObject.class);
                
                // 遍历所有物品配置
                for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
                    String itemName = entry.getKey();
                    
                    // 跳过注释和示例
                    if (itemName.startsWith("_")) {
                        continue;
                    }
                    
                    ResourceLocation itemKey = ResourceLocation.tryParse(itemName);
                    if (itemKey == null) {
                        continue;
                    }
                    
                    JsonElement itemConfig = entry.getValue();
                    
                    // 创建武器数据
                    WeaponData weaponData = new WeaponData();
                    
                    // 从配置文件加载数据
                    if (itemConfig.isJsonObject()) {
                        JsonObject itemJson = itemConfig.getAsJsonObject();
                        
                        // 加载元素数据
                        if (itemJson.has("elementData")) {
                            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
                            
                            // 加载初始属性
                            if (elementDataJson.has("InitialModifiers")) {
                                JsonArray initialModifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                                for (JsonElement modifierJson : initialModifiersArray) {
                                    if (modifierJson.isJsonObject()) {
                                        JsonObject modifierObject = modifierJson.getAsJsonObject();
                                        
                                        // 检查必要字段是否存在
                                        if (!modifierObject.has("name") || !modifierObject.has("amount") || !modifierObject.has("operation")) {
                                            continue;
                                        }
                                        
                                        String name = modifierObject.get("name").getAsString();
                                        double amount = modifierObject.get("amount").getAsDouble();
                                        String operation = modifierObject.get("operation").getAsString();
                                        
                                        // 生成UUID
                                        UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                        
                                        // 确保elementType包含命名空间，name保持原始名称
                                        String namespacedElementType = name.contains(":") ? name : "hamstercore:" + name;
                                        
                                        // 创建并添加初始属性
                                        weaponData.addInitialModifier(new InitialModifierEntry(name, namespacedElementType, amount, operation, modifierUuid, "def"));
                                        
                                        // 只有基础元素和复合元素才添加到Basic层
                                        ElementType type = ElementType.byName(name);
                                        if (type != null && (type.getTypeCategory() == ElementType.TypeCategory.BASIC || type.getTypeCategory() == ElementType.TypeCategory.COMPLEX)) {
                                            weaponData.addBasicElement(name, "def", 0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 如果没有从配置文件加载到数据，使用默认值
                    if (weaponData.getInitialModifiers().isEmpty()) {
                        addInitialModifiers(weaponData);
                    }
                    
                    // 添加到武器配置映射
                    weaponConfigs.put(itemKey, weaponData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载额外普通武器配置
     */
    public static void loadAdditionalNormalWeaponConfigs() {
        try {
            File configFile = new File(ADDITIONAL_NORMAL_WEAPONS_FILE);
            if (!configFile.exists()) {
                return;
            }
            
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject config = gson.fromJson(reader, JsonObject.class);
                
                // 遍历所有物品配置
                for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
                    String itemName = entry.getKey();
                    
                    // 跳过注释和示例
                    if (itemName.startsWith("_")) {
                        continue;
                    }
                    
                    ResourceLocation itemKey = ResourceLocation.tryParse(itemName);
                    if (itemKey == null) {
                        continue;
                    }
                    
                    JsonElement itemConfig = entry.getValue();
                    
                    // 创建武器数据
                    WeaponData weaponData = new WeaponData();
                    
                    // 添加默认初始属性
                    addInitialModifiers(weaponData);
                    
                    // 如果有自定义配置，应用自定义配置
                    if (itemConfig.isJsonObject()) {
                        JsonObject itemJson = itemConfig.getAsJsonObject();
                        
                        // 加载元素数据
                        if (itemJson.has("elementData")) {
                            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
                            
                            // 加载初始属性
                            if (elementDataJson.has("InitialModifiers")) {
                                // 清除默认属性
                                weaponData.getInitialModifiers().clear();
                                
                                JsonArray initialModifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                                for (JsonElement modifierJson : initialModifiersArray) {
                                    if (modifierJson.isJsonObject()) {
                                        JsonObject modifierObject = modifierJson.getAsJsonObject();
                                        
                                        String name = modifierObject.get("name").getAsString();
                                        double amount = modifierObject.get("amount").getAsDouble();
                                        String operation = modifierObject.get("operation").getAsString();
                                        
                                        // 生成UUID
                                        UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                        
                                        // 创建并添加初始属性
                                        weaponData.addInitialModifier(new InitialModifierEntry(name, name, amount, operation, modifierUuid, "def"));
                                        
                                        // 只有基础元素和复合元素才添加到Basic层
                                        ElementType type = ElementType.byName(name);
                                        if (type != null && (type.getTypeCategory() == ElementType.TypeCategory.BASIC || type.getTypeCategory() == ElementType.TypeCategory.COMPLEX)) {
                                            weaponData.addBasicElement(name, "def", 0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 添加到武器配置映射
                    weaponConfigs.put(itemKey, weaponData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}