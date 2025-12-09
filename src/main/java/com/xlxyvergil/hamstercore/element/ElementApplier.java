package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

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
        WeaponConfig weaponConfig = WeaponConfig.getInstance();
        if (weaponConfig == null) {
            DebugLogger.log("武器配置未初始化，跳过元素属性应用");
            return;
        }
        
        // 应用MOD特殊物品的元素属性
        applyModSpecialItemsElements(weaponConfig);
        
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
     * 为MOD特殊物品应用元素属性
     * 使用新的四层数据结构
     */
    private static boolean applyElementAttributesToModSpecialItem(WeaponConfig weaponConfig, ResourceLocation itemKey) {
        // 使用itemKey直接从配置中获取武器数据
        WeaponConfig.WeaponData weaponData = weaponConfig.getAllWeaponConfigs().get(itemKey);
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 创建一个虚拟的ItemStack用于存储元素属性
            ItemStack dummyStack = new ItemStack(net.minecraft.world.item.Items.AIR);
            
            // 使用新的数据结构设置属性
            WeaponElementData elementData = new WeaponElementData();
            
            // 从WeaponData获取elementData并复制到新的数据结构
            if (weaponData.elementData != null) {
                // 复制Basic层数据（配置文件中的基础数据）
                for (String type : weaponData.elementData.getAllBasicElements().keySet()) {
                    BasicEntry entry = weaponData.elementData.getBasicElement(type);
                    if (entry != null) {
                        elementData.addBasicElement(type, entry.getValue(), entry.getSource());
                    }
                }
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(dummyStack, elementData);
            
            // 将数据写入NBT
            WeaponDataManager.saveElementData(dummyStack, elementData);
            
            DebugLogger.log("成功为MOD特殊物品 %s 应用元素属性，Basic层数据: %d项", 
                          itemKey.toString(), elementData.getAllBasicElements().size());
            return true;
        } catch (Exception e) {
            DebugLogger.log("为MOD特殊物品 %s 应用元素属性时出错: %s", itemKey.toString(), e.getMessage());
            return false;
        }
    }
}