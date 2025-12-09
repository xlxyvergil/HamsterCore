package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsFinder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 武器配置类，用于管理所有可应用元素属性的武器的默认配置
 * 在游戏启动时自动遍历所有物品并生成配置文件
 */
public class WeaponConfig {
    
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String CONFIG_FILE_NAME = "weapon_config.json";
    
    // 默认元素属性配置
    private static final double DEFAULT_CRITICAL_CHANCE = 0.2;  // 暴击率 20%
    private static final double DEFAULT_CRITICAL_DAMAGE = 0.5;  // 暴击伤害 50%
    private static final double DEFAULT_TRIGGER_CHANCE = 0.2;   // 触发率 20%
    private static final double DEFAULT_SLASH = 0.5;           // 切割 50%
    private static final double DEFAULT_IMPACT = 0.2;          // 冲击 20%
    private static final double DEFAULT_PUNCTURE = 0.3;         // 穿刺 30%
    
    /**
     * 武器配置数据结构
     */
    public static class WeaponData {
        public String modid;           // 可以为null
        public String itemId;           // 可以为null
        public String gunId;           // 仅TACZ枪械使用
        public String translationKey;   // 仅SlashBlade拔刀剑使用
        
        // 武器元素数据结构
        public WeaponElementData elementData;
        
        public WeaponData() {
            elementData = new WeaponElementData();
        }
        
        /**
         * 获取元素数据
         */
        public WeaponElementData getElementData() {
            return elementData != null ? elementData : new WeaponElementData();
        }
    }
    
    /**
     * 存储所有武器配置的映射
     */
    private Map<ResourceLocation, WeaponData> weaponConfigs = new HashMap<>();
    
    /**
     * 加载武器配置
     */
    public static void load() {
        WeaponConfig config = new WeaponConfig();
        // 在世界加载时生成配置
        config.generateWeaponConfigs();
        // 保存实例
        instance = config;
    }
    
    /**
     * 在世界加载完成后重新生成武器配置（获取真实ID）
     */
    public static void reloadWithRealIds() {
        DebugLogger.log("重新加载武器配置以获取真实ID...");
        if (instance != null) {
            instance.generateWeaponConfigs();
            DebugLogger.log("武器配置重新加载完成");
        } else {
            DebugLogger.log("武器配置实例为空，无法重新加载");
        }
    }
    
    /**
     * 遍历所有物品并生成武器配置
     */
    private void generateWeaponConfigs() {
        DebugLogger.log("开始生成武器配置...");
        
        // 确保配置目录存在
        java.nio.file.Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        try {
            if (!java.nio.file.Files.exists(configPath)) {
                java.nio.file.Files.createDirectories(configPath);
            }
        } catch (IOException e) {
            DebugLogger.log("无法创建配置目录", e);
            return;
        }
        
        // 清空现有配置
        weaponConfigs.clear();
        
        // 使用新的工具类查找可应用元素属性的普通物品
        Set<ResourceLocation> applicableItems = WeaponApplicableItemsFinder.findApplicableItems();
        
        // 为每个可应用的普通物品生成配置
        for (ResourceLocation itemKey : applicableItems) {
            Item item = ForgeRegistries.ITEMS.getValue(itemKey);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                WeaponData weaponData = createWeaponData(stack, itemKey);
                if (weaponData != null) {
                    weaponConfigs.put(itemKey, weaponData);
                    DebugLogger.log("添加武器配置: %s", itemKey.toString());
                }
            }
        }
        
        // 添加MOD特殊物品（即使它们尚未注册）
        addModSpecialItems();
        
        // 保存配置到文件
        saveWeaponConfigs();
        
