package com.xlxyvergil.hamstercore.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

/**
 * 额外元素应用器
 * 用于处理玩家自定义的额外配置文件中的物品元素属性应用
 */
public class AdditionalElementApplier {
    
    // 额外配置文件路径
    private static final String ADDITIONAL_NORMAL_WEAPONS_FILE = "config/hamstercore/Weapon/additional_normal_weapons.json";
    
    /**
     * 应用额外的元素属性配置
     */
    public static int applyAdditionalElements() {
        
        // 确保配置已加载
        WeaponConfig.load();
        
        int appliedCount = 0;
        
        // 应用额外的普通武器配置
        appliedCount += applyAdditionalNormalWeapons();
        
        return appliedCount;
    }
    
    /**
     * 应用额外的普通物品的元素属性
     */
    public static int applyAdditionalNormalWeapons() {
        
        // 获取额外普通武器配置
        Map<ResourceLocation, WeaponData> additionalWeaponConfigs = getAdditionalWeaponConfigs();
        if (additionalWeaponConfigs == null) {
            return 0;
        }
        
        
        int appliedCount = 0;
        
        // 遍历所有额外普通物品配置，应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : additionalWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 获取额外武器配置
     */
    private static Map<ResourceLocation, WeaponData> getAdditionalWeaponConfigs() {
        File configFile = new File(ADDITIONAL_NORMAL_WEAPONS_FILE);
        if (!configFile.exists()) {
            return null;
        }
        
        try {
            Map<ResourceLocation, WeaponData> additionalConfigs = new java.util.HashMap<>();
            
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject configJson = JsonParser.parseReader(reader).getAsJsonObject();
                
                // 遍历配置中的所有物品
                for (Map.Entry<String, JsonElement> entry : configJson.entrySet()) {
                    String itemKeyStr = entry.getKey();
                    JsonObject itemConfig = entry.getValue().getAsJsonObject();
                    
                    // 跳过注释行
                    if (itemKeyStr.startsWith("_")) {
                        continue;
                    }
                    
                    try {
                        ResourceLocation itemKey = new ResourceLocation(itemKeyStr);
                        WeaponData weaponData = parseWeaponData(itemKey, itemConfig);
                        
                        if (weaponData != null) {
                            additionalConfigs.put(itemKey, weaponData);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            
            return additionalConfigs;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析武器数据
     * @param itemKey 物品键
     * @param itemConfig 物品配置
     * @return 武器数据
     */
    private static WeaponData parseWeaponData(ResourceLocation itemKey, JsonObject itemConfig) {
        try {
            WeaponData data = new WeaponData();
            
            // 基本信息
            data.modid = itemKey.getNamespace();
            data.itemId = itemKey.getPath();
            
            // 解析elementData部分
            if (itemConfig.has("elementData")) {
                JsonObject elementDataJson = itemConfig.getAsJsonObject("elementData");
                
                // 解析Basic层
                if (elementDataJson.has("Basic")) {
                    // 根据新的配置格式解析Basic层
                    // 新格式使用数组而不是对象
                    /* 示例格式:
                    "Basic": [
                        ["SLASH", "CONFIG", 0],
                        ["FIRE", "CONFIG", 1]
                    ]
                    */
                }
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 为普通物品应用元素属性
     * 使用新的两层数据结构
     */
    private static boolean applyElementAttributesToNormalItem(ResourceLocation itemKey, WeaponData weaponData) {
        // 检查武器数据是否为空
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 创建实际的ItemStack用于存储元素属性
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 应用元素修饰符到物品（使用独立实现并保存Basic层数据）
            applyElementModifiers(stack, weaponData.getBasicElements());
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 应用元素修饰符到物品
     * 根据配置文件中的元素属性修饰符，在Basic层里存储修饰符的元素类型、排序以及是否是CONFIG
     */
    private static void applyElementModifiers(ItemStack stack, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        // 同时将Basic层数据保存到NBT中
        WeaponData weaponData = new WeaponData();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                weaponData.addBasicElement(basicEntry.getType(), basicEntry.getSource(), basicEntry.getOrder());
            }
        }
        
        // 保存元素数据到NBT（只保存Basic层）
        WeaponDataManager.saveElementDataWithoutUsage(stack, weaponData);
    }

}