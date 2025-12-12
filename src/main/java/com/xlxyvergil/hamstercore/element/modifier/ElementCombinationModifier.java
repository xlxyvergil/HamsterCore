package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import java.util.*;

/**
 * 元素组合修饰器 - 负责处理元素间的复合反应
 * 复合规则: 爆炸(火焰+冰冻) 腐蚀(电击+毒素) 毒气(火焰+毒素) 磁力(冰冻+电击) 辐射(火焰+电击) 病毒(冰冻+毒素)
 */
public class ElementCombinationModifier {
    
    // 定义元素复合规则
    private static final Map<Set<String>, String> COMBINATION_RULES = new HashMap<>();
    private static final List<String[]> COMBINATION_ORDER = Arrays.asList(
        new String[]{"heat", "cold", "blast"},
        new String[]{"electricity", "toxin", "corrosive"},
        new String[]{"heat", "toxin", "gas"},
        new String[]{"cold", "electricity", "magnetic"},
        new String[]{"heat", "electricity", "radiation"},
        new String[]{"cold", "toxin", "viral"}
    );
    
    // 反向映射：复合元素 -> 组成元素
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
    
    // 元素条目类，用于存储元素类型、值和来源
    private static class ElementEntry {
        String type;
        double value;
        String source; // "def" 或 "user"
        String operation; // "add", "multiply", "subtract", "divide"
        boolean used; // 是否已用于复合
        
        ElementEntry(String type, double value, String source) {
            this.type = type;
            this.value = value;
            this.source = source;
            this.operation = "add"; // 默认操作为加法
            this.used = false;
        }
        
        ElementEntry(String type, double value, String source, String operation) {
            this.type = type;
            this.value = value;
            this.source = source;
            this.operation = operation;
            this.used = false;
        }
        
        @Override
        public String toString() {
            return String.format("%s(%.3f,%s,%s)", type, value, source, operation);
        }
    }
    
    /**
     * 执行元素组合修饰
     * @param data 武器元素数据
     */
    public static void apply(WeaponElementData data) {
        
        // 使用新的计算方法
        computeElementCombinations(data);
        
    }
    
    /**
     * 计算元素组合（用于WeaponDataManager中）
     * @param data 武器元素数据
     */
    public static void computeElementCombinations(WeaponElementData data) {
        
        // 第一阶段：元素收集与合并
        // 1. 数据收集阶段
        // 从Basic层收集基础元素数据
        List<ElementEntry> basicElements = collectOrderedBasicElements(data);
        // 从Computed层收集计算元素数据
        List<ElementEntry> computedElements = collectOrderedComputedElements(data);
        
        
        // 2. 按照规范顺序排列元素：
        // Basic层基础元素 → Computed层基础元素 → Computed层复合元素 → Basic层复合元素
        List<ElementEntry> orderedBasicElements = new ArrayList<>();
        List<ElementEntry> orderedCompoundElements = new ArrayList<>();
        
        // 分离Basic层元素为基础元素和复合元素
        List<ElementEntry> basicLayerBasicElements = new ArrayList<>();
        List<ElementEntry> basicLayerCompoundElements = new ArrayList<>();
        for (ElementEntry entry : basicElements) {
            if (isBasicElementType(entry.type)) {
                basicLayerBasicElements.add(entry);
            } else if (isCompoundElementType(entry.type)) {
                basicLayerCompoundElements.add(entry);
            }
        }
        
        // 分离Computed层元素为基础元素和复合元素
        List<ElementEntry> computedLayerBasicElements = new ArrayList<>();
        List<ElementEntry> computedLayerCompoundElements = new ArrayList<>();
        for (ElementEntry entry : computedElements) {
            if (isBasicElementType(entry.type)) {
                computedLayerBasicElements.add(entry);
            } else if (isCompoundElementType(entry.type)) {
                computedLayerCompoundElements.add(entry);
            }
        }
        
        // 按照规范顺序组合元素
        orderedBasicElements.addAll(basicLayerBasicElements);
        orderedBasicElements.addAll(computedLayerBasicElements);
        orderedCompoundElements.addAll(computedLayerCompoundElements);
        orderedCompoundElements.addAll(basicLayerCompoundElements);
        
        // 3. 分别进行元素合并计算
        // 对基础元素按类型分组并进行数值合并（只考虑加减）
        Map<String, Double> mergedBasicElements = mergeElementsByType(orderedBasicElements);
        // 对复合元素按类型分组并进行数值合并（只考虑加减）
        Map<String, Double> mergedCompoundElements = mergeCompoundElementsByType(orderedCompoundElements);
        
        // 4. 确定元素来源标记
        // 基础元素：即使来源为def，在这个阶段也视为user
        // 复合元素：保留原有的def/user标记
        adjustElementSources(mergedBasicElements, mergedCompoundElements);
        
        
        // 第二阶段：元素复合处理
        // 1. 先对基础元素的数组进行处理
        Map<String, Double> compositeResults = processElementCombinations(mergedBasicElements);
        Map<String, Double> remainingBasicElements = calculateRemainingElements(mergedBasicElements, compositeResults);
        
        // 2. 将基础元素复合完成的新数值，与复合元素数组进行处理
        processCompoundElementsWithBaseElements(compositeResults, remainingBasicElements, mergedCompoundElements);
        
        // 3. 将处理后的所有元素数据添加到Usage层
        for (Map.Entry<String, Double> entry : compositeResults.entrySet()) {
            // 只有值大于0的元素才添加到Usage层
            if (entry.getValue() > 0) {
                data.setUsageValue(entry.getKey(), entry.getValue());
            }
        }
        
    }
    
