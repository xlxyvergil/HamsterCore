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
        
        // 获取武器配置
        WeaponConfig weaponConfig = new WeaponConfig();
        
        // 应用MOD特殊物品的元素属性
        applyModSpecialItemsElements(weaponConfig);
        
        // 应用普通物品的元素属性
        applyNormalItemsElements(weaponConfig);
        
        DebugLogger.log("元素属性应用完成");
    }
    
    /**
     * 应用MOD特殊物品的元素属性
     */
    private static void applyModSpecialItemsElements(WeaponConfig weaponConfig) {
        DebugLogger.log("开始应用MOD特殊物品元素属性...");
        
        // 应用TACZ枪械元素属性
        int tacZAppliedCount = 0;
        if (ModList.get().isLoaded("tacz")) {
            for (ResourceLocation gunId : ModSpecialItemsFetcher.getTacZGunIDs()) {
                if (applyElementAttributesToModSpecialItem(weaponConfig, gunId)) {
                    tacZAppliedCount++;
                }
            }
        }
        
        // 应用拔刀剑元素属性
        int slashBladeAppliedCount = 0;
        if (ModList.get().isLoaded("slashblade")) {
            for (ResourceLocation bladeId : SlashBladeItemsFetcher.getSlashBladeIDs()) {
                if (applyElementAttributesToModSpecialItem(weaponConfig, bladeId)) {
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
    private static void applyNormalItemsElements(WeaponConfig weaponConfig) {
        DebugLogger.log("开始应用普通物品元素属性...");
        
        // 获取所有武器配置
        Map<ResourceLocation, WeaponData> allWeaponConfigs = weaponConfig.getAllWeaponConfigs();
        int normalAppliedCount = 0;
        
        // 遍历所有配置，过滤掉MOD特殊物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : allWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 检查是否为MOD特殊物品
            boolean isModSpecialItem = isModSpecialItem(itemKey);
            
            // 如果不是MOD特殊物品，则应用元素属性
            if (!isModSpecialItem) {
                if (applyElementAttributesToNormalItem(weaponConfig, itemKey, weaponData)) {
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
        // 检查是否为TACZ枪械
        if (ModList.get().isLoaded("tacz")) {
            Set<ResourceLocation> tacZGunIDs = ModSpecialItemsFetcher.getTacZGunIDs();
            if (tacZGunIDs.contains(itemKey)) {
                return true;
            }
        }
        
        // 检查是否为拔刀剑
        if (ModList.get().isLoaded("slashblade")) {
            Set<ResourceLocation> slashBladeIDs = SlashBladeItemsFetcher.getSlashBladeIDs();
            if (slashBladeIDs.contains(itemKey)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 为MOD特殊物品应用元素属性
     * 使用新的四层数据结构
     */
    private static boolean applyElementAttributesToModSpecialItem(WeaponConfig weaponConfig, ResourceLocation itemKey) {
        // 使用itemKey直接从配置中获取武器数据
        WeaponData weaponData = weaponConfig.getAllWeaponConfigs().get(itemKey);
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
            
            DebugLogger.log("成功为MOD特殊物品 %s 应用元素属性，Basic层数据: %d项", 
                          itemKey.toString(), elementData.getAllBasicElements().size());
            return true;
        } catch (Exception e) {
            DebugLogger.log("为MOD特殊物品 %s 应用元素属性时出错: %s", itemKey.toString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 为普通物品应用元素属性
     * 使用新的四层数据结构
     */
    private static boolean applyElementAttributesToNormalItem(WeaponConfig weaponConfig, ResourceLocation itemKey, WeaponData weaponData) {
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