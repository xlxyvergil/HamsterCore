package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Map;

/**
 * 普通物品元素应用器
 * 专门处理普通物品（非拔刀剑非TACZ）的元素属性应用
 */
public class NormalItemElementApplier {
    
    /**
     * 应用普通物品的元素属性
     */
    public static int applyNormalItemsElements() {
        DebugLogger.log("开始应用普通物品元素属性...");
        
        // 确保配置已加载
        WeaponConfig.load();
        
        // 获取所有武器配置
        Map<ResourceLocation, WeaponData> allWeaponConfigs = WeaponConfig.getAllWeaponConfigs();
        DebugLogger.log("配置文件中共有 %d 个武器配置", allWeaponConfigs.size());
        
        int appliedCount = 0;
        int totalNormalItems = 0;
        int skippedModItems = 0;
        
        // 遍历所有配置，过滤掉MOD特殊物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : allWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 检查是否为MOD特殊物品
            boolean isModSpecialItem = isModSpecialItem(itemKey);
            
            if (isModSpecialItem) {
                skippedModItems++;
                DebugLogger.log("跳过MOD特殊物品: %s", itemKey.toString());
            } else {
                totalNormalItems++;
                DebugLogger.log("处理普通物品: %s", itemKey.toString());
                if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                    appliedCount++;
                }
            }
        }
        
        DebugLogger.log("统计信息: 总配置=%d, 普通物品=%d, 跳过MOD物品=%d, 成功应用=%d", 
                       allWeaponConfigs.size(), totalNormalItems, skippedModItems, appliedCount);
        
        DebugLogger.log("普通物品元素属性应用完成，处理了 %d 个普通物品", appliedCount);
        return appliedCount;
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
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                DebugLogger.log("普通物品 %s 在注册表中不存在", itemKey.toString());
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            // 确保物品栈有效
            if (stack.isEmpty()) {
                DebugLogger.log("普通物品 %s 创建的物品栈为空", itemKey.toString());
                return false;
            }
            
            // 直接使用从配置加载的WeaponElementData
            WeaponElementData elementData = weaponData.getElementData();
            
            // 确保elementData不为空
            if (elementData == null) {
                DebugLogger.log("普通物品 %s 的元素数据为空，创建新的", itemKey.toString());
                elementData = new WeaponElementData();
            } else {
                DebugLogger.log("普通物品 %s 的元素数据: Basic层数据=%d项", 
                              itemKey.toString(), elementData.getAllBasicElements().size());
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 将数据写入NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            DebugLogger.log("成功为普通物品 %s 应用元素属性", itemKey.toString());
            return true;
        } catch (Exception e) {
            DebugLogger.log("为普通物品 %s 应用元素属性时出错: %s", itemKey.toString(), e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}