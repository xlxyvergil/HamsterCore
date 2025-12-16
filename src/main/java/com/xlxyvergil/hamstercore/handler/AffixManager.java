package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class AffixManager {
    /**
     * 添加词缀
     */
    public static void addAffix(ItemStack stack, String name, String elementType, double amount, String operation, String source) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        UUID uuid = UUID.randomUUID();
        InitialModifierEntry entry = new InitialModifierEntry(name, elementType, amount, operation, uuid, source);
        weaponData.addInitialModifier(entry);
        
        // 为基础元素添加basic条目，用于元素复合
        if (ElementType.byName(elementType) != null && ElementType.byName(elementType).isBasic()) {
            weaponData.addBasicElement(elementType, source, (int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 修改词缀
     */
    public static void modifyAffix(ItemStack stack, UUID affixUuid, double newAmount) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            if (entry.getUuid().equals(affixUuid)) {
                // 创建新的条目替换旧条目
                InitialModifierEntry newEntry = new InitialModifierEntry(
                    entry.getName(),
                    entry.getElementType(),
                    newAmount,
                    entry.getOperation(),
                    entry.getUuid(),
                    entry.getSource()
                );
                weaponData.getInitialModifiers().remove(entry);
                weaponData.addInitialModifier(newEntry);
                break;
            }
        }
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 删除词缀
     */
    public static void removeAffix(ItemStack stack, UUID affixUuid) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        weaponData.getInitialModifiers().removeIf(entry -> entry.getUuid().equals(affixUuid));
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 批量操作词缀
     */
    public static void batchAddAffixes(ItemStack stack, List<InitialModifierEntry> entries) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        entries.forEach(weaponData::addInitialModifier);
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
}