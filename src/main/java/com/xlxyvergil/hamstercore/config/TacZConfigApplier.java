package com.xlxyvergil.hamstercore.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
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
 * TACZ配置应用类
 * 处理TACZ枪械的NBT应用
 */
public class TacZConfigApplier {

    private static final String TACZ_MOD_ID = "tacz";

    /**
     * 加载TACZ配置并应用到物品
     * 在onServerStarted事件中调用此方法
     */
    public static void load() {
        applyConfigs();
    }

    /**
     * 应用TACZ的配置
     *
     * @return 成功应用配置的TACZ枪械数量
     */
    public static int applyConfigs() {
        // 检查TACZ模组是否已加载
        if (!ModList.get().isLoaded(TACZ_MOD_ID)) {
            return 0;
        }

        int appliedCount = 0;

        try {
            // 获取所有TACZ枪械配置（从配置文件加载）
            Map<String, List<WeaponData>> tacZConfigs = loadTacZGunConfigs();
            if (tacZConfigs == null || tacZConfigs.isEmpty()) {
                return 0;
            }

            // 获取所有TACZ gunId
            Set<ResourceLocation> gunIds = ModSpecialItemsFetcher.getTacZGunIDs();
            if (gunIds == null || gunIds.isEmpty()) {
                return 0;
            }

            // 为每个TACZ枪械应用元素属性
            for (ResourceLocation gunId : gunIds) {
                // 根据gunId获取对应的配置数据
                List<WeaponData> weaponDataList = tacZConfigs.get(gunId.toString());
                if (weaponDataList == null || weaponDataList.isEmpty()) {
                    continue;
                }

                // 使用第一个配置数据（通常只有一个）
                WeaponData weaponData = weaponDataList.get(0);
                if (weaponData == null) {
                    continue;
                }

                // 应用配置到TACZ枪械
                if (applyConfigToTacZGun(gunId, weaponData)) {
                    appliedCount++;
                }
            }
            
            // 注意：WeaponItemIds现在通过统一的初始化系统收集所有ID，不需要手动添加
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appliedCount;
    }

    /**
     * 从配置文件加载TACZ枪械配置
     */
    private static Map<String, List<WeaponData>> loadTacZGunConfigs() {
        Map<String, List<WeaponData>> tacZConfigs = new HashMap<>();
        
        File configFile = new File(TacZWeaponConfig.TACZ_WEAPONS_FILE);
        if (!configFile.exists()) {
            return tacZConfigs;
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
                            if (weaponData != null && weaponData.gunId != null) {
                                // 将配置添加到映射中
                                tacZConfigs.computeIfAbsent(weaponData.gunId, k -> new ArrayList<>()).add(weaponData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return tacZConfigs;
    }
    
    /**
     * 处理单个武器配置（数组格式，用于TACZ）
     * 适配新的两层数据结构
     */
    private static WeaponData processWeaponConfig(JsonObject itemJson, String configKey) {
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
                        
            // 读取初始属性属性数据
            if (elementDataJson.has("InitialModifiers")) {
                JsonArray modifiersArray = elementDataJson.getAsJsonArray("InitialModifiers");
                for (JsonElement modifierElement : modifiersArray) {
                    JsonObject modifierJson = modifierElement.getAsJsonObject();
                                 
                    String name = modifierJson.get("name").getAsString();
                    double amount = modifierJson.get("amount").getAsDouble();
                    String operationStr = modifierJson.get("operation").getAsString();
                                 
                    // UUID将在应用阶段生成
                    UUID uuid = UUID.nameUUIDFromBytes(("hamstercore:" + name).getBytes());
                                 
                    // 直接添加初始属性
                    // 确保elementType包含命名空间，name保持原始名称
                    String namespacedElementType = name.contains(":") ? name : "hamstercore:" + name;
                    weaponData.addInitialModifier(new InitialModifierEntry(name, namespacedElementType, amount, operationStr, uuid, "def"));
                    
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
     * 为TACZ枪械应用配置
     */
    private static boolean applyConfigToTacZGun(ResourceLocation gunId, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }

        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            TacZWeaponConfig.cacheTacZGunConfig(gunId.toString(), weaponData);
            
            // 根据gunId获取具体的TACZ枪械物品并应用配置
            Item tacZGunItem = getTacZGunItem();
            if (tacZGunItem != null) {
                // 创建具有特定gunId的TACZ枪械物品堆
                ItemStack stack = new ItemStack(tacZGunItem);
                // 在物品NBT中存储gunId，用于识别具体是哪把枪
                stack.getOrCreateTag().putString("GunId", gunId.toString());
                // 只保存InitialModifier层数据
                WeaponDataManager.saveInitialModifierData(stack, weaponData);
                // 将配置好的物品保存到全局映射中，供游戏运行时使用
                ModSpecialItemsFetcher.cacheTaczGunStack(gunId, stack);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取TACZ枪械物品实例
     * @return TACZ枪械物品实例
     */
    private static Item getTacZGunItem() {
        // 检查TACZ模组是否已加载
        if (!ModList.get().isLoaded(TACZ_MOD_ID)) {
            return null;
        }
        
        try {
            // 使用ModSpecialItemsFetcher中已有的方法获取物品
            // 这样避免了在TACZ未加载时直接引用其类导致的ClassNotFoundException
            return ModSpecialItemsFetcher.getTaczGunItem();
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            return null;
        } catch (Exception e) {
            // 其他异常
            return null;
        }
    }
}