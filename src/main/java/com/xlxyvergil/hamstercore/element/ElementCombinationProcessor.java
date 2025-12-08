package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 元素组合处理器，负责处理基础元素组合成复合元素的逻辑
 * 同时处理物理元素和特殊属性的计算
 */
public class ElementCombinationProcessor {
    
    /**
     * 处理元素组合逻辑，返回最终元素集合（包含生效状态）
     */
    public static List<ElementInstance> processElementCombinations(List<ElementInstance> directElements) {
        return processElementCombinations(directElements, null);
    }
    
    /**
     * 处理元素组合逻辑，返回最终元素集合（包含生效状态）
     * @param directElements 直接设置的元素列表
     * @param stack 关联的物品堆（可选，用于获取默认值）
     */
    public static List<ElementInstance> processElementCombinations(List<ElementInstance> directElements, ItemStack stack) {
        // 按位置排序
        List<ElementInstance> sortedElements = directElements.stream()
            .sorted(Comparator.comparingInt(ElementInstance::getPosition))
            .collect(Collectors.toList());
        
        // 分离不同类型元素，保持顺序
        List<ElementInstance> physicalElements = new ArrayList<>(); // 物理元素
        List<ElementInstance> basicElements = new ArrayList<>();    // 基础元素
        Map<ElementType, ElementInstance> directComplexElements = new HashMap<>(); // 复合元素
        Map<ElementType, ElementInstance> specialElements = new HashMap<>();       // 特殊属性
        
        for (ElementInstance element : sortedElements) {
            if (element.getType().isPhysical()) {
                physicalElements.add(element);
            } else if (element.getType().isBasic()) {
                basicElements.add(element);
            } else if (element.getType().isComplex()) {
                directComplexElements.put(element.getType(), element);
            } else if (element.getType().isSpecial()) {
                specialElements.put(element.getType(), element);
            }
        }
        
        // 获取武器数据（用于默认值）
        WeaponData weaponData = null;
        if (stack != null) {
            weaponData = WeaponDataManager.getInstance().getOrCreateWeaponData(stack);
        }
        
        // 1. 按顺序计算复合元素组合（包括直接设置的和武器默认的）
        Map<ElementType, ElementInstance> computedComplexElements = 
            computeComplexCombinationsInOrder(basicElements, weaponData);
        
        // 2. 确定哪些基础元素被组合使用
        Set<Integer> usedBasicElementPositions = findUsedBasicElementPositions(
            basicElements, computedComplexElements);
        
        // 3. 构建最终元素列表
        List<ElementInstance> finalElements = new ArrayList<>();
        
        // 添加物理元素（计算后生效）
        finalElements.addAll(computePhysicalElements(physicalElements, weaponData));
        
        // 添加基础元素，更新生效状态
        finalElements.addAll(computeBasicElements(basicElements, usedBasicElementPositions, weaponData));
        
        // 合并直接设置的复合元素和计算得出的复合元素
        for (ElementType complexType : ElementType.getComplexElements()) {
            ElementInstance directComplex = directComplexElements.get(complexType);
            ElementInstance computedComplex = computedComplexElements.get(complexType);
            
            if (directComplex != null && computedComplex != null) {
                // 两者都存在，合并数值
                double mergedValue = directComplex.getValue() + computedComplex.getValue();
                
                ElementInstance mergedElement = new ElementInstance(
                    directComplex.getType(),
                    mergedValue,
                    Math.min(directComplex.getPosition(), computedComplex.getPosition()), // 使用最小位置
                    true  // 合并后的元素总是生效的
                );
                
                finalElements.add(mergedElement);
            } else if (directComplex != null) {
                finalElements.add(directComplex);
            } else if (computedComplex != null) {
                finalElements.add(computedComplex);
            }
        }
        
        // 添加特殊属性（暴击率、暴击伤害、触发率）- 计算后生效
        finalElements.addAll(computeSpecialElements(specialElements, weaponData));
        
        // 按位置重新排序
        finalElements.sort(Comparator.comparingInt(ElementInstance::getPosition));
        return finalElements;
    }
    
