package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拔刀剑配置应用类
 * 处理拔刀剑的NBT应用
 */
public class SlashBladeConfigApplier {

    /**
     * 应用拔刀剑的配置
     *
     * @return 成功应用配置的拔刀剑数量
     */
    public static int applyConfigs() {
        int appliedCount = 0;

        try {
            // 确保配置已加载
            SlashBladeWeaponConfig.loadSlashBladeConfigFile();

            // 检查拔刀剑模组是否已加载
            if (!SlashBladeItemsFetcher.isSlashBladeLoaded()) {
                return 0;
            }

            // 获取所有拔刀剑配置
            Map<String, List<WeaponData>> slashBladeConfigs = SlashBladeWeaponConfig.getSlashBladeConfigs();
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
                if (applyElementAttributesToSlashBlade(translationKey, weaponData)) {
                    appliedCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appliedCount;
    }

    /**
     * 为拔刀剑应用元素属性
     */
    private static boolean applyElementAttributesToSlashBlade(String translationKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }

        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            SlashBladeWeaponConfig.cacheSlashBladeConfig(translationKey, weaponData);
            
            // 获取拔刀剑物品并保存元素数据到NBT
            Item slashBladeItem = getSlashBladeItem();
            if (slashBladeItem != null) {
                ItemStack stack = new ItemStack(slashBladeItem);
                // 在物品NBT中存储一个标识符，用于识别这是拔刀剑
                stack.getOrCreateTag().putString("TranslationKey", translationKey);
                WeaponDataManager.saveElementData(stack, weaponData);
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