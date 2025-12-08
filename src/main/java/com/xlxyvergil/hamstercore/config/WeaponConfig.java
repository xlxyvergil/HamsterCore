package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.element.ElementHelper;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        public String modid;
        public String itemId;
        public String gunId;           // 仅TACZ枪械使用
        public String translationKey;   // 仅SlashBlade拔刀剑使用
        
        // 元素属性配置
        public double criticalChance;
        public double criticalDamage;
        public double triggerChance;
        public Map<String, Double> elementRatios;
        
        public WeaponData() {
            elementRatios = new HashMap<>();
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
     * 遍历所有物品并生成武器配置
     */
    private void generateWeaponConfigs() {
        HamsterCore.LOGGER.info("开始生成武器配置...");
        
        // 确保配置目录存在
        java.nio.file.Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER);
        try {
            if (!java.nio.file.Files.exists(configPath)) {
                java.nio.file.Files.createDirectories(configPath);
            }
        } catch (IOException e) {
            HamsterCore.LOGGER.error("无法创建配置目录", e);
            return;
        }
        
        // 清空现有配置
        weaponConfigs.clear();
        
        // 遍历所有已注册的物品
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            if (itemKey == null) continue;
            
            ItemStack stack = new ItemStack(item);
            
            // 检查物品是否可以应用元素属性
            if (ElementHelper.canApplyElements(stack)) {
                WeaponData weaponData = createWeaponData(stack, itemKey);
                if (weaponData != null) {
                    weaponConfigs.put(itemKey, weaponData);
                    HamsterCore.LOGGER.debug("添加武器配置: " + itemKey);
                }
            }
        }
        
        // 保存配置到文件
        saveWeaponConfigs();
        
        HamsterCore.LOGGER.info("武器配置生成完成，共 " + weaponConfigs.size() + " 项");
    }
    
    /**
     * 为物品创建武器配置数据
     */
    private WeaponData createWeaponData(ItemStack stack, ResourceLocation itemKey) {
        WeaponData data = new WeaponData();
        
        // 基本信息
        data.modid = itemKey.getNamespace();
        data.itemId = itemKey.getPath();
        
        // 特殊mod信息
        if (ModCompat.isGun(stack)) {
            data.gunId = ModCompat.getGunId(stack);
            // 如果gunId为null，可能是API调用失败，但仍然记录为TACZ枪械
            if (data.gunId == null) {
                data.gunId = "tacz:unknown";
                HamsterCore.LOGGER.warn("TACZ枪械获取gunId失败，使用默认值: " + itemKey);
            } else {
                HamsterCore.LOGGER.info("TACZ枪械: " + itemKey + ", GunId: " + data.gunId);
            }
        }
        
        if (ModCompat.isSlashBlade(stack)) {
            data.translationKey = ModCompat.getSlashBladeTranslationKey(stack);
            // 如果translationKey为null，可能是API调用失败，但仍然记录为SlashBlade
            if (data.translationKey == null) {
                data.translationKey = "item.slashblade.unknown";
                HamsterCore.LOGGER.warn("SlashBlade拔刀剑获取translationKey失败，使用默认值: " + itemKey);
            } else {
                HamsterCore.LOGGER.info("SlashBlade拔刀剑: " + itemKey + ", TranslationKey: " + data.translationKey);
            }
        }
        
        // 默认元素属性
        data.criticalChance = DEFAULT_CRITICAL_CHANCE;
        data.criticalDamage = DEFAULT_CRITICAL_DAMAGE;
        data.triggerChance = DEFAULT_TRIGGER_CHANCE;
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatios(stack, data);
        
        return data;
    }
    
    /**
     * 根据物品类型设置默认元素占比
     */
    private void setDefaultElementRatios(ItemStack stack, WeaponData data) {
        // 检查物品类型并设置不同的默认元素占比
        if (ModCompat.isGun(stack)) {
            // 枪械类：主要是穿刺和冲击
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.6);
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.3);
            data.elementRatios.put(ElementType.SLASH.getName(), 0.1);
        } 
        else if (ModCompat.isSlashBlade(stack)) {
            // 拔刀剑类：主要是切割
            data.elementRatios.put(ElementType.SLASH.getName(), 0.7);
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.2);
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
            // 剑类：主要是切割
            data.elementRatios.put(ElementType.SLASH.getName(), 0.6);
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.2);
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.2);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) {
            // 斧类：主要是冲击和切割
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.5);
            data.elementRatios.put(ElementType.SLASH.getName(), 0.4);
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.BowItem || 
                  stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
            // 弓弩类：主要是穿刺
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.7);
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.2);
            data.elementRatios.put(ElementType.SLASH.getName(), 0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
            // 三叉戟类：主要是冲击和穿刺
            data.elementRatios.put(ElementType.IMPACT.getName(), 0.5);
            data.elementRatios.put(ElementType.PUNCTURE.getName(), 0.4);
            data.elementRatios.put(ElementType.SLASH.getName(), 0.1);
        }
        else {
            // 其他武器：默认物理元素占比
            data.elementRatios.put(ElementType.SLASH.getName(), DEFAULT_SLASH);
            data.elementRatios.put(ElementType.IMPACT.getName(), DEFAULT_IMPACT);
            data.elementRatios.put(ElementType.PUNCTURE.getName(), DEFAULT_PUNCTURE);
        }
    }
    
    /**
     * 保存武器配置到JSON文件
     */
    private void saveWeaponConfigs() {
        try {
            // 使用Gson格式化输出
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            java.nio.file.Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FOLDER).resolve(CONFIG_FILE_NAME);
            try (java.io.FileWriter writer = new java.io.FileWriter(configPath.toFile())) {
                gson.toJson(weaponConfigs, writer);
            }
            
            HamsterCore.LOGGER.info("武器配置已保存到: " + configPath.toAbsolutePath());
        } catch (IOException e) {
            HamsterCore.LOGGER.error("保存武器配置时出错", e);
        }
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