package com.xlxyvergil.hamstercore.weapon;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 武器分类辅助类
 * 用于改装件系统中检查改装件是否可以应用到特定武器上
 */
public class WeaponCategoryHelper {
    
    /**
     * 检查改装件是否可以应用到特定武器上
     * @param modificationCategory 改装件适用的武器类型列表
     * @param weaponStack 武器物品栈
     * @return 是否可以应用
     */
    public static boolean canApplyModification(List<WeaponType> modificationCategory, ItemStack weaponStack) {
        if (modificationCategory == null || modificationCategory.isEmpty()) {
            // 如果改装件没有指定适用类型，则可以应用到所有武器上
            return true;
        }
        
        // 检测武器类型
        WeaponType weaponType = WeaponTypeDetector.detectWeaponType(weaponStack);
        if (weaponType == null) {
            // 不是武器，不能应用
            return false;
        }
        
        // 检查改装件是否可以应用到该武器类型上
        return modificationCategory.contains(weaponType);
    }
    
    /**
     * 检查改装件是否可以应用到特定武器分类上
     * @param modificationCategory 改装件适用的武器分类
     * @param weaponStack 武器物品栈
     * @return 是否可以应用
     */
    public static boolean canApplyModification(WeaponCategory modificationCategory, ItemStack weaponStack) {
        // 检测武器类型
        WeaponType weaponType = WeaponTypeDetector.detectWeaponType(weaponStack);
        if (weaponType == null) {
            // 不是武器，不能应用
            return false;
        }
        
        // 检查改装件是否可以应用到该武器分类上
        return modificationCategory.allowsWeaponType(weaponType);
    }
    
    /**
     * 检查武器是否属于指定的武器分类
     * @param stack 物品栈
     * @param category 武器分类
     * @return 是否属于指定分类
     */
    public static boolean isWeaponInCategory(ItemStack stack, WeaponCategory category) {
        WeaponType type = WeaponTypeDetector.detectWeaponType(stack);
        return type != null && type.getCategory() == category;
    }
    
    /**
     * 检查武器是否属于指定的武器类型
     * @param stack 物品栈
     * @param type 武器类型
     * @return 是否属于指定类型
     */
    public static boolean isWeaponOfType(ItemStack stack, WeaponType type) {
        return WeaponTypeDetector.detectWeaponType(stack) == type;
    }
    
    /**
     * 获取武器的分类显示名称
     * @param stack 物品栈
     * @return 分类显示名称，如果不是武器则返回null
     */
    public static String getWeaponCategoryDisplayName(ItemStack stack) {
        WeaponType type = WeaponTypeDetector.detectWeaponType(stack);
        if (type == null) {
            return null;
        }
        return type.getCategory().getDisplayName();
    }
    
    /**
     * 获取武器的类型显示名称
     * @param stack 物品栈
     * @return 类型显示名称，如果不是武器则返回null
     */
    public static String getWeaponTypeDisplayName(ItemStack stack) {
        WeaponType type = WeaponTypeDetector.detectWeaponType(stack);
        if (type == null) {
            return null;
        }
        return type.getDisplayName();
    }
    
    /**
     * 检查物品是否是武器
     * @param stack 物品栈
     * @return 是否是武器
     */
    public static boolean isWeapon(ItemStack stack) {
        return WeaponTypeDetector.detectWeaponType(stack) != null;
    }
}
