package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;

import java.util.*;

/**
 * 元素组合修饰器 - 负责处理元素间的复合反应
 * 只对基础元素和复合元素进行操作，不处理其他类型元素
 * 完全依赖传入数据，不主动获取任何数据
 * 
 * 基础元素: fire, ice, electricity, toxin
 * 复合元素: explosion, corrosion, gas, magnetic, radiation, virus
 * 
 * 复合规则: 
 * - explosion(火焰+冰冻) 
 * - corrosion(电击+毒素) 
 * - gas(火焰+毒素) 
 * - magnetic(冰冻+电击) 
 * - radiation(火焰+电击) 
 * - virus(冰冻+毒素)
 */
public class ElementCombinationModifier {
    
    // 定义元素复合规则
    private static final Map<Set<String>, String> COMBINATION_RULES = new HashMap<>();
    private static final Map<String, Set<String>> REVERSE_COMBINATION_RULES = new HashMap<>();
    
    static {
        // 爆炸 = 火焰 + 冰冻
        Set<String> explosion = new HashSet<>(Arrays.asList("heat", "cold"));
        COMBINATION_RULES.put(explosion, "blast");
        REVERSE_COMBINATION_RULES.put("blast", explosion);
        
        // 腐蚀 = 电击 + 毒素
        Set<String> corrosion = new HashSet<>(Arrays.asList("electricity", "toxin"));
        COMBINATION_RULES.put(corrosion, "corrosive");
        REVERSE_COMBINATION_RULES.put("corrosive", corrosion);
        
        // 毒气 = 火焰 + 毒素
        Set<String> gas = new HashSet<>(Arrays.asList("heat", "toxin"));
        COMBINATION_RULES.put(gas, "gas");
        REVERSE_COMBINATION_RULES.put("gas", gas);
        
        // 磁力 = 冰冻 + 电击
        Set<String> magnetic = new HashSet<>(Arrays.asList("cold", "electricity"));
        COMBINATION_RULES.put(magnetic, "magnetic");
        REVERSE_COMBINATION_RULES.put("magnetic", magnetic);
        
        // 辐射 = 火焰 + 电击
        Set<String> radiation = new HashSet<>(Arrays.asList("heat", "electricity"));
        COMBINATION_RULES.put(radiation, "radiation");
        REVERSE_COMBINATION_RULES.put("radiation", radiation);
        
        // 病毒 = 冰冻 + 毒素
        Set<String> viral = new HashSet<>(Arrays.asList("cold", "toxin"));
        COMBINATION_RULES.put(viral, "viral");
        REVERSE_COMBINATION_RULES.put("viral", viral);
    }
    
