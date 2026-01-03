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
        weaponData.removeBasicElement(elementType);
    }
    
    /**
     * 检查是否需要从Basic层移除指定类型的元素
     * 如果该类型元素在InitialModifiers中完全不存在，则需要从Basic层移除
     */
    private static boolean shouldRemoveFromBasicLayer(WeaponData weaponData, String elementType) {
        boolean stillExists = hasElementTypeInInitialModifiers(weaponData, elementType);
        return !stillExists;
    }
    
    /**
     * 添加词缀
     */
    public static void addAffix(ItemStack stack, String name, String elementType, double amount, String operation, UUID uuid, String source) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 确保elementType包含命名空间，name保持原始名称
        String namespacedElementType = elementType.contains(":") ? elementType : "hamstercore:" + elementType;
        
        // 检查是否已经存在相同UUID和相同elementType的词缀，如果存在则不添加，避免重复赋予
        for (InitialModifierEntry existingEntry : weaponData.getInitialModifiers()) {
            if (existingEntry.getUuid().equals(uuid) && existingEntry.getElementType().equals(namespacedElementType)) {
                // 已经存在相同UUID和相同elementType的词缀，直接返回，不重复添加
                return;
            }
        }
        
        // 检查是否是该类型第一次加入InitialModifier
        boolean isFirstTime = !hasElementTypeInInitialModifiers(weaponData, namespacedElementType);
        
        // 创建初始修饰符条目：name是原始名称，elementType是带命名空间的完整名称
        InitialModifierEntry entry = new InitialModifierEntry(name, namespacedElementType, amount, operation, uuid, source);
        weaponData.addInitialModifier(entry);
        
        // 只有基础元素和复合元素在第一次加入时才添加到Basic层
        if (isFirstTime) {
            // 使用ElementType.byName直接判断是否为基础元素或复合元素
            ElementType type = ElementType.byName(name);
            if (type != null && (type.isBasic() || type.isComplex())) {
                // 使用名称作为Basic层的类型，保持一致
                int order = weaponData.getBasicElements().size();
                weaponData.addBasicElement(name, source, order);
            }
        }
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
        // 计算并存储元素值到NBT
        ElementCalculationCoordinator.INSTANCE.calculateAndStoreElements(stack, weaponData);
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
        
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
        // 计算并存储元素值到NBT
        ElementCalculationCoordinator.INSTANCE.calculateAndStoreElements(stack, weaponData);
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
            boolean shouldRemove = shouldRemoveFromBasicLayer(weaponData, removedElementType);
            if (shouldRemove) {
                // 从removedElementType中提取简单名称（不带命名空间）
                String simpleName = removedElementType;
                if (simpleName.contains(":")) {
                    simpleName = simpleName.substring(simpleName.lastIndexOf(":") + 1);
                }
                
                // 使用ElementType.byName直接判断是否为基础元素或复合元素
                ElementType type = ElementType.byName(simpleName);
                if (type != null && (type.isBasic() || type.isComplex())) {
                    removeFromBasicLayer(weaponData, simpleName);
                    
                    // 额外的日志记录，用于调试
                    System.out.println("从Basic层移除了元素: " + simpleName);
                }
            }
        }
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
        // 计算并存储元素值到NBT
        ElementCalculationCoordinator.INSTANCE.calculateAndStoreElements(stack, weaponData);
    }


}