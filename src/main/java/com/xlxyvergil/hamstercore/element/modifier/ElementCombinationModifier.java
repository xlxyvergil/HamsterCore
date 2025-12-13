package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.ElementModifierValueUtil;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/**
 * 元素组合修饰器 - 负责处理元素间的复合反应
 * 复合规则: 爆炸(火焰+冰冻) 腐蚀(电击+毒素) 毒气(火焰+毒素) 磁力(冰冻+电击) 辐射(火焰+电击) 病毒(冰冻+毒素)
 */
public class ElementCombinationModifier {
    
    // 定义元素复合规则
    private static final Map<Set<String>, String> COMBINATION_RULES = new HashMap<>();
    private static final Map<String, Set<String>> REVERSE_COMBINATION_RULES = new HashMap<>();
    
    static {
        // 爆炸 = 火焰 + 冰冻
        Set<String> explosion = new HashSet<>(Arrays.asList("fire", "ice"));
        COMBINATION_RULES.put(explosion, "explosion");
        REVERSE_COMBINATION_RULES.put("explosion", explosion);
        
        // 腐蚀 = 电击 + 毒素
        Set<String> corrosion = new HashSet<>(Arrays.asList("electricity", "toxin"));
        COMBINATION_RULES.put(corrosion, "corrosion");
        REVERSE_COMBINATION_RULES.put("corrosion", corrosion);
        
        // 毒气 = 火焰 + 毒素
        Set<String> gas = new HashSet<>(Arrays.asList("fire", "toxin"));
        COMBINATION_RULES.put(gas, "gas");
        REVERSE_COMBINATION_RULES.put("gas", gas);
        
        // 磁力 = 冰冻 + 电击
        Set<String> magnetic = new HashSet<>(Arrays.asList("ice", "electricity"));
        COMBINATION_RULES.put(magnetic, "magnetic");
        REVERSE_COMBINATION_RULES.put("magnetic", magnetic);
        
        // 辐射 = 火焰 + 电击
        Set<String> radiation = new HashSet<>(Arrays.asList("fire", "electricity"));
        COMBINATION_RULES.put(radiation, "radiation");
        REVERSE_COMBINATION_RULES.put("radiation", radiation);
        
        // 病毒 = 冰冻 + 毒素
        Set<String> viral = new HashSet<>(Arrays.asList("ice", "toxin"));
        COMBINATION_RULES.put(viral, "virus");
        REVERSE_COMBINATION_RULES.put("virus", viral);
    }
    
