package com.xlxyvergil.hamstercore.element;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 元素组合处理器，负责处理基础元素组合成复合元素的逻辑
 */
public class ElementCombinationProcessor {
    
    /**
     * 处理元素组合逻辑，返回最终元素集合（包含生效状态）
     */
    public static List<ElementInstance> processElementCombinations(List<ElementInstance> directElements) {
        // 按位置排序
        List<ElementInstance> sortedElements = directElements.stream()
            .sorted(Comparator.comparingInt(ElementInstance::getPosition))
            .collect(Collectors.toList());
        
        // 分离基础元素和复合元素
        Map<ElementType, ElementInstance> basicElements = new HashMap<>();
        Map<ElementType, ElementInstance> directComplexElements = new HashMap<>();
        
        for (ElementInstance element : sortedElements) {
            if (element.getType().isBasic()) {
                basicElements.put(element.getType(), element);
            } else if (element.getType().isComplex()) {
                directComplexElements.put(element.getType(), element);
            }
        }
        
        // 1. 计算可能的组合复合元素
        Map<ElementType, ElementInstance> computedComplexElements = 
            computeComplexCombinations(basicElements);
        
        // 2. 确定哪些基础元素被组合使用
        Set<Integer> usedBasicElementPositions = findUsedBasicElementPositions(
            basicElements, computedComplexElements);
        
        // 3. 构建最终元素列表
        List<ElementInstance> finalElements = new ArrayList<>();
        
        // 添加基础元素，更新生效状态
        for (ElementInstance element : basicElements.values()) {
            boolean isActive = !usedBasicElementPositions.contains(element.getPosition());
            finalElements.add(element.withActiveState(isActive));
        }
        
        // 合并直接设置的复合元素和计算得出的复合元素
        for (ElementType complexType : ElementType.getComplexElements()) {
            ElementInstance directComplex = directComplexElements.get(complexType);
            ElementInstance computedComplex = computedComplexElements.get(complexType);
            
            if (directComplex != null && computedComplex != null) {
                // 两者都存在，合并数值
                double mergedValue = directComplex.getValue() + computedComplex.getValue();
                ElementSource mergedSource = ElementSource.MERGED;
                
                ElementInstance mergedElement = new ElementInstance(
                    directComplex.getType(),
                    mergedValue,
                    directComplex.getPosition(),
                    true,  // 合并后的元素总是生效的
                    mergedSource
                );
                
                finalElements.add(mergedElement);
            } else if (directComplex != null) {
                finalElements.add(directComplex);
            } else if (computedComplex != null) {
                finalElements.add(computedComplex);
            }
        }
        
        // 按位置重新排序
        finalElements.sort(Comparator.comparingInt(ElementInstance::getPosition));
        return finalElements;
    }
    
    /**
     * 计算基础元素可能形成的复合组合
     * 考虑组合顺序敏感性
     */
    private static Map<ElementType, ElementInstance> computeComplexCombinations(
            Map<ElementType, ElementInstance> basicElements) {
        
        Map<ElementType, ElementInstance> result = new HashMap<>();
        List<ElementType> basicList = new ArrayList<>(basicElements.keySet());
        
        // 尝试所有可能的组合顺序
        for (int i = 0; i < basicList.size(); i++) {
            for (int j = i + 1; j < basicList.size(); j++) {
                ElementType elem1 = basicList.get(i);
                ElementType elem2 = basicList.get(j);
                
                // 尝试两个可能的组合顺序
                checkCombination(elem1, elem2, basicElements, result);
                checkCombination(elem2, elem1, basicElements, result);
            }
        }
        
        return result;
    }
    
    /**
     * 检查特定顺序的元素组合
     */
    private static void checkCombination(ElementType elem1, ElementType elem2, 
            Map<ElementType, ElementInstance> basicElements, 
            Map<ElementType, ElementInstance> result) {
        
        ElementType complex = ElementType.createComplex(elem1, elem2);
        if (complex != null && !result.containsKey(complex)) {
            ElementInstance inst1 = basicElements.get(elem1);
            ElementInstance inst2 = basicElements.get(elem2);
            
            // 计算复合元素的值（可以包含顺序敏感的计算）
            double complexValue = calculateComplexValue(complex, elem1, elem2, 
                inst1.getValue(), inst2.getValue());
            
            // 确定新复合元素的位置（取两个基础元素中的较小位置）
            int newPosition = Math.min(inst1.getPosition(), inst2.getPosition());
            
            ElementInstance complexElement = new ElementInstance(
                complex, 
                complexValue, 
                newPosition,
                true,  // 计算得出的复合元素默认生效
                ElementSource.COMPUTED
            );
            
            result.put(complex, complexElement);
        }
    }
    
    /**
     * 计算复合元素的值（支持顺序敏感的计算）
     */
    private static double calculateComplexValue(ElementType complex, 
            ElementType elem1, ElementType elem2, 
            double value1, double value2) {
        
        // 基础计算：取较小值
        double baseValue = Math.min(value1, value2);
        
        // 顺序敏感的加成
        if (isPrimaryElement(elem1, complex)) {
            // 如果第一个元素是主要元素，给予额外加成
            baseValue *= 1.1; // 10%加成
        }
        
        return baseValue;
    }
    
    /**
     * 检查某个元素是否是复合元素的主要组成部分
     */
    private static boolean isPrimaryElement(ElementType element, ElementType complex) {
        // 这里可以根据游戏设计定义主要元素规则
        // 例如：对于爆炸，火焰是主要元素；对于腐蚀，电击是主要元素等
        switch (complex) {
            case BLAST: return element == ElementType.HEAT;
            case CORROSIVE: return element == ElementType.ELECTRICITY;
            case GAS: return element == ElementType.HEAT;
            case MAGNETIC: return element == ElementType.COLD;
            case RADIATION: return element == ElementType.HEAT;
            case VIRAL: return element == ElementType.TOXIN;
            default: return false;
        }
    }
    
    /**
     * 找出被组合使用的基础元素的位置
     */
    private static Set<Integer> findUsedBasicElementPositions(
            Map<ElementType, ElementInstance> basicElements,
            Map<ElementType, ElementInstance> complexElements) {
        
        Set<Integer> usedPositions = new HashSet<>();
        
        // 检查每个计算得出的复合元素使用了哪些基础元素
        for (ElementInstance complexElement : complexElements.values()) {
            List<ElementType> composition = complexElement.getType().getComposition();
            if (composition != null && composition.size() == 2) {
                ElementType elem1 = composition.get(0);
                ElementType elem2 = composition.get(1);
                
                ElementInstance sourceElement1 = basicElements.get(elem1);
                ElementInstance sourceElement2 = basicElements.get(elem2);
                
                if (sourceElement1 != null) {
                    usedPositions.add(sourceElement1.getPosition());
                }
                if (sourceElement2 != null) {
                    usedPositions.add(sourceElement2.getPosition());
                }
            }
        }
        
        return usedPositions;
    }
}