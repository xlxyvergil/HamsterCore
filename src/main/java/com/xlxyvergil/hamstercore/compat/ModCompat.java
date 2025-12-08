package com.xlxyvergil.hamstercore.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * 兼容性工具类，用于检查可选mod是否加载
 * 参考TACZ的kubejs兼容实现
 */
public class ModCompat {
    
    /**
     * 检查TACZ是否已加载
     */
    public static boolean isTACZLoaded() {
        return ModList.get().isLoaded("tacz");
    }
    
    /**
     * 检查SlashBlade是否已加载
     */
    public static boolean isSlashBladeLoaded() {
        return ModList.get().isLoaded("slashblade");
    }
    
    /**
     * 检查物品是否为TACZ枪械
     * 使用API检查
     */
    public static boolean isGun(ItemStack stack) {
        if (!isTACZLoaded()) {
            return false;
        }
        
        // 使用API检查
        try {
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            Object iGun = getIGunOrNull.invoke(null, stack);
            return iGun != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查物品是否为SlashBlade拔刀剑
     * 使用API检查
     */
    public static boolean isSlashBlade(ItemStack stack) {
        if (!isSlashBladeLoaded()) {
            return false;
        }
        
        // 使用API检查
        try {
            Class<?> itemSlashBladeClass = Class.forName("mods.flammpfeil.slashblade.item.ItemSlashBlade");
            return itemSlashBladeClass.isInstance(stack.getItem());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取TACZ枪械的ID
     * 使用API获取，如果不是TACZ枪械或TACZ未加载，返回null
     */
    @Nullable
    public static String getGunId(ItemStack stack) {
        if (!isGun(stack)) {
            return null;
        }
        
        // 使用API获取，参考TaczAttributeAdd的实现
        try {
            // 获取IGun实例
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            Object iGun = getIGunOrNull.invoke(null, stack);
            
            if (iGun != null) {
                // 调用getGunId方法
                Method getGunId = iGunClass.getMethod("getGunId", ItemStack.class);
                Object gunIdObj = getGunId.invoke(iGun, stack);
                
                if (gunIdObj instanceof ResourceLocation) {
                    ResourceLocation gunId = (ResourceLocation) gunIdObj;
                    
                    // 检查是否为空枪ID
                    if (!isGunIdEmpty(gunId)) {
                        return gunId.toString();
                    }
                }
            }
        } catch (Exception e) {
            // API调用失败，返回null
        }
        
        return null;
    }
    
    /**
     * 检查枪械ID是否为空
     */
    private static boolean isGunIdEmpty(ResourceLocation gunId) {
        try {
            // 获取EMPTY_GUN_ID常量进行比较
            Class<?> defaultAssetsClass = Class.forName("com.tacz.guns.api.DefaultAssets");
            Object emptyGunId = defaultAssetsClass.getField("EMPTY_GUN_ID").get(null);
            
            return gunId.equals(emptyGunId);
        } catch (Exception e) {
            // 如果比较失败，使用字符串比较
            return "tacz:empty".equals(gunId.toString());
        }
    }
    
    /**
     * 获取SlashBlade的translationKey
     * 使用API获取，如果不是SlashBlade或SlashBlade未加载，返回null
     */
    @Nullable
    public static String getSlashBladeTranslationKey(ItemStack stack) {
        if (!isSlashBlade(stack)) {
            return null;
        }
        
        // 直接使用API获取
        try {
            // 检查物品是否是ItemSlashBlade实例
            Class<?> itemSlashBladeClass = Class.forName("mods.flammpfeil.slashblade.item.ItemSlashBlade");
            
            // 如果是ItemSlashBlade实例，直接调用getDescriptionId方法
            if (itemSlashBladeClass.isInstance(stack.getItem())) {
                Method getDescriptionId = itemSlashBladeClass.getMethod("getDescriptionId", ItemStack.class);
                Object translationKey = getDescriptionId.invoke(stack.getItem(), stack);
                
                if (translationKey instanceof String && !((String) translationKey).isEmpty()) {
                    return (String) translationKey;
                }
            }
        } catch (Exception e) {
            // API调用失败，返回null
        }
        
        return null;
    }
    
    /**
     * 获取武器的类型名称
     * 返回"tacz"、"slashblade"或null
     */
    @Nullable
    public static String getWeaponType(ItemStack stack) {
        if (isGun(stack)) {
            return "tacz";
        }
        
        if (isSlashBlade(stack)) {
            return "slashblade";
        }
        
        return null;
    }
}