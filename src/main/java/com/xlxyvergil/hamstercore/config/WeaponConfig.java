package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;

import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsFinder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
    private static final double DEFAULT_SLASH = 0.6;     // 60%劈砍伤害
    private static final double DEFAULT_IMPACT = 0.2;    // 20%冲击伤害
    private static final double DEFAULT_PUNCTURE = 0.2;  // 20%穿刺伤害
    
    // 配置文件路径
    private static final Path WEAPON_DIR = FMLPaths.CONFIGDIR.get().resolve("hamstercore/weapons/");
    private static final Path DEFAULT_WEAPONS_FILE = WEAPON_DIR.resolve("default_weapons.json");
    private static final Path ADDITIONAL_NORMAL_WEAPONS_FILE = WEAPON_DIR.resolve("additional_normal_weapons.json");
    
    // 武器配置映射
    private static final Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    // 配置好的物品堆映射
    private static final Map<ResourceLocation, ItemStack> configuredItemStacks = new HashMap<>();
    
    /**
     * 初始化武器配置系统
     */
    public static void init() {
        // 创建默认配置文件
        createDefaultWeaponConfigs();
        
        // 加载默认武器配置
        loadDefaultWeaponConfigs();
        
        // 加载额外普通武器配置
        loadAdditionalNormalWeaponConfigs();
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
    private static void addInitialModifiers(WeaponData data) {
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
    private static void addDefaultModifier(WeaponData data, String elementType, double defaultValue) {
        // 为每种元素类型使用固定的UUID
        UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementType).getBytes());
        
        // 添加到初始属性列表
        data.addInitialModifier(new InitialModifierEntry(elementType, elementType, defaultValue, "ADDITION", modifierUuid, "default"));
    }
    
    /**
     * 创建默认配置文件
     */
    private static void createDefaultWeaponConfigs() {
        try {
            // 确保目录存在
            Path weaponDir = WEAPON_DIR;
            if (!Files.exists(weaponDir)) {
                Files.createDirectories(weaponDir);
            }
            
            // 创建默认武器配置文件
            createDefaultWeaponConfigFile();
            
            // 创建额外普通武器配置文件
            createDefaultAdditionalNormalWeaponsConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建默认武器配置文件
     */
    private static void createDefaultWeaponConfigFile() throws IOException {
        File configFile = DEFAULT_WEAPONS_FILE.toFile();
        if (configFile.exists()) {
            return; // 文件已存在，不需要重新创建
        }
        
        // 创建默认配置内容（只包含注释和示例）
        JsonObject defaultConfig = new JsonObject();
        defaultConfig.addProperty("_comment", "默认武器配置文件，请勿直接修改此文件");
        defaultConfig.addProperty("_note", "如需添加额外普通武器，请修改additional_normal_weapons.json文件");
        
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
            Path weaponDir = WEAPON_DIR;
            if (!Files.exists(weaponDir)) {
                Files.createDirectories(weaponDir);
            }
            
            File configFile = ADDITIONAL_NORMAL_WEAPONS_FILE.toFile();
            if (configFile.exists()) {
                return; // 文件已存在，不需要重新创建
            }
            
            // 创建默认配置内容（只包含注释和示例）
            JsonObject defaultConfig = new JsonObject();
            defaultConfig.addProperty("_comment", "在此添加您想要应用元素属性的额外普通物品，格式如下:");
            defaultConfig.addProperty("_example_full", "{\n" + "  \"minecraft:diamond_sword\": {\n" + "    \"elementData\": {\n" + "      \"InitialModifiers\": [\n" + "        {\"name\": \"SLASH\", \"amount\": 5.0, \"operation\": \"ADDITION\"},\n" + "        {\"name\": \"CRITICAL_CHANCE\", \"amount\": 0.1, \"operation\": \"ADDITION\"}\n" + "      ]\n" + "    }\n" + "  }\n" + "}");
            defaultConfig.addProperty("_example_simple", "{\n" + "  \"minecraft:iron_sword\": {}, // 使用默认值\n" + "  \"minecraft:diamond_axe\": {}   // 使用默认值\n" + "}");
            
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
     * 加载默认武器配置
     */
    private static void loadDefaultWeaponConfigs() {
        // 获取所有可应用元素属性的物品
        Set<ResourceLocation> applicableItems = WeaponApplicableItemsFinder.findApplicableItems();
        
        // 为每个适用物品生成默认配置
        for (ResourceLocation itemKey : applicableItems) {
            // 只有当物品还没有配置时才生成默认配置
            if (!weaponConfigs.containsKey(itemKey)) {
                // 创建武器数据
                WeaponData weaponData = new WeaponData();
                
                // 添加默认初始属性
                addInitialModifiers(weaponData);
                
                // 添加到武器配置映射
                weaponConfigs.put(itemKey, weaponData);
            }
        }
    }
    
    /**
     * 加载额外普通武器配置
     */
    private static void loadAdditionalNormalWeaponConfigs() {
        try {
            File configFile = ADDITIONAL_NORMAL_WEAPONS_FILE.toFile();
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
                                JsonArray initialModifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                                for (JsonElement modifierJson : initialModifiersArray) {
                                    if (modifierJson.isJsonObject()) {
                                        JsonObject modifierObject = modifierJson.getAsJsonObject();
                                        
                                        String name = modifierObject.get("name").getAsString();
                                        double amount = modifierObject.get("amount").getAsDouble();
                                        String operation = modifierObject.get("operation").getAsString();
                                        
                                        // 生成UUID
                                        UUID uuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                        
                                        // 创建并添加初始属性
                                        InitialModifierEntry initialModifier = new InitialModifierEntry(name, name, amount, operation, uuid, "custom");
                                        weaponData.addInitialModifier(initialModifier);
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