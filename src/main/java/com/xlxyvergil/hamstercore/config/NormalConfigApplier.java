package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * 普通物品配置应用类
 * 处理普通物品的NBT应用
 */
public class NormalConfigApplier {
    
    /**
     * 应用普通物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 确保配置已加载
        WeaponConfig.init();
        
        // 获取所有武器配置（仅默认配置，不含额外配置）
        Map<ResourceLocation, WeaponData> allWeaponConfigs = loadDefaultWeaponConfigs();
        
        // 遍历所有配置，过滤掉MOD特殊物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : allWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 检查是否为MOD特殊物品
            if (isModSpecialItem(itemKey)) {
                continue; // 跳过MOD特殊物品，由其他应用器处理
            }
            
            // 直接应用配置到物品
            if (applyConfigToSingleItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 从文件加载默认武器配置
     */
    private static Map<ResourceLocation, WeaponData> loadDefaultWeaponConfigs() {
        // 创建一个临时映射来存储配置
        Map<ResourceLocation, WeaponData> weaponConfigs = new java.util.HashMap<>();
        
        try {
            // 加载默认武器配置
            loadDefaultWeaponConfigsFromFile(weaponConfigs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return weaponConfigs;
    }
    
    /**
     * 从文件加载默认武器配置
     */
    private static void loadDefaultWeaponConfigsFromFile(Map<ResourceLocation, WeaponData> weaponConfigs) {
        try {
            File configFile = new File(WeaponConfig.DEFAULT_WEAPONS_FILE);
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
                    
                    // 添加到武器配置映射
                    weaponConfigs.put(itemKey, weaponData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 判断是否为MOD特殊物品
     */
    private static boolean isModSpecialItem(ResourceLocation itemKey) {
        String itemKeyStr = itemKey.toString();
        
        // 检查是否为TACZ枪械
        if ("tacz:modern_kinetic_gun".equals(itemKeyStr)) {
            return true;
        }
        
        // 检查是否为拔刀剑
        if ("slashblade:slashblade".equals(itemKeyStr)) {
            return true;
        }
        
        return false;
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
            WeaponConfig.cacheWeaponConfig(itemKey, weaponData);
            
            // 创建物品堆并仅保存InitialModifier数据到NBT
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                // 只保存InitialModifier层数据
                WeaponDataManager.saveInitialModifierData(stack, weaponData);
                // 将配置好的物品保存到全局映射中，供游戏运行时使用
                WeaponConfig.cacheConfiguredItemStack(itemKey, stack);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 应用普通物品的配置到物品堆
     * @param stack 物品堆
     * @return 是否成功应用配置
     */
    public static boolean applyConfigToItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 获取物品的ResourceLocation
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) {
            return false;
        }
        
        // 检查是否为MOD特殊物品
        if (isModSpecialItem(itemKey)) {
            return false; // MOD特殊物品不由这个类处理
        }
        
        // 获取物品配置
        WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            return false;
        }
        
        // 应用配置到物品NBT，只保存InitialModifier层数据
        WeaponDataManager.saveInitialModifierData(stack, weaponData);
        
        return true;
    }
}