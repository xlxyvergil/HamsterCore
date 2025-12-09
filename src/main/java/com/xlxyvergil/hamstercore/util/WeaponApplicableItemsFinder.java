package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * 武器适用物品查找器，专门负责遍历物品并筛选出可应用元素属性的物品
 */
public class WeaponApplicableItemsFinder {
    
    /**
     * 遍历所有物品并找出可应用元素属性的物品
     * @return 包含所有可应用元素属性物品的集合
     */
    public static Set<ResourceLocation> findApplicableItems() {
        Set<ResourceLocation> applicableItems = new HashSet<>();
        
        // 遍历所有已注册的物品
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            if (itemKey == null) continue;
            
            ItemStack stack = new ItemStack(item);
            
            // 检查物品是否可以应用元素属性
            if (WeaponApplicableItemsChecker.canApplyElements(stack)) {
                applicableItems.add(itemKey);
            }
        }
        
        return applicableItems;
    }
}