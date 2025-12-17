package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * 额外配置应用类
 * 处理额外的元素属性配置应用
 */
public class AdditionalConfigApplier {
    
    /**
     * 应用额外的元素属性配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 确保配置已加载
        WeaponConfig.init();
        
        // 获取所有额外配置
        Map<ResourceLocation, WeaponData> additionalConfigs = loadAdditionalWeaponConfigs();
        if (additionalConfigs == null || additionalConfigs.isEmpty()) {
            return 0;
        }
        
        // 为每个额外配置应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : additionalConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            if (applyConfigToSingleItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 从文件加载额外武器配置
     */
    private static Map<ResourceLocation, WeaponData> loadAdditionalWeaponConfigs() {
        Map<ResourceLocation, WeaponData> additionalConfigs = new HashMap<>();
        
        try {
            File configFile = new File(WeaponConfig.ADDITIONAL_NORMAL_WEAPONS_FILE);
            if (!configFile.exists()) {
                return additionalConfigs;
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
                    WeaponConfig.addInitialModifiers(weaponData);
                    
                    // 如果有自定义配置，应用自定义配置
                    if (itemConfig.isJsonObject()) {
                        JsonObject itemJson = itemConfig.getAsJsonObject();
                        
                        // 加载元素数据
                        if (itemJson.has("elementData")) {
                            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
                            
                            // 不再读取Basic层
                            
                            // 不再读取Usage层
                            
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
                                        UUID uuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                        
                                        // 创建并添加初始属性
                                        InitialModifierEntry initialModifier = new InitialModifierEntry(name, name, amount, operation, uuid, "custom");
                                        weaponData.addInitialModifier(initialModifier);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 添加到额外配置映射
                    additionalConfigs.put(itemKey, weaponData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return additionalConfigs;
    }
    
    /**
     * 应用配置到单个物品
     */
    private static boolean applyConfigToSingleItem(ResourceLocation itemKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            WeaponConfig.cacheAdditionalWeaponConfig(itemKey, weaponData);
            
            // 创建物品堆并仅保存InitialModifier数据到NBT
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                // 只保存InitialModifier层数据
                WeaponDataManager.saveInitialModifierData(stack, weaponData);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}