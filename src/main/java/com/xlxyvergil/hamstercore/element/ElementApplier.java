package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.handler.ElementModifierEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.fml.ModList;

import java.util.*;

/**
 * 元素应用器
 * 负责将配置文件中的元素数据应用到物品上
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
            for (ResourceLocation gunId : com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher.getTacZGunIDs()) {
                if (applyGunAttributes(gunId)) {
                    tacZAppliedCount++;
                }
            }
         }
        
        // 应用拔刀剑元素属性 - 使用具体translationKey
        int slashBladeAppliedCount = 0;
         if (ModList.get().isLoaded("slashblade")) {
            // 确保拔刀剑物品获取器已初始化
            com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher.init();
            for (String translationKey : com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher.getSlashBladeTranslationKeys()) {
                if (applySlashBladeAttributes(translationKey)) {
                    slashBladeAppliedCount++;
                }
            }
         }
        
        return tacZAppliedCount + slashBladeAppliedCount;
    }

    

    
    
    
    
    /**
     * 应用元素修饰符到物品
     * 根据配置文件中的元素属性修饰符，在Basic层里存储修饰符的元素类型、排序以及是否是CONFIG
     */
    public static void applyElementModifiers(ItemStack stack, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        // 同时将Basic层数据保存到NBT中
        WeaponData weaponData = new WeaponData();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                weaponData.addBasicElement(basicEntry.getType(), basicEntry.getSource(), basicEntry.getOrder());
            }
        }
        
        // 保存元素数据到NBT（只保存Basic层）
        WeaponDataManager.saveElementDataWithoutUsage(stack, weaponData);
    }
    
    /**
     * 为枪械应用属性
     * 适配新的两层数据结构
     */
    private static boolean applyGunAttributes(ResourceLocation gunId) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = com.xlxyvergil.hamstercore.config.WeaponConfig.getWeaponConfigByGunId(gunId.toString());
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
            
            ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置gunId到NBT
            stack.getOrCreateTag().putString("gunId", gunId.toString());
            
            // 应用元素修饰符到物品（只处理Basic层）
            applyElementModifiers(stack, weaponData.getBasicElements());
            
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
            WeaponData weaponData = com.xlxyvergil.hamstercore.config.WeaponConfig.getWeaponConfigByTranslationKey(translationKey);
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
            
            ItemStack stack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey));
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置translationKey到NBT
            stack.getOrCreateTag().putString("translationKey", translationKey);
            
            // 应用元素修饰符到物品（只处理Basic层）
            applyElementModifiers(stack, weaponData.getBasicElements());
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 应用元素数据到物品
     * @param stack 目标物品
     * @param weaponData 武器数据
     */
    public static void applyElements(ItemStack stack, WeaponData weaponData) {
        // 确保物品有NBT标签
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        
        CompoundTag elementData = new CompoundTag();
        
        // 应用基础元素数据
        CompoundTag basicTag = new CompoundTag();
        Map<String, List<WeaponData.BasicEntry>> basicElements = weaponData.getBasicElements();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            ListTag listTag = new ListTag();
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                CompoundTag basicEntryTag = new CompoundTag();
                basicEntryTag.putString("type", basicEntry.getType());
                basicEntryTag.putString("source", basicEntry.getSource());
                basicEntryTag.putInt("order", basicEntry.getOrder());
                listTag.add(basicEntryTag);
            }
            basicTag.put(entry.getKey(), listTag);
        }
        elementData.put("basic", basicTag);
        
        // 应用使用层元素数据
        CompoundTag usageTag = new CompoundTag();
        Map<String, Double> usageElements = weaponData.getUsageElements();
        for (Map.Entry<String, Double> entry : usageElements.entrySet()) {
            usageTag.putDouble(entry.getKey(), entry.getValue());
        }
        elementData.put("usage", usageTag);
        
        // 将元素数据保存到物品NBT中
        stack.getTag().put("element_data", elementData);
    }
}