    /**
     * 执行元素组合修饰
     * @param data 武器元素数据
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    public static void apply(WeaponData data, ItemStack stack) {
        // 清空使用层数据
        data.getUsageElements().clear();
        
        // 执行元素复合计算
        computeElementCombinations(data, stack);
    }    
    /**
     * 计算元素组合（用于WeaponDataManager中）
     * @param data 武器元素数据
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    public static void computeElementCombinations(WeaponData data, ItemStack stack) {
        // 1. 从Basic层收集基础元素数据，按顺序排列
        List<ElementEntry> orderedBasicElements = collectOrderedBasicElements(data, stack);        
        // 2. 对基础元素进行二次排序（user优先，def次之）
        List<ElementEntry> reorderedBasicElements = reorderBasicElements(orderedBasicElements);
        
        // 3. 对所有基础元素按重新排序后的顺序进行复合操作
        Map<String, Double> compositeResults = processAllElementCombinations(reorderedBasicElements, stack);
        
        // 4. 分离Basic层中的复合元素（这些始终为def）
        Map<String, Double> defCompositeElementsFromBasic = separateDefCompositeElementsFromBasic(reorderedBasicElements, stack);        
        // 5. 合并计算产生的复合元素和Basic层中的复合元素
        mergeCalculatedAndBasicCompositeElements(compositeResults, defCompositeElementsFromBasic);
        
        // 6. 处理剩余的基础元素与复合结果的交互
        processRemainingElementsInteraction(compositeResults, reorderedBasicElements, stack);        
        // 7. 将处理后的所有元素数据添加到Usage层
        for (Map.Entry<String, Double> entry : compositeResults.entrySet()) {
            // 只有值大于0的元素才添加到Usage层
            if (entry.getValue() > 0) {
                data.setUsageElement(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 收集Basic层的所有元素数据（按顺序排列）
     * @param data 武器数据
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    private static List<ElementEntry> collectOrderedBasicElements(WeaponData data, ItemStack stack) {
        List<ElementEntry> elements = new ArrayList<>();
        
        // 创建一个按顺序排列的元素列表
        List<Map.Entry<String, List<WeaponData.BasicEntry>>> orderedEntries = new ArrayList<>(data.getBasicElements().entrySet());
        
        // 按照order字段排序
        orderedEntries.sort((e1, e2) -> {
            int order1 = e1.getValue().get(0).getOrder();
            int order2 = e2.getValue().get(0).getOrder();
            return Integer.compare(order1, order2);
        });
        
        // 按顺序添加元素
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : orderedEntries) {
            String type = entry.getKey();
            String source = entry.getValue().get(0).getSource(); // 获取def/user标记
            
            // 获取元素类型
            ElementType elementType = ElementType.byName(type);
            double value = 0.0;
            if (elementType != null) {
                // 从物品的修饰符中获取实际数值
                value = ElementModifierValueUtil.getElementValueFromAttributes(stack, elementType);
            }
            
            elements.add(new ElementEntry(type, value, source));
        }
        
        return elements;
    }
    
    /**
     * 对基础元素进行二次排序（user优先，def次之）
     */
    private static List<ElementEntry> reorderBasicElements(List<ElementEntry> elements) {
        List<ElementEntry> reorderedElements = new ArrayList<>(elements);
        
        // 按照source字段排序，user优先，def次之
        reorderedElements.sort((e1, e2) -> {
            // 如果两个元素都是基础元素或者都是复合元素，则按source排序
            boolean isBasic1 = isBasicElementType(e1.type);
            boolean isBasic2 = isBasicElementType(e2.type);
            
            // 基础元素优先于复合元素
            if (isBasic1 && !isBasic2) {
                return -1;
            } else if (!isBasic1 && isBasic2) {
                return 1;
            }
            
            // 如果都是基础元素或都是复合元素，则按user/def排序
            if (e1.source.equals(e2.source)) {
                return 0; // 如果source相同，则保持原有顺序
            } else if (e1.source.equals("user")) {
                return -1; // user优先
            } else {
                return 1; // def次之
            }
        });
        
        return reorderedElements;
    }
    
    /**
     * 判断是否为基础元素类型
     */
    private static boolean isBasicElementType(String type) {
        Set<String> basicTypes = new HashSet<>(Arrays.asList(
            "fire", "ice", "electricity", "toxin" // 基础元素类型
        ));
        return basicTypes.contains(type);
    }
    
    /**
     * 对所有基础元素按重新排序后的顺序进行复合操作
     * @param reorderedElements 重新排序后的元素列表
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    private static Map<String, Double> processAllElementCombinations(List<ElementEntry> reorderedElements, ItemStack stack) {
        Map<String, Double> compositeResults = new LinkedHashMap<>();
        Set<String> usedElements = new HashSet<>();
        
        // 只处理基础元素进行复合
        List<ElementEntry> basicElements = new ArrayList<>();
        for (ElementEntry element : reorderedElements) {
            if (isBasicElementType(element.type)) {
                basicElements.add(element);
            }
        }
        
        // 按照重新排序后的顺序尝试组合相邻的基础元素
        for (int i = 0; i < basicElements.size() - 1; i++) {
            ElementEntry elem1 = basicElements.get(i);
            ElementEntry elem2 = basicElements.get(i + 1);
            
            // 检查两个元素是否都未被使用
            if (!usedElements.contains(elem1.type) && !usedElements.contains(elem2.type)) {
                // 查找这两个元素是否能组成复合元素
                String compositeType = findCompositeType(elem1.type, elem2.type);
                if (compositeType != null) {
                    // 组合元素，数值相加
                    double compositeValue = elem1.value + elem2.value;
                    
                    // 添加复合元素到结果中
                    compositeResults.put(compositeType, compositeValue);
                    
                    // 标记元素为已使用
                    usedElements.add(elem1.type);
                    usedElements.add(elem2.type);
                }
            }
        }
        
        return compositeResults;
    }
    
    /**
     * 分离Basic层中的def复合元素
     * @param reorderedElements 重新排序后的元素列表
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    private static Map<String, Double> separateDefCompositeElementsFromBasic(List<ElementEntry> reorderedElements, ItemStack stack) {
        Map<String, Double> defCompositeElements = new LinkedHashMap<>();
        
        for (ElementEntry element : reorderedElements) {
            // Basic层中的复合元素始终标记为def
            if (!isBasicElementType(element.type)) {
                // 获取元素类型
                ElementType elementType = ElementType.byName(element.type);
                double value = 0.0;
                if (elementType != null) {
                    // 从物品的修饰符中获取实际数值
                    value = ElementModifierValueUtil.getElementValueFromAttributes(stack, elementType);
                }
                
                defCompositeElements.put(element.type, value);
            }
        }
        
        return defCompositeElements;
    }
    
    /**
     * 合并计算产生的复合元素和Basic层中的复合元素
     */
    private static void mergeCalculatedAndBasicCompositeElements(
            Map<String, Double> compositeResults,
            Map<String, Double> defCompositeElementsFromBasic) {
        
        // 将Basic层中的def复合元素添加到结果中或与已有的同类型元素合并
        for (Map.Entry<String, Double> defEntry : defCompositeElementsFromBasic.entrySet()) {
            String type = defEntry.getKey();
            double value = defEntry.getValue();
            
            if (compositeResults.containsKey(type)) {
                // 如果已存在同类型元素，则数值相加
                compositeResults.put(type, compositeResults.get(type) + value);
            } else {
                // 如果不存在，则直接添加
                compositeResults.put(type, value);
            }
        }
    }
    
