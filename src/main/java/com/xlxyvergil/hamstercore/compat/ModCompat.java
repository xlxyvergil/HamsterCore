package com.xlxyvergil.hamstercore.compat;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.DefaultAssets;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

/**
 * 兼容性工具类，基于ck目录中其他模组的最佳实践
 * 使用直接API调用，避免反射
 */
public class ModCompat {
    
    private static final String TACZ_MOD_ID = "tacz";
    private static final String SLASHBLADE_MOD_ID = "slashblade";
    
    /**
     * 检查TACZ是否已加载
     */
    public static boolean isTACZLoaded() {
        return ModList.get().isLoaded(TACZ_MOD_ID);
    }
    
    /**
     * 检查SlashBlade是否已加载
     */
    public static boolean isSlashBladeLoaded() {
        return ModList.get().isLoaded(SLASHBLADE_MOD_ID);
    }
    
    /**
     * 检查物品是否为TACZ枪械
     * 直接调用API，参考ck目录中其他模组的做法
     */
    public static boolean isGun(ItemStack stack) {
        if (!isTACZLoaded()) {
            return false;
        }
        
        try {
            // 直接调用API，不使用反射
            return IGun.getIGunOrNull(stack) != null;
        } catch (Exception e) {
            // API调用失败，返回false
            return false;
        }
    }
    
    /**
     * 检查物品是否为SlashBlade拔刀剑
     * 直接调用API，参考ck目录中其他模组的做法
     */
    public static boolean isSlashBlade(ItemStack stack) {
        if (!isSlashBladeLoaded()) {
            return false;
        }
        
        try {
            // 直接调用API，不使用反射
            return stack.getItem() instanceof ItemSlashBlade;
        } catch (Exception e) {
            // API调用失败，返回false
            return false;
        }
    }
    
    /**
     * 获取TACZ枪械的ID
     * 直接调用API获取，如果不是TACZ枪械或TACZ未加载，返回null
     */
    @Nullable
    public static String getGunId(ItemStack stack) {
        if (!isGun(stack)) {
            return null;
        }
        
        try {
            // 直接调用API，不使用反射
            IGun iGun = IGun.getIGunOrNull(stack);
            if (iGun != null) {
                ResourceLocation gunId = iGun.getGunId(stack);
                if (gunId != null && !isGunIdEmpty(gunId)) {
                    return gunId.toString();
                }
            }
        } catch (Exception e) {
            // API调用失败，返回null
        }
        
        return null;
    }
    
    /**
     * 检查枪械ID是否为空
     * 直接使用API常量，不使用反射
     */
    private static boolean isGunIdEmpty(ResourceLocation gunId) {
        try {
            // 直接使用API常量，不使用反射
            return gunId.equals(DefaultAssets.EMPTY_GUN_ID);
        } catch (Exception e) {
            // 如果API访问失败，使用字符串比较作为备用
            return "tacz:empty".equals(gunId.toString());
        }
    }
    
    /**
     * 获取SlashBlade的translationKey
     * 使用SlashBlade的Capability API获取真实的translationKey
     */
    @Nullable
    public static String getSlashBladeTranslationKey(ItemStack stack) {
        if (!isSlashBlade(stack)) {
            return null;
        }
        
        try {
            // 使用SlashBlade的Capability API
            var bladeState = stack.getCapability(mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE);
            if (bladeState.isPresent()) {
                String translationKey = bladeState.resolve().get().getTranslationKey();
                if (translationKey != null && !translationKey.isBlank()) {
                    return translationKey;
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