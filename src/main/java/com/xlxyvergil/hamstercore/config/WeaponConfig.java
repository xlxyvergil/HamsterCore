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
    private static final String WEAPON_CONFIG_FILE = CONFIG_DIR + "weapons.json";
    
    // 武器配置映射表
    private static Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    /**
     * 加载武器配置
     * 如果配置文件不存在，则生成默认配置
     */
    public static void load() {
        File configFile = new File(WEAPON_CONFIG_FILE);
        
        if (!configFile.exists()) {
            // 配置文件不存在，生成默认配置
            generateDefaultConfigs();
            saveWeaponConfigs();
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
        
        // 添加普通可应用元素属性的物品
        addNormalApplicableItems();
        
        // 添加MOD特殊物品（如TACZ枪械、拔刀剑等）
        addModSpecialItems();
        
    }
    
    /**
     * 添加普通可应用元素属性的物品
     */
    private static void addNormalApplicableItems() {
        // 查找所有可应用元素属性的物品
        Set<ResourceLocation> applicableItems = com.xlxyvergil.hamstercore.util.WeaponApplicableItemsFinder.findApplicableItems();
        
        // 为每个适用物品创建配置
        for (ResourceLocation itemKey : applicableItems) {
            if (!weaponConfigs.containsKey(itemKey)) {
                WeaponData weaponData = createNormalWeaponData(itemKey);
                if (weaponData != null) {
                    weaponConfigs.put(itemKey, weaponData);
                }
            }
        }
        
    }
    
    /**
     * 添加MOD特殊物品（如TACZ枪械、拔刀剑等）
     */
    private static void addModSpecialItems() {
        
        // 添加TACZ枪械
        Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
        for (ResourceLocation gunId : tacZGunIDs) {
            if (!weaponConfigs.containsKey(gunId)) {
                WeaponData weaponData = createModSpecialWeaponData(gunId, true, false);
                if (weaponData != null) {
                    weaponConfigs.put(gunId, weaponData);
                }
            }
        }
        
        // 添加拔刀剑
        Set<ResourceLocation> slashBladeIDs = SlashBladeItemsFetcher.getSlashBladeIDs();
        for (ResourceLocation bladeId : slashBladeIDs) {
            if (!weaponConfigs.containsKey(bladeId)) {
                WeaponData weaponData = createModSpecialWeaponData(bladeId, false, true);
                if (weaponData != null) {
                    weaponConfigs.put(bladeId, weaponData);
                }
            }
        }
        
        // MOD特殊物品配置添加完成，新增TACZ枪械和拔刀剑数量记录
    }
    
    /**
     * 为MOD特殊物品创建武器配置数据
     */
    private static WeaponData createModSpecialWeaponData(ResourceLocation itemKey, boolean isGun, boolean isSlashBlade) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        if (itemKey != null) {
            data.modid = itemKey.getNamespace();
            data.itemId = itemKey.getPath();
        }
        
        // 特殊mod信息
        if (isGun) {
            data.gunId = itemKey.toString(); // 使用真实的gunId
        }
        
        if (isSlashBlade) {
            data.translationKey = "item.slashblade." + itemKey.getPath(); // 使用真实的translationKey
        }
        
        // 添加默认特殊属性
        data.elementData.addBasicElement("critical_chance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("critical_damage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("trigger_chance", DEFAULT_TRIGGER_CHANCE);
        
        // 设置默认元素占比到Basic层
        setDefaultElementRatiosForModSpecial(data.elementData, isGun, isSlashBlade);
        
        return data;
    }
    
    /**
     * 为MOD特殊物品设置默认元素占比
     */
    private static void setDefaultElementRatiosForModSpecial(WeaponElementData elementData, boolean isGun, boolean isSlashBlade) {
        if (isGun) {
            // 枪械类：主要是穿刺和冲击
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.6);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.3);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        } 
        else if (isSlashBlade) {
            // 拔刀剑类：主要是切割
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.7);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.1);
        }
        else {
            // 其他武器：默认物理元素占比
            elementData.addBasicElement(ElementType.SLASH.getName(), DEFAULT_SLASH);
            elementData.addBasicElement(ElementType.IMPACT.getName(), DEFAULT_IMPACT);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        }
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
     * 为物品创建武器配置数据
     */
    private static WeaponData createWeaponData(ItemStack stack, ResourceLocation itemKey) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        if (itemKey != null) {
            data.modid = itemKey.getNamespace();
            data.itemId = itemKey.getPath();
        }
        
        // 特殊mod信息
        if (ModCompat.isGun(stack)) {
            String gunId = ModCompat.getGunId(stack);
            if (gunId != null) {
                data.gunId = gunId;
            } else {
                // 如果无法获取gunId，使用itemKey作为备用
                data.gunId = itemKey.toString();
            }
        }
        
        if (ModCompat.isSlashBlade(stack)) {
            String translationKey = ModCompat.getSlashBladeTranslationKey(stack);
            if (translationKey != null) {
                data.translationKey = translationKey;
            } else {
                // 如果无法获取translationKey，使用默认格式
                data.translationKey = "item.slashblade." + itemKey.getPath();
            }
        }
        
        // 添加默认特殊属性
        data.elementData.addBasicElement("critical_chance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("critical_damage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("trigger_chance", DEFAULT_TRIGGER_CHANCE);
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatios(stack, data.elementData);
        
        return data;
    }
    
    /**
     * 根据物品类型设置默认元素占比
     */
    private static void setDefaultElementRatios(ItemStack stack, WeaponElementData elementData) {
        // 检查物品类型并设置不同的默认元素占比
        if (ModCompat.isGun(stack)) {
            // 枪械类：主要是穿刺和冲击
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.6);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.3);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        } 
        else if (ModCompat.isSlashBlade(stack)) {
            // 拔刀剑类：主要是切割
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.7);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
            // 剑类：主要是切割
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.6);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.2);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) {
            // 斧类：主要是冲击和切割
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.5);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.4);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.BowItem || 
                      stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
            // 弓弩类：主要是穿刺
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.7);
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.2);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
            // 三叉戟类：主要是冲击和穿刺
            elementData.addBasicElement(ElementType.IMPACT.getName(), 0.5);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), 0.4);
            elementData.addBasicElement(ElementType.SLASH.getName(), 0.1);
        }
        else {
            // 其他武器：默认物理元素占比
            elementData.addBasicElement(ElementType.SLASH.getName(), DEFAULT_SLASH);
            elementData.addBasicElement(ElementType.IMPACT.getName(), DEFAULT_IMPACT);
            elementData.addBasicElement(ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        }
    }
    
    /**
     * 保存武器配置到JSON文件
     */
    private static void saveWeaponConfigs() {
        try {
            // 使用Gson格式化输出
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // 创建直接的JSON结构而不是使用WeaponData对象
            Map<String, JsonObject> configJson = new HashMap<>();
            
            // 转换配置格式
            for (Map.Entry<ResourceLocation, WeaponData> entry : weaponConfigs.entrySet()) {
                ResourceLocation itemKey = entry.getKey();
                WeaponData weaponData = entry.getValue();
                
                // 创建直接的JSON对象
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
                
                // 使用itemKey作为键
                configJson.put(itemKey.toString(), itemJson);
            }
            
            // 确保配置目录存在
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            Path configPath = Paths.get(WEAPON_CONFIG_FILE);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                gson.toJson(configJson, writer);
            }
            
        } catch (IOException e) {
        }
    }
    
    /**
     * 创建简化的武器数据，去除不必要的字段
     */
    private static WeaponData createSimplifiedWeaponData(WeaponData originalData, ResourceLocation itemKey) {
        WeaponData simplifiedData = new WeaponData();
        
        // 只复制elementData
        simplifiedData.elementData = originalData.getElementData();
        
        // 特殊处理TACZ枪械和拔刀剑
        if (originalData.gunId != null && !originalData.gunId.isEmpty()) {
            // TACZ枪械保留gunId字段
            simplifiedData.gunId = originalData.gunId;
        }
        
        if (originalData.translationKey != null && !originalData.translationKey.isEmpty()) {
            // 拔刀剑保留translationKey字段
            simplifiedData.translationKey = originalData.translationKey;
        }
        
        // 不再复制其他额外字段
        
        return simplifiedData;
    }
    
    /**
     * 加载武器配置
     */
    private static void loadWeaponConfigs() {
        try {
            // 使用Gson解析JSON文件
            Gson gson = new Gson();
            java.nio.file.Path configPath = java.nio.file.Paths.get(WEAPON_CONFIG_FILE);
            try (java.io.FileReader reader = new java.io.FileReader(configPath.toFile())) {
                // 直接解析为JsonObject
                JsonObject loadedConfigs = gson.fromJson(reader, JsonObject.class);
                
                // 转换配置格式
                for (Map.Entry<String, JsonElement> entry : loadedConfigs.entrySet()) {
                    String configKey = entry.getKey();
                    JsonObject itemJson = entry.getValue().getAsJsonObject();
                    
                    ResourceLocation itemKey = ResourceLocation.tryParse(configKey);
                    if (itemKey != null) {
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
                                    String elementType = basicEntry.getKey();
                                    JsonObject elementJson = basicEntry.getValue().getAsJsonObject();
                                    
                                    String type = elementJson.get("type").getAsString();
                                    double value = elementJson.get("value").getAsDouble();
                                    String source = elementJson.get("source").getAsString();
                                    
                                    weaponData.getElementData().addBasicElement(type, value, source);
                                }
                            }
                        }
                        
                        weaponConfigs.put(itemKey, weaponData);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
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
}