    /**
     * 使用预计算的元素数值进行元素组合计算
     * ElementCombinationModifier完全依赖传入的数据，不主动获取任何数据
     * @param data 武器元素数据（用于获取Basic层的类型和来源信息）
     * @param elementValues 预计算并已分类的元素数值映射（仅包含基础元素和复合元素）
     */
    public static void computeElementCombinationsWithValues(WeaponData data, Map<String, Double> elementValues) {
        // 1. 清空使用层数据
        data.getUsageElements().clear();
        
        // 2. 从Basic层收集基础元素类型和来源信息，使用预分类的elementValues
        List<ElementEntry> orderedBasicElements = collectBasicElementsWithValues(data, elementValues);
        
        // 3. 对基础元素进行二次排序（user优先，def次之）
        List<ElementEntry> reorderedBasicElements = reorderBasicElements(orderedBasicElements);
        
        // 4. 对所有基础元素按重新排序后的顺序进行复合操作，完全使用传入的数值
        Map<String, Double> compositeResults = processElementCombinations(reorderedBasicElements);
        
        // 5. 分离Basic层中的复合元素（这些始终为def），完全使用传入的数值
        Map<String, Double> defCompositeElementsFromBasic = separateDefCompositeElements(reorderedBasicElements);
        
        // 6. 合并计算产生的复合元素和Basic层中的复合元素
        mergeCalculatedAndBasicCompositeElements(compositeResults, defCompositeElementsFromBasic);
        
        // 7. 处理剩余的基础元素与复合结果的交互，完全使用传入的数值
        processRemainingElementsInteraction(compositeResults, reorderedBasicElements);
        
        // 8. 将处理后的所有元素数据添加到Usage层
        for (Map.Entry<String, Double> entry : compositeResults.entrySet()) {
            // 只有值大于0的元素才添加到Usage层
            if (entry.getValue() > 0) {
                data.setUsageElement(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 收集Basic层的所有元素数据，使用预分类的elementValues（按顺序排列）
     * 不主动获取任何数据，完全依赖传入参数
     * @param data 武器元素数据（仅用于获取类型和来源信息）
     * @param elementValues 预分类的元素数值映射（仅包含基础元素和复合元素）
     */
    private static List<ElementEntry> collectBasicElementsWithValues(WeaponData data, Map<String, Double> elementValues) {
        List<ElementEntry> elements = new ArrayList<>();
        
        // 创建一个按顺序排列的元素列表
        List<Map.Entry<String, List<BasicEntry>>> orderedEntries = new ArrayList<>(data.getBasicElements().entrySet());
        
        // 按照order字段排序
        orderedEntries.sort((e1, e2) -> {
            int order1 = e1.getValue().get(0).getOrder();
            int order2 = e2.getValue().get(0).getOrder();
            return Integer.compare(order1, order2);
        });
        
        // 按顺序添加元素 - elementValues已经过滤过，直接使用
        for (Map.Entry<String, List<BasicEntry>> entry : orderedEntries) {
            String type = entry.getKey();
            String source = entry.getValue().get(0).getSource(); // 获取def/user标记
            
            // 完全使用传入的elementValues，不主动获取任何数据
            double value = elementValues.getOrDefault(type, 0.0);
            
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
            // 根据ElementType设定判断基础元素和复合元素
            ElementType type1 = ElementType.byName(e1.type);
            ElementType type2 = ElementType.byName(e2.type);
            
            boolean isBasic1 = type1 != null && type1.isBasic();
            boolean isBasic2 = type2 != null && type2.isBasic();
            
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
     * ElementCombinationModifier只对基础元素和复合元素进行操作
     */
    private static boolean isBasicElementType(String type) {
        // 基础元素类型：只有这四种可以相互组合
        return "heat".equals(type) || "cold".equals(type) || 
               "electricity".equals(type) || "toxin".equals(type);
    }
    
    
    
    /**
     * 对所有基础元素按重新排序后的顺序进行复合操作
     * 完全使用传入元素Entry中已包含的数值，不主动获取任何数据
     * @param reorderedElements 重新排序后的元素列表（包含预计算的数值）
     */
    private static Map<String, Double> processElementCombinations(List<ElementEntry> reorderedElements) {
        Map<String, Double> compositeResults = new LinkedHashMap<>();
        Set<String> usedElements = new HashSet<>();
        
        // 只处理基础元素进行复合 - 根据复合规则，只有这四种可以相互组合
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
     * 完全使用传入元素Entry中已包含的数值，不主动获取任何数据
     * @param reorderedElements 重新排序后的元素列表（包含预计算的数值）
     */
    private static Map<String, Double> separateDefCompositeElements(List<ElementEntry> reorderedElements) {
        Map<String, Double> defCompositeElements = new LinkedHashMap<>();
        
        for (ElementEntry element : reorderedElements) {
            // Basic层中的复合元素 - 根据ElementType设定判断
            ElementType elementType = ElementType.byName(element.type);
            if (elementType != null && elementType.isComplex()) {
                defCompositeElements.put(element.type, element.value);
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
     * 完全使用传入元素Entry中已包含的数值，不主动获取任何数据
     * @param compositeResults 复合结果映射
     * @param reorderedElements 重新排序后的元素列表（包含预计算的数值）
     */
    private static void processRemainingElementsInteraction(
            Map<String, Double> compositeResults,
            List<ElementEntry> reorderedElements) {
        
        // 找出未被使用的基础元素
        Set<String> usedInCombinations = new HashSet<>();
        // 从复合结果中找出已使用的元素
        for (String compositeType : compositeResults.keySet()) {
            Set<String> components = REVERSE_COMBINATION_RULES.get(compositeType);
            if (components != null) {
                usedInCombinations.addAll(components);
            }
        }
        
        // 查找未被使用的元素 - 根据复合规则，只有这四种可以相互组合
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
                // 检查是否为复合元素且存在于compositeResults中 - 根据ElementType设定
                ElementType elementType = ElementType.byName(element.type);
                if (elementType != null && elementType.isComplex() && compositeResults.containsKey(element.type)) {
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
        
        @Override
        public String toString() {
            return String.format("%s(%.3f,%s)", type, value, source);
        }
    }
}