package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TACZ配置应用类
 * 处理TACZ枪械的NBT应用
 */
public class TacZConfigApplier {

    private static final String TACZ_MOD_ID = "tacz";

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
            // 确保配置已加载
            TacZWeaponConfig.loadTacZConfigFile();

            // 获取所有TACZ枪械配置
            Map<String, List<WeaponData>> tacZConfigs = TacZWeaponConfig.getTacZGunConfigs();
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

                // 应用元素属性到TACZ枪械
                if (applyElementAttributesToTacZGun(gunId, weaponData)) {
                    appliedCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appliedCount;
    }

    /**
     * 为TACZ枪械应用元素属性
     */
    private static boolean applyElementAttributesToTacZGun(ResourceLocation gunId, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }

        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            TacZWeaponConfig.cacheTacZGunConfig(gunId.toString(), weaponData);
            
            // 获取TACZ枪械物品并保存元素数据到NBT
            Item tacZGunItem = getTacZGunItem();
            if (tacZGunItem != null) {
                ItemStack stack = new ItemStack(tacZGunItem);
                // 在物品NBT中存储一个标识符，用于识别这是TACZ枪械
                stack.getOrCreateTag().putString("GunId", gunId.toString());
                WeaponDataManager.saveElementData(stack, weaponData);
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