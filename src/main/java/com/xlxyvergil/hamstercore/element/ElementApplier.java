package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Map;
import java.util.Set;

/**
 * 元素应用器，用于在服务器启动时将配置文件中的元素数据应用到武器上
 * 使用新的四层数据结构：Basic、Computed、Usage、Extra
 */
public class ElementApplier {
    
    /**
     * 从配置文件应用元素属性到武器
     */
    public static void applyElementsFromConfig() {
        DebugLogger.log("开始应用元素属性到武器...");
        
        // 确保配置已加载
        WeaponConfig.load();
        
        // 应用MOD特殊物品的元素属性
        applyModSpecialItemsElements();
        
        // 应用普通物品的元素属性
        applyNormalItemsElements();
        
        DebugLogger.log("元素属性应用完成");
    }
    
    /**
     * 应用MOD特殊物品的元素属性
     */
    private static void applyModSpecialItemsElements() {
        DebugLogger.log("开始应用MOD特殊物品元素属性...");
        
        // 应用TACZ枪械元素属性 - 使用具体gunId
        int tacZAppliedCount = 0;
        if (ModList.get().isLoaded("tacz")) {
            for (ResourceLocation gunId : ModSpecialItemsFetcher.getTacZGunIDs()) {
                if (applyGunAttributes(gunId)) {
                    tacZAppliedCount++;
                }
            }
        }
        
        // 应用拔刀剑元素属性 - 使用具体translationKey
        int slashBladeAppliedCount = 0;
        if (ModList.get().isLoaded("slashblade")) {
            for (String translationKey : SlashBladeItemsFetcher.getSlashBladeTranslationKeys()) {
                if (applySlashBladeAttributes(translationKey)) {
                    slashBladeAppliedCount++;
                }
            }
        }
        
        DebugLogger.log("MOD特殊物品元素属性应用完成，TACZ枪械: %d, 拔刀剑: %d", 
                               tacZAppliedCount, slashBladeAppliedCount);
    }
    
    /**
     * 应用普通物品的元素属性
     */
    private static void applyNormalItemsElements() {
        DebugLogger.log("开始应用普通物品元素属性...");
        
        // 获取所有武器配置
        Map<ResourceLocation, WeaponData> allWeaponConfigs = WeaponConfig.getAllWeaponConfigs();
        int normalAppliedCount = 0;
        
        // 遍历所有配置，过滤掉MOD特殊物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : allWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 检查是否为MOD特殊物品
            boolean isModSpecialItem = isModSpecialItem(itemKey);
            
            // 如果不是MOD特殊物品，则应用元素属性
            if (!isModSpecialItem) {
                if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                    normalAppliedCount++;
                }
            }
        }
        
        DebugLogger.log("普通物品元素属性应用完成，处理了 %d 个普通物品", normalAppliedCount);
    }
    
    /**
     * 判断是否为MOD特殊物品
     */
    private static boolean isModSpecialItem(ResourceLocation itemKey) {
        String itemKeyStr = itemKey.toString();
        
        // 检查是否为TACZ枪械（使用统一ID）
        if (ModList.get().isLoaded("tacz")) {
            if ("tacz:modern_kinetic_gun".equals(itemKeyStr)) {
                return true;
            }
        }
        
        // 检查是否为拔刀剑（使用统一ID）
        if (ModList.get().isLoaded("slashblade")) {
            if ("slashblade:slashblade".equals(itemKeyStr)) {
                return true;
            }
        }
        
        return false;
    }
    

    
    /**
     * 为枪械应用属性
     */
    private static boolean applyGunAttributes(ResourceLocation gunId) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByGunId(gunId.toString());
            if (weaponData == null) {
                DebugLogger.log("无法找到枪械 %s 的配置数据", gunId.toString());
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用tacz:modern_kinetic_gun
                itemKey = new ResourceLocation("tacz", "modern_kinetic_gun");
            }
            
            ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                DebugLogger.log("无法创建物品栈: %s", itemKey.toString());
                return false;
            }
            
            // 设置gunId到NBT
            stack.getOrCreateTag().putString("gunId", gunId.toString());
            
            // 复制元素数据
            WeaponElementData elementData = weaponData.getElementData().copy();
            if (elementData == null) {
                elementData = new WeaponElementData();
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 保存到NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            return true;
        } catch (Exception e) {
            DebugLogger.log("为枪械 %s 应用属性失败: %s", gunId.toString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 为拔刀剑应用属性
     */
    private static boolean applySlashBladeAttributes(String translationKey) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByTranslationKey(translationKey);
            if (weaponData == null) {
                DebugLogger.log("无法找到拔刀剑 %s 的配置数据", translationKey);
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用slashblade:slashblade
                itemKey = new ResourceLocation("slashblade", "slashblade");
            }
            
            ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                DebugLogger.log("无法创建物品栈: %s", itemKey.toString());
                return false;
            }
            
            // 设置translationKey到NBT
            stack.getOrCreateTag().putString("translationKey", translationKey);
            
            // 复制元素数据
            WeaponElementData elementData = weaponData.getElementData().copy();
            if (elementData == null) {
                elementData = new WeaponElementData();
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 保存到NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            return true;
        } catch (Exception e) {
            DebugLogger.log("为拔刀剑 %s 应用属性失败: %s", translationKey, e.getMessage());
            return false;
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
            ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey));
            
            // 确保物品栈有效
            if (stack.isEmpty()) {
                return false;
            }
            
            // 直接使用从配置加载的WeaponElementData
            WeaponElementData elementData = weaponData.getElementData();
            
            // 确保elementData不为空
            if (elementData == null) {
                elementData = new WeaponElementData();
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 将数据写入NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            DebugLogger.log("成功为普通物品 %s 应用元素属性，Basic层数据: %d项", 
                          itemKey.toString(), elementData.getAllBasicElements().size());
            return true;
        } catch (Exception e) {
            DebugLogger.log("为普通物品 %s 应用元素属性时出错: %s", itemKey.toString(), e.getMessage());
            return false;
        }
    }
}