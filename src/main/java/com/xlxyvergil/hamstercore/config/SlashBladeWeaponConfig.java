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
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
 * 拔刀剑武器配置管理器
 * 负责加载、保存和管理拔刀剑的元素配置
 */
public class SlashBladeWeaponConfig {
    
    private static final String SLASHBLADE_MOD_ID = "slashblade";
    
    // 默认特殊属性值
    private static final double DEFAULT_CRITICAL_CHANCE = 0.05; // 5%暴击率
    private static final double DEFAULT_CRITICAL_DAMAGE = 1.5;  // 1.5倍暴击伤害
    private static final double DEFAULT_TRIGGER_CHANCE = 0.1;   // 10%触发率
    
    // 默认物理元素占比（拔刀剑统一使用默认元素占比：切割70% 冲击20% 穿刺10%）
    private static final double DEFAULT_SLASH = 0.7;
    private static final double DEFAULT_IMPACT = 0.2;
    private static final double DEFAULT_PUNCTURE = 0.1;
    
    // 配置文件路径
    private static final String CONFIG_DIR = "config/hamstercore/";
    private static final String WEAPON_DIR = CONFIG_DIR + "Weapon/";
    private static final String SLASHBLADE_WEAPONS_FILE = WEAPON_DIR + "slashblade_weapons.json";
    
    // 拔刀剑配置缓存
    private static Map<String, WeaponData> slashBladeConfigCache = new HashMap<>();
    
    // 拔刀剑translationKey到配置的映射表
    private static Map<String, WeaponData> translationKeyToConfigMap = new HashMap<>();
    
    /**
     * 生成拔刀剑武器配置文件
     */
    public static void generateSlashBladeWeaponsConfig() {
        // 只有当SlashBlade模组加载时才生成配置
        if (!ModList.get().isLoaded(SLASHBLADE_MOD_ID)) {
            return;
        }
        
        Map<String, Object> slashBladeConfigs = new HashMap<>();
        
        // 获取所有拔刀剑translationKey
        Set<String> translationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys();
        List<JsonObject> weaponsList = new ArrayList<>();
        
        for (String translationKey : translationKeys) {
            WeaponData weaponData = createSlashBladeWeaponData(translationKey);
            if (weaponData != null) {
                JsonObject itemJson = createSlashBladeWeaponConfigJson(weaponData);
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
     * 为拔刀剑设置默认元素占比（统一配置）
     */
    private static void setDefaultElementRatiosForSlashBlade(WeaponData data, String translationKey) {
        // 拔刀剑统一使用默认元素占比：切割70% 冲击20% 穿刺10%
        data.addBasicElement(ElementType.SLASH.getName(), "CONFIG", 0);
        data.addBasicElement(ElementType.IMPACT.getName(), "CONFIG", 1);
        data.addBasicElement(ElementType.PUNCTURE.getName(), "CONFIG", 2);
    }
    
    /**
     * 创建拔刀剑武器配置的JSON对象
     * 适配新的两层数据结构
     */
    private static JsonObject createSlashBladeWeaponConfigJson(WeaponData weaponData) {
        JsonObject itemJson = new JsonObject();
        
        // 添加translationKey（如果存在）
        if (weaponData.translationKey != null && !weaponData.translationKey.isEmpty()) {
            itemJson.addProperty("translationKey", weaponData.translationKey);
        }
        
        // 添加elementData部分
        JsonObject elementDataJson = new JsonObject();
        
        // 添加Basic层 - 记录元素名称、来源和添加顺序
        JsonArray basicArray = new JsonArray();
        for (Map.Entry<String, List<BasicEntry>> entry : weaponData.getBasicElements().entrySet()) {
            for (BasicEntry basicEntry : entry.getValue()) {
                JsonArray elementArray = new JsonArray();
                elementArray.add(basicEntry.getType());
                elementArray.add(basicEntry.getSource());
                elementArray.add(basicEntry.getOrder());
                basicArray.add(elementArray);
            }
        }
        elementDataJson.add("Basic", basicArray);
        
        // 添加Usage层 - 使用空数组，将在运行时由ElementCombinationModifier计算
        JsonArray usageArray = new JsonArray();
        elementDataJson.add("Usage", usageArray);
        
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
            e.printStackTrace();
        }
    }
    
    /**
     * 加载拔刀剑配置（数组格式）
     */
    public static void loadSlashBladeConfigFile() {
        File configFile = new File(SLASHBLADE_WEAPONS_FILE);
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
                            processWeaponConfig(itemJson, configKey);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理单个武器配置（数组格式，用于拔刀剑）
     * 适配新的两层数据结构
     */
    private static void processWeaponConfig(JsonObject itemJson, String configKey) {
        // 创建WeaponData对象
        WeaponData weaponData = new WeaponData();
        
        // 读取translationKey（如果存在）
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
                        .add(new BasicEntry(type, source, order));
                }
            }
            
            // Usage层和Def层数据将由ElementCombinationModifier在运行时计算
            // 不需要从配置文件中读取这些数据
            
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
        
        // 根据配置类型决定内存映射的键名
        if (weaponData.translationKey != null) {
            // 拔刀剑使用统一的slashblade:slashblade作为键
            translationKeyToConfigMap.put(weaponData.translationKey, weaponData);
        }
    }
    
    /**
     * 获取拔刀剑武器配置（根据具体的刀）
     */
    public static WeaponData getWeaponConfig(ItemStack stack) {
        // 获取拔刀剑的translationKey - 使用SlashBladeItemsFetcher
        String translationKey = com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher.getSlashBladeTranslationKey(stack);
        if (translationKey != null) {
            // 查找匹配的translationKey配置
            return getWeaponConfigByTranslationKey(translationKey);
        }
        // 如果没找到具体配置，返回统一的slashblade:slashblade配置
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
        
        return null;
    }
    
    /**
     * 获取拔刀剑配置
     */
    public static Map<String, List<WeaponData>> getSlashBladeConfigs() {
        // 构建拔刀剑配置映射
        Map<String, List<WeaponData>> slashBladeConfigs = new HashMap<>();
        
        // 遍历translationKeyToConfigMap，将配置按照translationKey分组
        for (Map.Entry<String, WeaponData> entry : translationKeyToConfigMap.entrySet()) {
            String translationKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 每个translationKey对应一个WeaponData列表（尽管通常只有一个）
            slashBladeConfigs.computeIfAbsent(translationKey, k -> new ArrayList<>()).add(weaponData);
        }
        
        return slashBladeConfigs;
    }
    
    /**
     * 缓存拔刀剑配置到全局映射中
     */
    public static void cacheSlashBladeConfig(String translationKey, WeaponData weaponData) {
        if (translationKey != null && weaponData != null) {
            translationKeyToConfigMap.put(translationKey, weaponData);
        }
    }
    
    /**
     * 为武器添加初始修饰符
     */
    private static void addInitialModifiers(WeaponData data) {
        // 为每个基础元素添加初始修饰符
        for (Map.Entry<String, List<BasicEntry>> entry : data.getBasicElements().entrySet()) {
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
            data.addInitialModifier(new InitialModifierEntry(elementType, modifier));
        }
    }
}