    /**
     * 处理剩余的基础元素与复合结果的交互
     * @param compositeResults 复合结果映射
     * @param reorderedElements 重新排序后的元素列表
     * @param stack 物品堆，用于获取实际的元素修饰符值
     */
    private static void processRemainingElementsInteraction(
            Map<String, Double> compositeResults,
            List<ElementEntry> reorderedElements,
            ItemStack stack) {
        
        // 找出未被使用的基础元素
        Set<String> usedInCombinations = new HashSet<>();
        // 从复合结果中找出已使用的元素
        for (String compositeType : compositeResults.keySet()) {
            Set<String> components = REVERSE_COMBINATION_RULES.get(compositeType);
            if (components != null) {
                usedInCombinations.addAll(components);
            }
        }
        
        // 查找未被使用的元素
        List<ElementEntry> remainingElements = new ArrayList<>();
        for (ElementEntry element : reorderedElements) {
            if (isBasicElementType(element.type) && !usedInCombinations.contains(element.type)) {
                remainingElements.add(element);
            }
        }
        
        // 对于每个剩余的基础元素，按照Basic层顺序找到首个可以由这个元素合成的复合元素，并把值加入这个复合元素
        for (ElementEntry remainingElement : remainingElements) {
            String baseType = remainingElement.type;
            double baseValue = remainingElement.value;
            
            // 按照Basic层中的复合元素顺序找到首个可以由这个基础元素合成的复合元素
            for (ElementEntry element : reorderedElements) {
                // 检查是否为复合元素且存在于compositeResults中
                if (!isBasicElementType(element.type) && compositeResults.containsKey(element.type)) {
                    Set<String> components = REVERSE_COMBINATION_RULES.get(element.type);
                    
                    if (components != null && components.contains(baseType)) {
                        // 将基础元素值加到复合元素中
                        double newValue = compositeResults.get(element.type) + baseValue;
                        compositeResults.put(element.type, newValue);
                        break; // 只处理第一个匹配的复合元素
                    }
                }
            }
        }
    }
    
    /**
     * 根据两个基础元素类型查找对应的复合元素类型
     * @param type1 第一个元素类型
     * @param type2 第二个元素类型
     * @return 复合元素类型，如果找不到则返回null
     */
    private static String findCompositeType(String type1, String type2) {
        // 创建元素集合
        Set<String> elements = new HashSet<>();
        elements.add(type1);
        elements.add(type2);
        
        // 查找对应的复合元素类型
        return COMBINATION_RULES.get(elements);
    }
    
    // 元素条目类，用于存储元素类型、值和来源
    private static class ElementEntry {
        String type;
        double value;
        String source; // "def" 或 "user"
        
        ElementEntry(String type, double value, String source) {
            this.type = type;
            this.value = value;
            this.source = source;
        }
        
        public net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation getOperation() {
            return net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
        }
        
        @Override
        public String toString() {
            return String.format("%s(%.3f,%s)", type, value, source);
        }
    }
}