    /**
     * 计算物理元素的值
     * 如果没有设置物理元素，则使用默认值
     * 如果设置了多个相同的物理元素，则将其值相加
     */
    private static List<ElementInstance> computePhysicalElements(List<ElementInstance> physicalElements, WeaponData weaponData) {
        Map<ElementType, Double> physicalValues = new HashMap<>();
        
        // 收集所有物理元素的值
        for (ElementInstance element : physicalElements) {
            ElementType type = element.getType();
            double currentValue = physicalValues.getOrDefault(type, 0.0);
            physicalValues.put(type, currentValue + element.getValue());
        }
        
        // 添加默认值（如果未设置）
        for (ElementType physicalType : ElementType.getPhysicalElements()) {
            if (!physicalValues.containsKey(physicalType)) {
                double defaultValue = 0.0;
                if (weaponData != null) {
                    defaultValue = weaponData.getDefaultPhysicalValue(physicalType);
                }
                if (defaultValue > 0.0) {
                    physicalValues.put(physicalType, defaultValue);
                }
            }
        }
        
        // 创建计算后的物理元素列表
        List<ElementInstance> result = new ArrayList<>();
        int position = 0; // 物理元素使用固定位置
        
        for (Map.Entry<ElementType, Double> entry : physicalValues.entrySet()) {
            ElementType type = entry.getKey();
            double value = entry.getValue();
            
            ElementInstance computedElement = new ElementInstance(
                type,
                value,
                position++,
                true // 物理元素始终生效
            );
            
            result.add(computedElement);
        }
        
        return result;
    }
    