    /**
     * 收集Basic层的所有元素数据（保持顺序）
     */
    private static List<ElementEntry> collectOrderedBasicElements(WeaponElementData data) {
        List<ElementEntry> elements = new ArrayList<>();
        Map<String, List<BasicEntry>> allBasics = data.getAllBasicElements();
        
        // 按照添加顺序处理元素（维持插入顺序）
        for (Map.Entry<String, List<BasicEntry>> entry : allBasics.entrySet()) {
            String type = entry.getKey();
            List<BasicEntry> basics = entry.getValue();
            
            // 处理所有基础元素
            for (BasicEntry basic : basics) {
                // 基础元素的def在合并阶段视为user
                elements.add(new ElementEntry(type, basic.getValue(), basic.getSource(), basic.getOperation()));
            }
        }
        
        return elements;
    }
    
    /**
     * 收集Computed层的所有元素数据（保持顺序）
     */
    private static List<ElementEntry> collectOrderedComputedElements(WeaponElementData data) {
        List<ElementEntry> elements = new ArrayList<>();
        Map<String, List<ComputedEntry>> allComputed = data.getAllComputedElements();
        
        // 按照添加顺序处理元素（维持插入顺序）
        for (Map.Entry<String, List<ComputedEntry>> entry : allComputed.entrySet()) {
            String type = entry.getKey();
            List<ComputedEntry> computedList = entry.getValue();
            
            // 处理所有计算元素
            for (ComputedEntry computed : computedList) {
                elements.add(new ElementEntry(type, computed.getValue(), computed.getSource(), computed.getOperation()));
            }
        }
        
        return elements;
    }
    
    /**
     * 合并同类基础元素（按类型分组并根据计算方法进行合并，只考虑加减）
     */
    private static Map<String, Double> mergeElementsByType(List<ElementEntry> elements) {
        Map<String, Double> mergedElements = new LinkedHashMap<>();
        
        for (ElementEntry entry : elements) {
            // 只处理基础元素类型（排除物理元素和特殊属性）
            if (isBasicElementType(entry.type)) {
                // 只考虑加减运算
                if ("add".equals(entry.operation)) {
                    mergedElements.merge(entry.type, entry.value, Double::sum);
                } else if ("subtract".equals(entry.operation)) {
                    mergedElements.merge(entry.type, -entry.value, Double::sum);
                }
                // 忽略 multiply 和 divide 运算
            }
        }
        
        return mergedElements;
    }
    
    /**
     * 合并同类复合元素（按类型分组并根据计算方法进行合并，只考虑加减）
     */
    private static Map<String, Double> mergeCompoundElementsByType(List<ElementEntry> elements) {
        Map<String, Double> mergedElements = new LinkedHashMap<>();
        
        for (ElementEntry entry : elements) {
            // 只处理复合元素类型
            if (isCompoundElementType(entry.type)) {
                // 只考虑加减运算
                if ("add".equals(entry.operation)) {
                    mergedElements.merge(entry.type, entry.value, Double::sum);
                } else if ("subtract".equals(entry.operation)) {
                    mergedElements.merge(entry.type, -entry.value, Double::sum);
                }
                // 忽略 multiply 和 divide 运算
            }
        }
        
        return mergedElements;
    }
    
    /**
     * 调整元素来源标记
     * 基础元素：即使来源为def，在这个阶段也视为user
     * 复合元素：保留原有的def/user标记
     */
    private static void adjustElementSources(Map<String, Double> basicElements, Map<String, Double> compoundElements) {
        // 基础元素的def在合并阶段视为user，已经在collect阶段处理过了
        // 复合元素保留原有标记，无需额外处理
    }
    