        DebugLogger.log("武器配置生成完成，共 " + weaponConfigs.size() + " 项");
    }
    
    /**
     * 添加MOD特殊物品到配置中
     */
    private void addModSpecialItems() {
        DebugLogger.log("开始添加MOD特殊物品配置...");
        
        // 添加TACZ枪械
        Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
        HamsterCore.LOGGER.info("获取到 %d 个TACZ枪械ID", tacZGunIDs.size());
        for (ResourceLocation gunId : tacZGunIDs) {
            DebugLogger.log("处理TACZ枪械: %s", gunId.toString());
            if (!weaponConfigs.containsKey(gunId)) {
                WeaponData weaponData = createModSpecialWeaponData(gunId, true, false);
                if (weaponData != null) {
                    weaponConfigs.put(gunId, weaponData);
                    DebugLogger.log("添加TACZ枪械配置: %s", gunId.toString());
                }
            }
        }
        
        // 添加拔刀剑
        Set<ResourceLocation> slashBladeIDs = SlashBladeItemsFetcher.getSlashBladeIDs();
        HamsterCore.LOGGER.info("获取到 %d 个拔刀剑ID", slashBladeIDs.size());
        for (ResourceLocation bladeId : slashBladeIDs) {
            DebugLogger.log("处理拔刀剑: %s", bladeId.toString());
            if (!weaponConfigs.containsKey(bladeId)) {
                WeaponData weaponData = createModSpecialWeaponData(bladeId, false, true);
                if (weaponData != null) {
                    weaponConfigs.put(bladeId, weaponData);
                    DebugLogger.log("添加拔刀剑配置: %s", bladeId.toString());
                }
            }
        }
        
        HamsterCore.LOGGER.info("MOD特殊物品配置添加完成，新增TACZ枪械: %d, 新增拔刀剑: %d", 
                               tacZGunIDs.size(), slashBladeIDs.size());
    }
    
    /**
     * 为MOD特殊物品创建武器配置数据
     */
    private WeaponData createModSpecialWeaponData(ResourceLocation itemKey, boolean isGun, boolean isSlashBlade) {
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
        
        // 设置默认特殊属性到Basic层
        data.elementData.addBasicElement("criticalChance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("criticalDamage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("triggerChance", DEFAULT_TRIGGER_CHANCE);
        
        // 设置默认元素占比到Basic层
        setDefaultElementRatiosForModSpecial(data.elementData, isGun, isSlashBlade);
        
        return data;
    }
    
    /**
     * 为MOD特殊物品设置默认元素占比
     */
    private void setDefaultElementRatiosForModSpecial(WeaponElementData elementData, boolean isGun, boolean isSlashBlade) {
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
     * 为物品创建武器配置数据
     */
    private WeaponData createWeaponData(ItemStack stack, ResourceLocation itemKey) {
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
            DebugLogger.log("TACZ枪械: " + itemKey + ", GunId: " + data.gunId);
        }
        
        if (ModCompat.isSlashBlade(stack)) {
            String translationKey = ModCompat.getSlashBladeTranslationKey(stack);
            if (translationKey != null) {
                data.translationKey = translationKey;
            } else {
                // 如果无法获取translationKey，使用默认格式
                data.translationKey = "item.slashblade." + itemKey.getPath();
            }
            DebugLogger.log("SlashBlade拔刀剑: " + itemKey + ", TranslationKey: " + data.translationKey);
        }
        
        // 设置默认特殊属性到Basic层
        data.elementData.addBasicElement("criticalChance", DEFAULT_CRITICAL_CHANCE);
        data.elementData.addBasicElement("criticalDamage", DEFAULT_CRITICAL_DAMAGE);
        data.elementData.addBasicElement("triggerChance", DEFAULT_TRIGGER_CHANCE);
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatios(stack, data.elementData);
        
        return data;
    }
    
    /**
     * 根据物品类型设置默认元素占比
     */
    private void setDefaultElementRatios(ItemStack stack, WeaponElementData elementData) {
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
    private void saveWeaponConfigs() {
        try {
            // 使用Gson格式化输出
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // 创建一个新的映射，用于保存简化后的配置
            Map<String, WeaponData> simplifiedConfigs = new HashMap<>();
            
            // 转换配置格式
            for (Map.Entry<ResourceLocation, WeaponData> entry : weaponConfigs.entrySet()) {
                ResourceLocation itemKey = entry.getKey();
                WeaponData weaponData = entry.getValue();
                
                // 对于TACZ枪械和拔刀剑，使用gunId或translationKey作为键
                String configKey;
                if (weaponData.gunId != null && !weaponData.gunId.isEmpty()) {
                    // TACZ枪械使用itemKey作为键，gunId作为内部值
                    configKey = itemKey.toString();
                } else if (weaponData.translationKey != null && !weaponData.translationKey.isEmpty()) {
                    // 拔刀剑使用itemKey作为键，translationKey作为内部值
                    configKey = itemKey.toString();
                } else {
                    // 其他物品使用原有的ResourceLocation作为键
                    configKey = itemKey.toString();
                }
                
                simplifiedConfigs.put(configKey, createSimplifiedWeaponData(weaponData, itemKey));
            }
            
            java.nio.file.Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER).resolve(CONFIG_FILE_NAME);
            try (java.io.FileWriter writer = new java.io.FileWriter(configPath.toFile())) {
                gson.toJson(simplifiedConfigs, writer);
            }
            
            DebugLogger.log("武器配置已保存到: " + configPath.toAbsolutePath());
        } catch (IOException e) {
            DebugLogger.log("保存武器配置时出错", e);
        }
    }
    
    /**
     * 创建简化的武器数据，去除不必要的字段
     */
    private WeaponData createSimplifiedWeaponData(WeaponData originalData, ResourceLocation itemKey) {
        WeaponData simplifiedData = new WeaponData();
        
        // 复制elementData
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
        
        // 清除不必要的modid和itemId字段
        simplifiedData.modid = null;
        simplifiedData.itemId = null;
        
        return simplifiedData;
    }
    
    /**
     * 获取物品的武器配置
     */
    public WeaponData getWeaponConfig(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return null;
        
        return weaponConfigs.get(itemKey);
    }
    
    /**
     * 检查物品是否有武器配置
     */
    public boolean hasWeaponConfig(ItemStack stack) {
        return getWeaponConfig(stack) != null;
    }
    
    /**
     * 获取所有武器配置
     */
    public Map<ResourceLocation, WeaponData> getAllWeaponConfigs() {
        return new HashMap<>(weaponConfigs);
    }
    
    // 静态实例，用于全局访问
    private static WeaponConfig instance;
    
    /**
     * 静态方法获取武器配置
     */
    public static WeaponData getWeaponConfigStatic(ItemStack stack) {
        if (instance == null) return null;
        return instance.getWeaponConfig(stack);
    }
    
    /**
     * 静态方法检查物品是否有武器配置
     */
    public static boolean hasWeaponConfigStatic(ItemStack stack) {
        if (instance == null) return false;
        return instance.hasWeaponConfig(stack);
    }
    
    /**
     * 获取静态实例
     */
    public static WeaponConfig getInstance() {
        return instance;
    }
}