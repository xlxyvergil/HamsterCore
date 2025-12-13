package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.ElementModifierUtil;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MOD特殊物品元素应用器，专门处理TACZ枪械和拔刀剑的元素属性应用
 * 使用新的两层数据结构：Basic、Usage
 */
public class ElementApplier {
    
    /**
     * 应用MOD特殊物品的元素属性
     * @return 应用的物品数量
     */
    public static int applyModSpecialItemsElements() {
        // 应用TACZ枪械元素属性 - 使用具体gunId
        int tacZAppliedCount = 0;
         if (ModList.get().isLoaded("tacz")) {
            for (ResourceLocation gunId : ModSpecialItemsFetcher.getTacZGunIDs()) {
                if (applyGunAttributes(gunId)) {
                    tacZAppliedCount++;
                }
            }
         }
        
        // 应用拔刀剑元素属性 - 使用具体translationKey
        int slashBladeAppliedCount = 0;
         if (ModList.get().isLoaded("slashblade")) {
            // 确保拔刀剑物品获取器已初始化
            SlashBladeItemsFetcher.init();
            for (String translationKey : SlashBladeItemsFetcher.getSlashBladeTranslationKeys()) {
                if (applySlashBladeAttributes(translationKey)) {
                    slashBladeAppliedCount++;
                }
            }
         }
        
        return tacZAppliedCount + slashBladeAppliedCount;
    }

    

    
    
    
    

    /**
     * 应用元素修饰符到物品
     * 使用新的两层数据结构
     */
    public static void applyElementModifiers(ItemStack stack, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        ElementModifierUtil.applyElementModifiers(stack, basicElements);
    }
    
    /**
     * 为枪械应用属性
     * 适配新的两层数据结构
     */
    private static boolean applyGunAttributes(ResourceLocation gunId) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByGunId(gunId.toString());
            if (weaponData == null) {
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用tacz:modern_kinetic_gun
                itemKey = new ResourceLocation("tacz", "modern_kinetic_gun");
            }
            
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置gunId到NBT
            stack.getOrCreateTag().putString("gunId", gunId.toString());
            
            // 应用元素修饰符到物品
            applyElementModifiers(stack, weaponData.getBasicElements());
            
            // 保存元素数据到NBT（只保存Basic层和Usage层）
            WeaponDataManager.saveElementData(stack, weaponData);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 为拔刀剑应用属性
     * 适配新的两层数据结构
     */
    private static boolean applySlashBladeAttributes(String translationKey) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByTranslationKey(translationKey);
            if (weaponData == null) {
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用slashblade:slashblade
                itemKey = new ResourceLocation("slashblade", "slashblade");
            }
            
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置translationKey到NBT
            stack.getOrCreateTag().putString("translationKey", translationKey);
            
            // 应用元素修饰符到物品
            applyElementModifiers(stack, weaponData.getBasicElements());
            
            // 保存元素数据到NBT（只保存Basic层和Usage层）
            WeaponDataManager.saveElementData(stack, weaponData);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}