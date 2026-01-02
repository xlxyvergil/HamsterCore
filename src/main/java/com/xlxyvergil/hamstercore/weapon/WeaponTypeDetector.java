package com.xlxyvergil.hamstercore.weapon;

import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import com.xlxyvergil.hamstercore.util.WeaponJudgeUtil;
import net.minecraft.world.item.ItemStack;

/**
 * 武器类型检测器
 * 用于检测物品的武器类型
 */
public class WeaponTypeDetector {
    
    /**
     * 检测物品的武器类型
     * @param stack 物品栈
     * @return 武器类型，如果不是武器则返回null
     */
    public static WeaponType detectWeaponType(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        
        // 首先检测是否是TACZ枪械
        if (ModSpecialItemsFetcher.isTaczLoaded()) {
            WeaponType tacZType = detectTacZWeaponType(stack);
            if (tacZType != null) {
                return tacZType;
            }
        }
        
        // 检测是否是拔刀剑
        if (isSlashBlade(stack)) {
            return WeaponType.MELEE;
        }
        
        // 检测是否是近战武器
        if (isMeleeWeapon(stack)) {
            return WeaponType.MELEE;
        }
        
        // 检测是否是远程武器（包括TACZ枪械和其他远程武器）
        if (isRangedWeapon(stack)) {
            return WeaponType.RANGED;
        }
        
        return null;
    }
    

    
    /**
     * 检测TACZ枪械的武器类型
     * @param stack 物品栈
     * @return TACZ枪械的武器类型，如果不是TACZ枪械则返回null
     */
    private static WeaponType detectTacZWeaponType(ItemStack stack) {
        if (!ModSpecialItemsFetcher.isTacZGun(stack)) {
            return null;
        }
        
        try {
            // 使用TimelessAPI获取CommonGunIndex
            com.tacz.guns.api.item.IGun iGun = com.tacz.guns.api.item.IGun.getIGunOrNull(stack);
            if (iGun == null) {
                return null;
            }
            
            // 获取枪械ID
            net.minecraft.resources.ResourceLocation gunId = iGun.getGunId(stack);
            if (gunId == null) {
                return null;
            }
            
            // 获取CommonGunIndex
            java.util.Optional<com.tacz.guns.resource.index.CommonGunIndex> gunIndexOpt = com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gunId);
            if (!gunIndexOpt.isPresent()) {
                return null;
            }
            
            // 获取武器类型
            String weaponType = gunIndexOpt.get().getType();
            if (weaponType == null) {
                return null;
            }
            
            // 根据武器类型名称返回对应的WeaponType
            switch (weaponType.toLowerCase()) {
                case "rifle":
                    return WeaponType.RIFLE;
                case "smg":
                    return WeaponType.SMG;
                case "lmg":
                    return WeaponType.LMG;
                case "rpg":
                    return WeaponType.RPG;
                case "shotgun":
                    return WeaponType.SHOTGUN;
                case "sniper":
                    return WeaponType.SNIPER;
                case "pistol":
                    return WeaponType.PISTOL;
                default:
                    return null;
            }
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            return null;
        } catch (Exception e) {
            // API调用失败，返回null
            return null;
        }
    }
    
    /**
     * 检测是否是拔刀剑
     * @param stack 物品栈
     * @return 是否是拔刀剑
     */
    private static boolean isSlashBlade(ItemStack stack) {
        return SlashBladeItemsFetcher.isSlashBlade(stack);
    }
    
    /**
     * 检测是否是近战武器
     * @param stack 物品栈
     * @return 是否是近战武器
     */
    private static boolean isMeleeWeapon(ItemStack stack) {
        // 使用WeaponJudgeUtil来检测近战武器
        return WeaponJudgeUtil.isMeleeWeapon(stack.getItem()) ||
               // 额外检测三叉戟，因为WeaponJudgeUtil中已经将其归类为近战武器
               stack.getItem() instanceof net.minecraft.world.item.TridentItem ||
               // 检测拔刀剑
               isSlashBlade(stack);
    }
    
    /**
     * 检测是否是远程武器
     * @param stack 物品栈
     * @return 是否是远程武器
     */
    private static boolean isRangedWeapon(ItemStack stack) {
        // 使用WeaponJudgeUtil来检测远程武器
        return WeaponJudgeUtil.isRangedWeapon(stack.getItem());
    }
    
    /**
     * 检测物品是否属于指定的武器分类
     * @param stack 物品栈
     * @param category 武器分类
     * @return 是否属于指定的武器分类
     */
    public static boolean isWeaponCategory(ItemStack stack, WeaponCategory category) {
        WeaponType type = detectWeaponType(stack);
        return type != null && type.getCategory() == category;
    }
    
    /**
     * 检测物品是否属于指定的武器类型
     * @param stack 物品栈
     * @param type 武器类型
     * @return 是否属于指定的武器类型
     */
    public static boolean isWeaponType(ItemStack stack, WeaponType type) {
        return detectWeaponType(stack) == type;
    }
    
    /**
     * 检测TACZ是否加载
     * @return TACZ是否加载
     */
    public static boolean isTacZLoaded() {
        return ModSpecialItemsFetcher.isTaczLoaded();
    }
}
