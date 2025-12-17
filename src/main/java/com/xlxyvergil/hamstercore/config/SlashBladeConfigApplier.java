package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.xlxyvergil.hamstercore.element.ElementType;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 拔刀剑配置应用类
 * 处理拔刀剑的NBT应用
 */
public class SlashBladeConfigApplier {

    /**
     * 加载拔刀剑配置并应用到物品
     * 在onServerStarted事件中调用此方法
     */
    public static void load() {
        applyConfigs();
    }

    /**
     * 应用拔刀剑的配置
     *
     * @return 成功应用配置的拔刀剑数量
     */
    public static int applyConfigs() {
        int appliedCount = 0;

        try {

            // 检查拔刀剑模组是否已加载
            if (!SlashBladeItemsFetcher.isSlashBladeLoaded()) {
                return 0;
            }

            // 获取所有拔刀剑配置（从配置文件加载）
            Map<String, List<WeaponData>> slashBladeConfigs = loadSlashBladeConfigs();
            if (slashBladeConfigs == null || slashBladeConfigs.isEmpty()) {
                return 0;
            }

            // 获取所有拔刀剑translationKey
            Set<String> translationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys();
            if (translationKeys == null || translationKeys.isEmpty()) {
                return 0;
            }

            // 为每个拔刀剑应用元素属性
            for (String translationKey : translationKeys) {
                // 根据translationKey获取对应的配置数据
                List<WeaponData> weaponDataList = slashBladeConfigs.get(translationKey);
                if (weaponDataList == null || weaponDataList.isEmpty()) {
                    continue;
                }

                // 使用第一个配置数据（通常只有一个）
                WeaponData weaponData = weaponDataList.get(0);
                if (weaponData == null) {
                    continue;
                }

                // 应用元素属性到拔刀剑
                if (applyConfigToSlashBlade(translationKey, weaponData)) {
                    appliedCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appliedCount;
    }

    /**
     * 从配置文件加载拔刀剑配置
     */
    private static Map<String, List<WeaponData>> loadSlashBladeConfigs() {
        Map<String, List<WeaponData>> slashBladeConfigs = new HashMap<>();
        
        File configFile = new File(SlashBladeWeaponConfig.SLASHBLADE_WEAPONS_FILE);
        if (!configFile.exists()) {
            return slashBladeConfigs;
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
                            
                            // 处理单个武器配置
                            WeaponData weaponData = processWeaponConfig(itemJson, configKey);
                            if (weaponData != null && weaponData.translationKey != null) {
                                // 将配置添加到映射中
                                slashBladeConfigs.computeIfAbsent(weaponData.translationKey, k -> new ArrayList<>()).add(weaponData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return slashBladeConfigs;
    }
    
    /**
     * 处理单个武器配置（数组格式，用于拔刀剑）
     * 适配新的两层数据结构
     */
    private static WeaponData processWeaponConfig(JsonObject itemJson, String configKey) {
        // 创建WeaponData对象
        WeaponData weaponData = new WeaponData();
        
        // 读取translationKey（如果存在）
        if (itemJson.has("translationKey")) {
            weaponData.translationKey = itemJson.get("translationKey").getAsString();
        }
        
        // 读取elementData
        if (itemJson.has("elementData")) {
            JsonObject elementDataJson = itemJson.getAsJsonObject("elementData");
                        
            // 不再读取Basic层
                        
            // 不再读取Usage层
                        
            // 读取初始属性数据
            if (elementDataJson.has("InitialModifiers")) {
                JsonArray modifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                for (JsonElement modifierElement : modifiersArray) {
                    JsonObject modifierJson = modifierElement.getAsJsonObject();
                                
                    String name = modifierJson.get("name").getAsString();
                    double amount = modifierJson.get("amount").getAsDouble();
                    String operationStr = modifierJson.get("operation").getAsString();
                                
                    // UUID将在应用阶段生成
                    UUID uuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                
                    // 添加到初始属性列表
                    weaponData.addInitialModifier(new InitialModifierEntry(name, name, amount, operationStr, uuid, "def"));
                    
                    // 只有基础元素和复合元素才添加到Basic层
                    ElementType type = ElementType.byName(name);
                    if (type != null && (type.getTypeCategory() == ElementType.TypeCategory.BASIC || type.getTypeCategory() == ElementType.TypeCategory.COMPLEX)) {
                        weaponData.addBasicElement(name, "def", 0);
                    }
                }
            }
        }
        
        return weaponData;
    }

    /**
     * 为拔刀剑应用配置
     */
    private static boolean applyConfigToSlashBlade(String translationKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }

        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            SlashBladeWeaponConfig.cacheSlashBladeConfig(translationKey, weaponData);
            
            // 根据translationKey获取具体的拔刀剑物品并应用配置
            Item slashBladeItem = getSlashBladeItem();
            if (slashBladeItem != null) {
                // 创建具有特定translationKey的拔刀剑物品堆
                ItemStack stack = new ItemStack(slashBladeItem);
                // 在物品NBT中存储translationKey，用于识别具体是哪把刀
                stack.getOrCreateTag().putString("TranslationKey", translationKey);
                // 只保存InitialModifier层数据
                WeaponDataManager.saveInitialModifierData(stack, weaponData);
                // 将配置好的物品保存到全局映射中，供游戏运行时使用
                SlashBladeItemsFetcher.cacheSlashBladeStack(translationKey, stack);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取拔刀剑物品实例
     * @return 拔刀剑物品实例
     */
    private static Item getSlashBladeItem() {
        // 检查拔刀剑模组是否已加载
        if (!SlashBladeItemsFetcher.isSlashBladeLoaded()) {
            return null;
        }
        
        try {
            // 使用SlashBladeItemsFetcher中已有的方法获取物品
            // 这样避免了在SlashBlade未加载时直接引用其类导致的ClassNotFoundException
            return SlashBladeItemsFetcher.getSlashBladeItem();
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明SlashBlade未正确加载
            return null;
        } catch (Exception e) {
            // 其他异常
            return null;
        }
    }
}