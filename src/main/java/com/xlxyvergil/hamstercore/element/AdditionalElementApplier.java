package com.xlxyvergil.hamstercore.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.FileReader;
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
                    JsonObject basicJson = elementDataJson.getAsJsonObject("Basic");
                    for (Map.Entry<String, JsonElement> basicEntry : basicJson.entrySet()) {
                        JsonObject elementJson = basicEntry.getValue().getAsJsonObject();
                        
                        String type = elementJson.get("type").getAsString();
                        double value = elementJson.get("value").getAsDouble();
                        String source = elementJson.get("source").getAsString();
                        
                        data.getElementData().addBasicElement(type, value, source);
                    }
                }
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 为普通物品应用元素属性
     * 使用新的四层数据结构
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
            
            // 确保物品栈有效
            if (stack.isEmpty()) {
                return false;
            }
            
            // 直接使用从配置加载的WeaponElementData
            WeaponElementData elementData = weaponData.getElementData();
            
            // 确保elementData不为空
            if (elementData == null) {
                elementData = new WeaponElementData();
            } else {
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 将数据写入NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}