    /**
     * 处理基础元素的复合操作
     * 按照数组中user标记的基础元素从上到下的顺序进行复合
     */
    private static Map<String, Double> processElementCombinations(Map<String, Double> baseElements) {
        Map<String, Double> compositeResults = new LinkedHashMap<>();
        Map<String, Double> remainingElements = new LinkedHashMap<>(baseElements);
        
        // 按照数组中user标记的基础元素从上到下的顺序进行复合
        // 先获取所有可用的基础元素类型
        List<String> elementTypes = new ArrayList<>(remainingElements.keySet());
        
        // 从上到下依次尝试组合相邻的元素对
        for (int i = 0; i < elementTypes.size() - 1; i++) {
            String type1 = elementTypes.get(i);
            String type2 = elementTypes.get(i + 1);
            
            // 检查两个元素是否都存在且值大于0
            if (remainingElements.containsKey(type1) && remainingElements.containsKey(type2) &&
                remainingElements.get(type1) > 0 && remainingElements.get(type2) > 0) {
                
                // 查找这两个元素是否能组成复合元素
                String compositeType = findCompositeType(type1, type2);
                if (compositeType != null) {
                    // 组合元素，数值相加
                    double value1 = remainingElements.get(type1);
                    double value2 = remainingElements.get(type2);
                    double compositeValue = value1 + value2;
                    
                    // 添加复合元素到结果中
                    compositeResults.put(compositeType, compositeValue);
                    
                    // 从剩余元素中移除已组合的元素
                    remainingElements.remove(type1);
                    remainingElements.remove(type2);
                    

                }
            }
        }
        
        // 将未参与组合的剩余元素添加到结果中
        // 但在处理复合元素时，需要避免重复计算，所以只添加真正未参与任何组合的基础元素
        for (Map.Entry<String, Double> entry : remainingElements.entrySet()) {
            String type = entry.getKey();
            double value = entry.getValue();
            // 只有基础元素才添加到结果中，复合元素已经在其他地方处理
            if (isBasicElementType(type) && value > 0) {
                compositeResults.put(type, value);
            }
        }
        
        return compositeResults;
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
    
    /**
     * 计算剩余的基础元素（已参与组合的元素应被移除）
     * @param baseElements 基础元素
     * @param compositeResults 复合结果
     * @return 剩余的基础元素
     */
    private static Map<String, Double> calculateRemainingElements(Map<String, Double> baseElements, Map<String, Double> compositeResults) {
        Map<String, Double> remainingElements = new LinkedHashMap<>(baseElements);
        
        // 从剩余元素中移除已组合的基础元素（通过查找复合元素的组成部分）
        for (String compositeType : compositeResults.keySet()) {
            // 检查是否为复合元素类型
            Set<String> components = REVERSE_COMBINATION_RULES.get(compositeType);
            if (components != null) {
                // 移除组成该复合元素的基础元素
                for (String component : components) {
                    remainingElements.remove(component);
                }
            }
        }
        
        return remainingElements;
    }
    
    /**
     * 处理复合元素与基础元素的交互
     * @param compositeResults 复合结果
     * @param remainingElements 剩余的基础元素（已参与组合的元素应被移除）
     * @param compoundElements 复合元素
     */
    private static void processCompoundElementsWithBaseElements(
            Map<String, Double> compositeResults,
            Map<String, Double> remainingElements,
            Map<String, Double> compoundElements) {
        
        // 先把user类型的复合元素与def类型的同类型复合元素合并相加
        for (Map.Entry<String, Double> compoundEntry : compoundElements.entrySet()) {
            String type = compoundEntry.getKey();
            double value = compoundEntry.getValue();
            
            compositeResults.merge(type, value, Double::sum);
        }
        
        // 如果此时user类型里还有基础元素，那就从复合后的数组中从下往上找哪个复合元素可以由这个基础元素合并而成
        // 找到第一个可以由这个基础元素合并而成的复合元素后，把user类型里还有基础元素的值与这个复合元素的值相加
        // 注意：只有在没有剩余元素的情况下才执行此操作，避免重复计算
        if (remainingElements.isEmpty()) {
            return;
        }
        
        for (Map.Entry<String, Double> remainingEntry : remainingElements.entrySet()) {
            String baseType = remainingEntry.getKey();
            double baseValue = remainingEntry.getValue();
            
            // 从复合后的数组中从下往上找
            List<Map.Entry<String, Double>> entriesList = new ArrayList<>(compositeResults.entrySet());
            
            // 逆序遍历（从下往上）
            for (int i = entriesList.size() - 1; i >= 0; i--) {
                Map.Entry<String, Double> compositeEntry = entriesList.get(i);
                String compositeType = compositeEntry.getKey();
                
                // 检查该复合元素是否由当前基础元素组成
                Set<String> components = REVERSE_COMBINATION_RULES.get(compositeType);
                if (components != null && components.contains(baseType)) {
                    // 如果复合元素包含该基础元素，则将基础元素值加到复合元素中
                    double newValue = compositeEntry.getValue() + baseValue;
                    compositeEntry.setValue(newValue);
                    
                    break;
                }
            }
        }
    }
    
    /**
     * 判断是否为复合元素类型
     */
    private static boolean isCompoundElementType(String type) {
        return "blast".equals(type) || "corrosive".equals(type) || 
               "gas".equals(type) || "magnetic".equals(type) || 
               "radiation".equals(type) || "viral".equals(type);
    }
    
    /**
     * 判断是否为基础元素类型（排除物理元素和特殊属性）
     */
    private static boolean isBasicElementType(String type) {
        // 物理元素
        if ("slash".equals(type) || "puncture".equals(type) || "impact".equals(type)) {
            return false;
        }
        
        // 特殊属性
        if ("critical_chance".equals(type) || "critical_damage".equals(type) || "trigger_chance".equals(type)) {
            return false;
        }
        
        // 复合元素
        if (isCompoundElementType(type)) {
            return false;
        }
        
        // 其余都是基础元素
        return true;
    }
}