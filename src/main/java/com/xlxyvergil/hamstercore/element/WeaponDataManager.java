package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.config.TacZWeaponConfig;
import com.xlxyvergil.hamstercore.config.SlashBladeWeaponConfig;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.xlxyvergil.hamstercore.element.InitialModifierEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 武器数据管理器
 * 负责管理WeaponData实例的生命周期，提供缓存机制以提高性能
 * 使用旧版类结构，但保持新的数据结构和使用方式
 */
public class WeaponDataManager {
    
    // NBT标签常量
    public static final String ELEMENT_DATA = "ElementData";
    
    // 缓存WeaponData实例，避免重复创建和解析
    private static final Map<String, WeaponData> weaponDataCache = new ConcurrentHashMap<>();
    
    /**
     * 获取物品堆的武器数据
     * 优先从NBT读取，如果NBT中不存在则从配置文件获取
     * 如果都不存在则返回一个新的空WeaponData实例
     * @param stack 物品堆
     * @return 武器数据
     */
    public static WeaponData getWeaponData(ItemStack stack) {
        if (stack.isEmpty()) {
            return new WeaponData();
        }
        
        // 首先尝试从NBT中读取
        WeaponData weaponData = readElementData(stack);
        if (weaponData != null) {
            return weaponData;
        }
        
        // 如果NBT中不存在，则从配置文件获取
        weaponData = getWeaponConfig(stack);
        if (weaponData != null) {
            // 将配置文件中的数据写入NBT
            saveElementData(stack, weaponData);
        } else {
            // 如果配置文件中也不存在，则创建一个新的空实例
            weaponData = new WeaponData();
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
            return new WeaponData();
        }
        
        // 从NBT中读取武器数据
        WeaponData weaponData = readElementData(stack);
        if (weaponData != null) {
            return weaponData;
        }
        
        // 如果NBT中没有数据，尝试从配置文件加载
        weaponData = getWeaponConfig(stack);
        if (weaponData != null) {
            saveElementData(stack, weaponData);
            return weaponData;
        }
        
        // 如果配置文件中也不存在，则创建一个新的空实例
        return new WeaponData();
    }
    
    /**
     * 获取物品的武器配置（根据物品类型选择不同的配置获取方法）
     * @param stack 物品堆
     * @return 武器数据
     */
    public static WeaponData getWeaponConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        // 判断物品类型并调用相应的配置获取方法
        if (net.minecraftforge.fml.ModList.get().isLoaded("tacz") && ModSpecialItemsFetcher.isTacZGun(stack)) {
            // TACZ枪械使用专门的配置获取方法
            return TacZWeaponConfig.getWeaponConfig(stack);
        } else if (net.minecraftforge.fml.ModList.get().isLoaded("slashblade") && SlashBladeItemsFetcher.isSlashBlade(stack)) {
            // 拔刀剑使用专门的配置获取方法
            return SlashBladeWeaponConfig.getWeaponConfig(stack);
        } else {
            // 普通物品使用通用方法
            return WeaponConfig.getWeaponConfig(stack);
        }
    }
    
    /**
     * 将武器元素数据保存到物品NBT中
     * @param stack 物品堆
     * @param data 武器数据
     */
    public static void saveElementData(ItemStack stack, WeaponData data) {
        if (stack.isEmpty() || data == null) {
            return;
        }
        
        // 将数据写入NBT
        CompoundTag nbt = data.toNBT();
        stack.getOrCreateTag().put(ELEMENT_DATA, nbt);
    }
    
    /**
     * 从物品中读取武器元素数据
     * @param stack 物品堆
     * @return 武器数据，如果不存在则返回null
     */
    public static WeaponData readElementData(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        // 检查物品是否有元素数据NBT
        if (!stack.hasTag() || !stack.getTag().contains(ELEMENT_DATA)) {
            return null;
        }
        
        // 从NBT中读取数据
        CompoundTag nbt = stack.getTag().getCompound(ELEMENT_DATA);
        return WeaponData.fromNBT(nbt);
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
    
    /**
     * 将武器初始修饰符数据保存到物品NBT中
     * @param stack 物品堆
     * @param data 武器数据
     */
    public static void saveInitialModifierData(ItemStack stack, WeaponData data) {
        if (stack.isEmpty() || data == null) {
            return;
        }
        
        // 创建一个新的WeaponData对象，只包含InitialModifier数据
        WeaponData initialModifierData = new WeaponData();
        initialModifierData.modid = data.modid;
        initialModifierData.itemId = data.itemId;
        initialModifierData.gunId = data.gunId;
        initialModifierData.translationKey = data.translationKey;
        
        // 复制InitialModifier数据
        for (InitialModifierEntry entry : data.getInitialModifiers()) {
            initialModifierData.addInitialModifier(entry);
        }
        
        // 将数据写入NBT
        CompoundTag nbt = initialModifierData.toNBT();
        stack.getOrCreateTag().put(ELEMENT_DATA, nbt);
    }
}