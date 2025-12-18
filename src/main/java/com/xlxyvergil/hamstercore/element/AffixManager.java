package com.xlxyvergil.hamstercore.element;


import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    
    /**
     * 批量操作词缀
     */
    public static void batchAddAffixes(ItemStack stack, List<InitialModifierEntry> entries) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 对要添加的元素进行排序
        List<InitialModifierEntry> sortedEntries = new ArrayList<>(entries);
        // 自定义排序逻辑：基础元素按冰、电、火、毒顺序，其余按字母顺序
        sortedEntries.sort((entry1, entry2) -> {
            String type1 = entry1.getElementType();
            String type2 = entry2.getElementType();
            
            // 定义基础元素的排序顺序
            Map<String, Integer> basicElementOrder = new LinkedHashMap<>();
            basicElementOrder.put("cold", 0);     // 冰
            basicElementOrder.put("electricity", 1); // 电
            basicElementOrder.put("heat", 2);      // 火
            basicElementOrder.put("toxin", 3);     // 毒
            
            // 检查是否都是基础元素
            boolean isBasic1 = basicElementOrder.containsKey(type1);
            boolean isBasic2 = basicElementOrder.containsKey(type2);
            
            if (isBasic1 && isBasic2) {
                // 都是基础元素，按照预定义顺序排序
                return Integer.compare(basicElementOrder.get(type1), basicElementOrder.get(type2));
            } else if (isBasic1) {
                // 只有第一个是基础元素，排在前面
                return -1;
            } else if (isBasic2) {
                // 只有第二个是基础元素，排在前面
                return 1;
            } else {
                // 都不是基础元素，按照字母顺序排序
                return type1.compareTo(type2);
            }
        });
        
        // 收集所有第一次加入的元素类型及其索引
        Map<String, Integer> firstTimeElements = new LinkedHashMap<>();
        int order = 0;
        
        // 第一步：收集所有第一次加入的元素类型
        for (InitialModifierEntry entry : sortedEntries) {
            String elementType = entry.getElementType();
            if (!hasElementTypeInInitialModifiers(weaponData, elementType)) {
                firstTimeElements.put(elementType, order++);
            }
        }
        
        // 第二步：逐个添加词缀
        for (InitialModifierEntry entry : sortedEntries) {
            String elementType = entry.getElementType();
            
            // 检查是否是该类型第一次加入InitialModifier
            boolean isFirstTime = firstTimeElements.containsKey(elementType);
            
            // 添加到InitialModifier
            weaponData.addInitialModifier(entry);
            
            // 只有基础元素和复合元素在第一次加入时才添加到Basic层
            if (isFirstTime) {
                ElementType type = ElementType.byName(elementType);
                if (type != null && (type.isBasic() || type.isComplex())) {
                    weaponData.addBasicElement(elementType, entry.getSource(), firstTimeElements.get(elementType));
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
    
    /**
     * 批量删除词缀
     */
    public static void batchRemoveAffixes(ItemStack stack, List<UUID> uuids) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 收集所有需要检查的元素类型
        for (UUID uuid : uuids) {
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
        }
        
        // 显式保存WeaponData到NBT
        WeaponDataManager.saveElementData(stack, weaponData);
        
        // 计算并缓存元素值
        ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
        // 失效AffixManager的临时缓存
        AffixManagerCache.invalidateCache(stack);
    }
}