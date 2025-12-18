package com.xlxyvergil.hamstercore.element;


import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class AffixManager {
    /**
     * 检查InitialModifier中是否已存在指定类型的元素
     */
    private static boolean hasElementTypeInInitialModifiers(WeaponData weaponData, String elementType) {
        return weaponData.getInitialModifiers().stream()
                .anyMatch(entry -> entry.getElementType().equals(elementType));
    }
    
    /**
     * 从Basic层移除指定类型的元素
     */
    private static void removeFromBasicLayer(WeaponData weaponData, String elementType) {
        weaponData.getBasicElements().remove(elementType);
    }
    
    /**
     * 添加词缀
     */
    public static void addAffix(ItemStack stack, String name, String elementType, double amount, String operation, UUID uuid, String source) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 检查是否是该类型第一次加入InitialModifier
        boolean isFirstTime = !hasElementTypeInInitialModifiers(weaponData, elementType);
        
        InitialModifierEntry entry = new InitialModifierEntry(name, elementType, amount, operation, uuid, source);
        weaponData.addInitialModifier(entry);
        
        // 只有基础元素和复合元素在第一次加入时才添加到Basic层
        if (isFirstTime) {
            ElementType type = ElementType.byName(elementType);
            if (type != null && (type.isBasic() || type.isComplex())) {
                // 使用当前Basic层元素的数量作为order值，保证唯一性和递增性
                int order = weaponData.getBasicElements().size();
                weaponData.addBasicElement(elementType, source, order);
            }
        }
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 计算并缓存元素值
        ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
    }
    
    /**
     * 修改词缀
     */
    public static void modifyAffix(ItemStack stack, UUID uuid, double newAmount) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            if (entry.getUuid().equals(uuid)) {
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
        
        // modify操作不会影响Basic层，因为元素类型没有改变，只是数值改变
        // Basic层只记录元素类型的存在和顺序，不记录具体数值
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 计算并缓存元素值
        ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
    }
    
    /**
     * 删除词缀
     */
    public static void removeAffix(ItemStack stack, UUID uuid) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 获取要删除的条目的元素类型
        String removedElementType = null;
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            if (entry.getUuid().equals(uuid)) {
                removedElementType = entry.getElementType();
                break;
            }
        }
        
        // 删除词缀
        weaponData.getInitialModifiers().removeIf(entry -> entry.getUuid().equals(uuid));
        
        // 检查是否需要从Basic层移除（只有当该类型的元素完全不存在时才移除）
        if (removedElementType != null) {
            boolean stillExists = hasElementTypeInInitialModifiers(weaponData, removedElementType);
            if (!stillExists) {
                ElementType type = ElementType.byName(removedElementType);
                if (type != null && (type.isBasic() || type.isComplex())) {
                    removeFromBasicLayer(weaponData, removedElementType);
                }
            }
        }
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 计算并缓存元素值
        ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
    }


}