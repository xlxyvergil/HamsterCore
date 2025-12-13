package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 武器数据管理器
 * 负责管理WeaponData实例的生命周期，提供缓存机制以提高性能
 */
public class WeaponDataManager {
    
    // 缓存WeaponData实例，避免重复创建和解析
    private static final Map<String, WeaponData> weaponDataCache = new ConcurrentHashMap<>();
    
    /**
     * 获取物品堆的武器数据
     * 优先从NBT读取，如果NBT中不存在则从配置文件获取
     * @param stack 物品堆
     * @return 武器数据
     */
    public static WeaponData getWeaponData(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        // 首先尝试从NBT中读取
        WeaponData weaponData = WeaponDataNBTUtil.readWeaponDataFromNBT(stack);
        if (weaponData != null) {
            return weaponData;
        }
        
        // 如果NBT中不存在，则从配置文件获取
        weaponData = WeaponConfig.getWeaponConfig(stack);
        if (weaponData != null) {
            // 将配置文件中的数据写入NBT
            WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
        }
        
        return weaponData;
    }
    
    /**
     * 从物品中加载元素数据（专门用于事件处理）
     * @param stack 物品堆
     * @return 武器数据
     */
    public static WeaponData loadElementData(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        // 从NBT中读取武器数据
        WeaponData weaponData = WeaponDataNBTUtil.readWeaponDataFromNBT(stack);
        if (weaponData == null) {
            return null;
        }
        
        // 计算Usage层数据
        weaponData.computeUsageData(stack);
        
        return weaponData;
    }
    
    /**
     * 获取缓存的武器数据
     * @param key 缓存键
     * @return 武器数据，如果不存在则返回null
     */
    public static WeaponData getCachedWeaponData(String key) {
        return weaponDataCache.get(key);
    }
    
    /**
     * 将武器数据添加到缓存
     * @param key 缓存键
     * @param weaponData 武器数据
     */
    public static void cacheWeaponData(String key, WeaponData weaponData) {
        if (key != null && weaponData != null) {
            weaponDataCache.put(key, weaponData);
        }
    }
    
    /**
     * 从缓存中移除武器数据
     * @param key 缓存键
     */
    public static void removeCachedWeaponData(String key) {
        weaponDataCache.remove(key);
    }
    
    /**
     * 清空缓存
     */
    public static void clearCache() {
        weaponDataCache.clear();
    }
}