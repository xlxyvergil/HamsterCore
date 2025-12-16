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
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.resources.ResourceLocation;
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
    private static final double DEFAULT_SLASH = 0.33;
    private static final double DEFAULT_IMPACT = 0.33;
    private static final double DEFAULT_PUNCTURE = 0.34;
    
    // 配置文件路径
    private static final String CONFIG_DIR = "config/hamstercore/";
    private static final String WEAPON_DIR = CONFIG_DIR + "Weapon/";
    private static final String TACZ_WEAPONS_FILE = WEAPON_DIR + "tacz_weapons.json";
    
    // TACZ配置缓存
    private static Map<String, WeaponData> taczConfigCache = new HashMap<>();
    
    // TACZ gunId到配置的映射表
    private static Map<String, WeaponData> gunIdToConfigMap = new HashMap<>();
    
    /**
     * 生成TACZ武器配置文件
     */
    public static void generateTacZWeaponsConfig() {
        // 只有当TACZ模组加载时才生成配置
        if (!ModList.get().isLoaded(TACZ_MOD_ID)) {
            return;
        }
        
        Map<String, Object> tacZConfigs = new HashMap<>();
        
        // 获取所有TACZ枪械ID
        Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
        List<JsonObject> weaponsList = new ArrayList<>();
        
        for (ResourceLocation gunId : tacZGunIDs) {
            WeaponData weaponData = createTacZWeaponData(gunId);
            if (weaponData != null) {
                JsonObject itemJson = createTacZWeaponConfigJson(weaponData);
                // 添加gunId字段到配置中
                itemJson.addProperty("gunId", gunId.toString());
                weaponsList.add(itemJson);
                
                // 存储到内存映射表，使用gunId作为键以便后续查找
                gunIdToConfigMap.put(gunId.toString(), weaponData);
            }
        }
        
        // 使用统一的物品ID作为键名，值为武器配置数组
        tacZConfigs.put("tacz:modern_kinetic_gun", weaponsList);
        
        // 保存TACZ武器配置文件
        saveWeaponConfigToFile(tacZConfigs, TACZ_WEAPONS_FILE);
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
        




        
        // 添加初始修饰符
        addInitialModifiers(data);
        
        return data;
    }
    
    /**
     * 为TACZ枪械设置默认元素占比
     */
    private static void setDefaultElementRatiosForTacZ(WeaponData data, ResourceLocation gunId) {
        // 不再向Basic层和Usage层添加数据
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
     * 处理单个武器配置（数组格式，用于TACZ）
     * 适配新的两层数据结构
     */
    private static void processWeaponConfig(JsonObject itemJson, String configKey) {
        // 创建WeaponData对象
        WeaponData weaponData = new WeaponData();
        
        // 读取gunId（如果存在）
        if (itemJson.has("gunId")) {
            weaponData.gunId = itemJson.get("gunId").getAsString();
        }
        
        // 读取elementData
        if (itemJson.has("elementData")) {
            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
                        
            // 不再读取Basic层
                        
            // 不再读取Usage层
                        
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
        ResourceLocation itemKey = null;
        if (weaponData.gunId != null) {
            // TACZ武器使用gunId作为键
            itemKey = ResourceLocation.tryParse(weaponData.gunId);
        }
        
        if (itemKey != null) {
            gunIdToConfigMap.put(weaponData.gunId, weaponData);
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
     * 缓存TACZ枪械配置到全局映射中
     */
    public static void cacheTacZGunConfig(String gunId, WeaponData weaponData) {
        if (gunId != null && weaponData != null) {
            gunIdToConfigMap.put(gunId, weaponData);
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
}