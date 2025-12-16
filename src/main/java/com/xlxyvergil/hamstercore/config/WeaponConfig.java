package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;

import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;

import com.xlxyvergil.hamstercore.util.ElementUUIDManager;

import java.io.File;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 武器配置管理器
 * 负责加载、保存和管理所有武器的元素配置
 * 已适配新的两层NBT数据结构
 * 注意：TACZ和拔刀剑的配置已移至独立的配置类中
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
    private static final String ADDITIONAL_NORMAL_WEAPONS_FILE = WEAPON_DIR + "additional_normal_weapons.json";
    
    // 武器配置映射表
    private static Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    // 已配置的物品堆映射表
    private static Map<ResourceLocation, ItemStack> configuredItemStacks = new HashMap<>();
    
    /**
     * 加载武器配置
     * 如果配置文件不存在，则生成默认配置
     */
    public static void load() {
        // 检查配置文件是否存在，如果不存在则生成默认配置
        File normalFile = new File(NORMAL_WEAPONS_FILE);
        
        if (!normalFile.exists()) {
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
        
        // 生成普通武器配置文件
        generateNormalWeaponsConfig();
        
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
        
        // 为每个普通物品创建配置
        for (ResourceLocation itemKey : applicableItems) {
            if (!modSpecialItems.contains(itemKey)) {
                WeaponData weaponData = createNormalWeaponData(itemKey);
                if (weaponData != null) {
                    JsonObject itemJson = createNormalWeaponConfigJson(weaponData);
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
     * 为普通物品创建武器配置数据
     */
    private static WeaponData createNormalWeaponData(ResourceLocation itemKey) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        if (itemKey != null) {
            data.modid = itemKey.getNamespace();
            data.itemId = itemKey.getPath();
        }
        




        
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
        // 不再向Basic层添加元素，仅保留空实现
    }
    
    /**
     * 创建普通武器配置的JSON对象
     * 只处理InitialModifier层数据
     */
    private static JsonObject createNormalWeaponConfigJson(WeaponData weaponData) {
        JsonObject itemJson = new JsonObject();
        
        // 添加elementData部分
        JsonObject elementDataJson = new JsonObject();
        
        // 添加初始属性修饰符数据（只包含名称和数值，UUID在应用阶段生成）
        JsonArray modifiersArray = new JsonArray();
        for (InitialModifierEntry modifierEntry : weaponData.getInitialModifiers()) {
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
     * 从配置文件加载武器配置
     */
    private static void loadWeaponConfigs() {
        weaponConfigs.clear();
        
        try {
            // 加载普通武器配置（使用对象格式）
            loadNormalWeaponsConfig();
            
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
     * 处理普通武器配置（数组格式）
     * 只处理InitialModifier层数据
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
                    weaponData.addInitialModifier(new InitialModifierEntry(name, modifier));
                }
            }
        }
        
        // 使用物品ID作为键
        if (itemResourceKey != null) {
            weaponConfigs.put(itemResourceKey, weaponData);
        }
    }
    
    /**
     * 获取物品的武器配置
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
     * 获取所有武器配置
     */
    public static Map<ResourceLocation, WeaponData> getAllWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
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
     * 缓存已配置的物品堆到全局映射中
     */
    public static void cacheConfiguredItemStack(ResourceLocation itemKey, ItemStack stack) {
        if (itemKey != null && stack != null && !stack.isEmpty()) {
            configuredItemStacks.put(itemKey, stack);
        }
    }
    
    /**
     * 获取已配置的物品堆
     */
    public static ItemStack getConfiguredItemStack(ResourceLocation itemKey) {
        return configuredItemStacks.get(itemKey);
    }
    
    /**
     * 获取额外武器配置（与getAllWeaponConfigs相同）
     */
    public static Map<ResourceLocation, WeaponData> getAdditionalWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
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
     * 为武器添加初始修饰符
     */
    private static void addInitialModifiers(WeaponData data) {
        // 直接添加默认的初始修饰符，不依赖Basic层数据
        
        // 添加默认物理元素修饰符
        addDefaultModifier(data, ElementType.SLASH.getName(), DEFAULT_SLASH);
        addDefaultModifier(data, ElementType.IMPACT.getName(), DEFAULT_IMPACT);
        addDefaultModifier(data, ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        
        // 添加默认特殊属性修饰符
        addDefaultModifier(data, "critical_chance", DEFAULT_CRITICAL_CHANCE);
        addDefaultModifier(data, "critical_damage", DEFAULT_CRITICAL_DAMAGE);
        addDefaultModifier(data, "trigger_chance", DEFAULT_TRIGGER_CHANCE);
    }
    
    /**
     * 添加默认修饰符
     */
    private static void addDefaultModifier(WeaponData data, String elementType, double defaultValue) {
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
        data.addInitialModifier(new InitialModifierEntry(elementType, modifier));
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
                    + "      \"InitialModifiers\": [\n"
                    + "        {\"name\": \"SLASH\", \"amount\": 5.0, \"operation\": \"ADDITION\"},\n"
                    + "        {\"name\": \"CRITICAL_CHANCE\", \"amount\": 0.1, \"operation\": \"ADDITION\"}\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
            
            // 保存文件
            saveWeaponConfigToFile(Collections.singletonMap("_comment", defaultConfig), ADDITIONAL_NORMAL_WEAPONS_FILE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}