    /**
     * 计算基础元素的值
     * 如果没有设置基础元素，则使用默认值
     */
    private static List<ElementInstance> computeBasicElements(List<ElementInstance> basicElements, 
                                                              Set<Integer> usedBasicElementPositions,
                                                              WeaponData weaponData) {
        List<ElementInstance> result = new ArrayList<>();
        
        // 先添加已设置的基础元素
        for (ElementInstance element : basicElements) {
            boolean isActive = !usedBasicElementPositions.contains(element.getPosition());
            result.add(element.withActiveState(isActive));
        }
        
        // 添加未设置但有默认值的基础元素
        if (weaponData != null) {
            int position = 200; // 基础元素使用固定位置偏移
            
            for (ElementType basicType : ElementType.getBasicElements()) {
                // 检查是否已经有该类型的基础元素
                boolean hasElement = false;
                for (ElementInstance element : basicElements) {
                    if (element.getType() == basicType) {
                        hasElement = true;
                        break;
                    }
                }
                
                // 如果没有该类型的基础元素，但有默认值，则添加
                if (!hasElement) {
                    double defaultValue = weaponData.getDefaultBasicValue(basicType);
                    if (defaultValue > 0.0) {
                        ElementInstance defaultElement = new ElementInstance(
                            basicType,
                            defaultValue,
                            position++,
                            true // 默认基础元素始终生效
                        );
                        result.add(defaultElement);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 计算特殊属性的值（暴击率、暴击伤害、触发率）
     * 如果没有设置特殊属性，则使用默认值
     * 如果设置了多个相同的特殊属性，则将其值相加
     */
    private static List<ElementInstance> computeSpecialElements(Map<ElementType, ElementInstance> specialElements, WeaponData weaponData) {
        List<ElementInstance> result = new ArrayList<>();
        int position = 1000; // 特殊属性使用固定位置偏移
        
        // 处理暴击率
        ElementInstance criticalChanceElement = specialElements.get(ElementType.CRITICAL_CHANCE);
        double criticalChance = 0.0; // 默认值
        if (weaponData != null) {
            criticalChance = weaponData.getCriticalChance();
        }
        if (criticalChanceElement != null) {
            criticalChance = criticalChanceElement.getValue();
        }
        
        result.add(new ElementInstance(
            ElementType.CRITICAL_CHANCE,
            criticalChance,
            position++,
            true // 特殊属性始终生效
        ));
        
        // 处理暴击伤害
        ElementInstance criticalDamageElement = specialElements.get(ElementType.CRITICAL_DAMAGE);
        double criticalDamage = 0.0; // 默认值
        if (weaponData != null) {
            criticalDamage = weaponData.getCriticalDamage();
        }
        if (criticalDamageElement != null) {
            criticalDamage = criticalDamageElement.getValue();
        }
        
        result.add(new ElementInstance(
            ElementType.CRITICAL_DAMAGE,
            criticalDamage,
            position++,
            true // 特殊属性始终生效
        ));
        
        // 处理触发率
        ElementInstance triggerChanceElement = specialElements.get(ElementType.TRIGGER_CHANCE);
        double triggerChance = 0.1; // 默认值 10%
        if (weaponData != null) {
            triggerChance = weaponData.getTriggerChance();
        }
        if (triggerChanceElement != null) {
            triggerChance = triggerChanceElement.getValue();
        }
        
        result.add(new ElementInstance(
            ElementType.TRIGGER_CHANCE,
            triggerChance,
            position++,
            true // 特殊属性始终生效
        ));
        
        return result;
    }
    
    /**
     * 按照元素顺序计算复合元素组合
     * 规则：
     * 1. 元素的顺序决定了元素伤害组合
     * 2. 按照顺序，从上到下，依次复合
     * 3. 武器固有元素伤害则在上述计算完成后再组合添加进去
     * 4. 如果使用多个相同元素类型，最先使用的那个元素决定了该类型基础元素进行复合的次序
     * 5. 本身含复合元素伤害的武器或被其他方式赋予了复合后的元素，保留复合元素，可组合特殊组合（如病毒+腐蚀）
     * 6. 爆炸（火焰+冰冻），腐蚀（电击+毒素），毒气（火焰+毒素），磁力（冰冻+电击），辐射（火焰+电击），病毒（冰冻+毒素）
     * 7. 参与复合的基础元素，效果将不生效
     */
    private static Map<ElementType, ElementInstance> computeComplexCombinationsInOrder(
            List<ElementInstance> basicElements, WeaponData weaponData) {
        
        Map<ElementType, ElementInstance> result = new HashMap<>();
        Set<ElementType> usedTypes = new HashSet<>(); // 记录已使用的元素类型
        
        // 按顺序处理基础元素（直接设置的元素）
        for (int i = 0; i < basicElements.size(); i++) {
            ElementInstance elem1 = basicElements.get(i);
            ElementType type1 = elem1.getType();
            
            // 如果该类型的元素已经被使用过，则跳过
            if (usedTypes.contains(type1)) {
                continue;
            }
            
            // 查找后续不同类型的元素
            for (int j = i + 1; j < basicElements.size(); j++) {
                ElementInstance elem2 = basicElements.get(j);
                ElementType type2 = elem2.getType();
                
                // 如果该类型的元素已经被使用过，则跳过
                if (usedTypes.contains(type2)) {
                    continue;
                }
                
                // 尝试组合这两个元素
                ElementType complex = ElementType.createComplex(type1, type2);
                if (complex != null) {
                    // 计算复合元素的值：取两个元素的值之和
                    double complexValue = elem1.getValue() + elem2.getValue();
                    
                    // 确定新复合元素的位置（取两个基础元素中的较小位置）
                    int newPosition = Math.min(elem1.getPosition(), elem2.getPosition());
                    
                    ElementInstance complexElement = new ElementInstance(
                        complex, 
                        complexValue, 
                        newPosition,
                        true  // 计算得出的复合元素默认生效
                    );
                    
                    result.put(complex, complexElement);
                    
                    // 标记这两个元素类型已被使用
                    usedTypes.add(type1);
                    usedTypes.add(type2);
                    
                    // 找到组合后跳出内层循环，继续处理下一个未使用的元素
                    break;
                }
            }
        }
        
        // 处理武器默认的基础元素与直接设置的基础元素的组合
        if (weaponData != null) {
            int position = 500; // 复合元素使用固定位置偏移
            
            // 获取所有武器默认的基础元素
            Map<ElementType, Double> defaultBasicValues = new HashMap<>();
            for (ElementType basicType : ElementType.getBasicElements()) {
                double defaultValue = weaponData.getDefaultBasicValue(basicType);
                if (defaultValue > 0.0) {
                    defaultBasicValues.put(basicType, defaultValue);
                }
            }
            
            // 按照规则尝试组合所有可能的元素对（直接设置的元素优先）
            List<ElementInstance> allBasicElements = new ArrayList<>();
            // 先添加直接设置的未使用的元素
            for (ElementInstance element : basicElements) {
                if (!usedTypes.contains(element.getType())) {
                    allBasicElements.add(element);
                }
            }
            // 再添加默认的基础元素
            int defaultPosition = 300; // 默认基础元素位置
            for (Map.Entry<ElementType, Double> entry : defaultBasicValues.entrySet()) {
                boolean alreadyExists = false;
                for (ElementInstance element : basicElements) {
                    if (element.getType() == entry.getKey() && !usedTypes.contains(element.getType())) {
                        alreadyExists = true;
                        break;
                    }
                }
                
                if (!alreadyExists) {
                    ElementInstance defaultElement = new ElementInstance(
                        entry.getKey(),
                        entry.getValue(),
                        defaultPosition++,
                        true
                    );
                    allBasicElements.add(defaultElement);
                }
            }
            
            // 继续处理剩余的组合可能性
            for (int i = 0; i < allBasicElements.size(); i++) {
                ElementInstance elem1 = allBasicElements.get(i);
                ElementType type1 = elem1.getType();
                
                // 如果该类型的元素已经被使用过，则跳过
                if (usedTypes.contains(type1)) {
                    continue;
                }
                
                // 查找后续不同类型的元素
                for (int j = i + 1; j < allBasicElements.size(); j++) {
                    ElementInstance elem2 = allBasicElements.get(j);
                    ElementType type2 = elem2.getType();
                    
                    // 如果该类型的元素已经被使用过，则跳过
                    if (usedTypes.contains(type2)) {
                        continue;
                    }
                    
                    // 尝试组合这两个元素
                    ElementType complex = ElementType.createComplex(type1, type2);
                    if (complex != null && !result.containsKey(complex)) {
                        // 计算复合元素的值：取两个元素的值之和
                        double complexValue = elem1.getValue() + elem2.getValue();
                        
                        // 确定新复合元素的位置（取两个基础元素中的较小位置）
                        int newPosition = Math.min(elem1.getPosition(), elem2.getPosition());
                        
                        ElementInstance complexElement = new ElementInstance(
                            complex, 
                            complexValue, 
                            newPosition,
                            true  // 计算得出的复合元素默认生效
                        );
                        
                        result.put(complex, complexElement);
                        
                        // 标记这两个元素类型已被使用
                        usedTypes.add(type1);
                        usedTypes.add(type2);
                        
                        // 找到组合后跳出内层循环，继续处理下一个未使用的元素
                        break;
                    }
                }
            }
        }
        
        // 添加默认的复合元素（直接设置的复合元素默认值）
        if (weaponData != null) {
            int position = 600; // 默认复合元素使用固定位置偏移
            
            for (ElementType complexType : ElementType.getComplexElements()) {
                boolean hasElement = false;
                // 检查是否已经存在该类型的复合元素（无论是直接设置还是计算得出）
                if (result.containsKey(complexType)) {
                    hasElement = true;
                } else {
                    // 检查是否直接设置了该类型的复合元素
                    for (ElementInstance element : basicElements) {
                        if (element.getType() == complexType) {
                            hasElement = true;
                            break;
                        }
                    }
                }
                
                // 如果没有该类型的复合元素，但有默认值，则添加
                if (!hasElement) {
                    double defaultValue = weaponData.getDefaultComplexValue(complexType);
                    if (defaultValue > 0.0) {
                        ElementInstance defaultElement = new ElementInstance(
                            complexType,
                            defaultValue,
                            position++,
                            true // 默认复合元素始终生效
                        );
                        result.put(complexType, defaultElement);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 找出被组合使用的基础元素的位置
     */
    private static Set<Integer> findUsedBasicElementPositions(
            List<ElementInstance> basicElements,
            Map<ElementType, ElementInstance> complexElements) {
        
        Set<Integer> usedPositions = new HashSet<>();
        Set<ElementType> usedTypes = new HashSet<>();
        
        // 收集所有复合元素使用的元素类型
        for (ElementInstance complexElement : complexElements.values()) {
            List<ElementType> composition = complexElement.getType().getComposition();
            if (composition != null && composition.size() == 2) {
                usedTypes.addAll(composition);
            }
        }
        
        // 标记这些类型的所有元素为已使用
        for (ElementInstance element : basicElements) {
            if (usedTypes.contains(element.getType())) {
                usedPositions.add(element.getPosition());
            }
        }
        
        return usedPositions;
    }
}