package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.util.DebugLogger;

import java.util.*;

/**
 * 元素复合Modifier
 * 专门处理基础元素和复合元素的组合计算
 */
public class ElementCombinationModifier {
    
    /**
     * 计算元素复合结果
     * 从Basic层和Computed层获取基础元素数据，计算复合元素，放入Usage层
     */
    public static void computeElementCombinations(WeaponElementData data) {
        DebugLogger.log("开始计算元素复合...");
        
        // 收集所有基础元素数据
        Map<String, Double> basicElementValues = collectBasicElementValues(data);
        
        // 按顺序计算复合元素组合
        Map<String, Double> complexElementValues = computeComplexCombinations(basicElementValues);
        
        // 将所有元素值写入Usage层
        for (Map.Entry<String, Double> entry : basicElementValues.entrySet()) {
            data.setUsageValue(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String, Double> entry : complexElementValues.entrySet()) {
            data.setUsageValue(entry.getKey(), entry.getValue());
        }
        
        DebugLogger.log("元素复合计算完成，基础元素: %d个，复合元素: %d个", 
                       basicElementValues.size(), complexElementValues.size());
    }
    
    /**
     * 收集所有基础元素值（从Basic层和Computed层）
     */
    private static Map<String, Double> collectBasicElementValues(WeaponElementData data) {
        Map<String, Double> result = new HashMap<>();
        
        DebugLogger.log("收集基础元素值，Basic层总数: %d", data.getAllBasicElements().size());
        
        // 收集Basic层的基础元素
        for (String type : data.getAllBasicElements().keySet()) {
            BasicEntry entry = data.getBasicElement(type);
            if (entry != null && isBasicElementType(type)) {
                double value = entry.getValue();
                
                DebugLogger.log("从Basic层获取到基础元素 %s: %.3f", type, value);
                
                // 应用Computed层的修正
                ComputedEntry computedEntry = data.getComputedElement(type);
                if (computedEntry != null) {
                    value = applyModifier(value, computedEntry);
                    DebugLogger.log("应用Computed层修正后 %s: %.3f", type, value);
                }
                
                result.put(type, value);
            }
        }
        
        // 收集Computed层独有的基础元素
        for (String type : data.getAllComputedElements().keySet()) {
            if (!result.containsKey(type) && isBasicElementType(type)) {
                ComputedEntry computedEntry = data.getComputedElement(type);
                if (computedEntry != null) {
                    result.put(type, computedEntry.getValue());
                    DebugLogger.log("从Computed层获取到独有基础元素 %s: %.3f", type, computedEntry.getValue());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 检查是否为基础元素类型
     */
    private static boolean isBasicElementType(String type) {
        // 先检查是否为预定义的元素类型
        try {
            ElementType elementType = ElementType.byName(type);
            if (elementType != null) {
                return elementType.isBasic();
            }
        } catch (Exception e) {
            // 忽略异常，继续检查其他条件
        }
        
        // 如果不是预定义的元素类型，根据名称判断是否为基础元素
        // 基础元素包括三类：物理元素、元素属性、特殊属性
        return "slash".equals(type) || "puncture".equals(type) || "impact".equals(type) ||  // 物理元素
               "heat".equals(type) || "cold".equals(type) || "electricity".equals(type) || "toxin".equals(type) || // 元素属性
               "critical_chance".equals(type) || "critical_damage".equals(type) || "trigger_chance".equals(type); // 特殊属性
    }
    
    /**
     * 计算复合元素组合
     * 规则：
     * 1. 元素的顺序决定了元素伤害组合
     * 2. 按照顺序，从上到下，依次复合
     * 3. 如果使用多个相同元素类型，最先使用的那个元素决定了该类型基础元素进行复合的次序
     * 4. 参与复合的基础元素，效果将不生效
     * 5. 爆炸（火焰+冰冻），腐蚀（电击+毒素），毒气（火焰+毒素），磁力（冰冻+电击），辐射（火焰+电击），病毒（冰冻+毒素）
     */
    private static Map<String, Double> computeComplexCombinations(Map<String, Double> basicElements) {
        Map<String, Double> result = new HashMap<>();
        Set<String> usedTypes = new HashSet<>(); // 记录已使用的元素类型
        
        // 按预定义顺序处理基础元素（需要根据实际的元素顺序）
        List<String> elementOrder = getBasicElementOrder();
        
        for (int i = 0; i < elementOrder.size(); i++) {
            String type1 = elementOrder.get(i);
            
            // 如果该元素不存在或已被使用，跳过
            if (!basicElements.containsKey(type1) || usedTypes.contains(type1)) {
                continue;
            }
            
            // 查找后续不同类型的元素
            for (int j = i + 1; j < elementOrder.size(); j++) {
                String type2 = elementOrder.get(j);
                
                // 如果该元素不存在或已被使用，跳过
                if (!basicElements.containsKey(type2) || usedTypes.contains(type2)) {
                    continue;
                }
                
                // 尝试组合这两个元素
                String complexType = createComplexElement(type1, type2);
                if (complexType != null) {
                    // 计算复合元素的值：取两个元素的值之和
                    double value1 = basicElements.get(type1);
                    double value2 = basicElements.get(type2);
                    double complexValue = value1 + value2;
                    
                    result.put(complexType, complexValue);
                    
                    // 标记这两个元素类型已被使用（基础元素不再生效）
                    usedTypes.add(type1);
                    usedTypes.add(type2);
                    
                    DebugLogger.log("复合元素组合: %s + %s -> %s (%.3f)", 
                                  type1, type2, complexType, complexValue);
                    
                    // 找到组合后跳出内层循环，继续处理下一个未使用的元素
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取基础元素的顺序（需要根据实际元素定义调整）
     */
    private static List<String> getBasicElementOrder() {
        return Arrays.asList(
            "heat",        // 火焰
            "cold",        // 冰冻  
            "electricity", // 电击
            "toxin",       // 毒素
            "slash",       // 挥砍
            "puncture",    // 穿刺
            "impact"       // 冲击
        );
    }
    
    /**
     * 创建复合元素
     */
    private static String createComplexElement(String type1, String type2) {
        // 爆炸（火焰+冰冻）
        if (("heat".equals(type1) && "cold".equals(type2)) || 
            ("cold".equals(type1) && "heat".equals(type2))) {
            return "blast";
        }
        
        // 腐蚀（电击+毒素）
        if (("electricity".equals(type1) && "toxin".equals(type2)) || 
            ("toxin".equals(type1) && "electricity".equals(type2))) {
            return "corrosive";
        }
        
        // 毒气（火焰+毒素）
        if (("heat".equals(type1) && "toxin".equals(type2)) || 
            ("toxin".equals(type1) && "heat".equals(type2))) {
            return "gas";
        }
        
        // 磁力（冰冻+电击）
        if (("cold".equals(type1) && "electricity".equals(type2)) || 
            ("electricity".equals(type1) && "cold".equals(type2))) {
            return "magnetic";
        }
        
        // 辐射（火焰+电击）
        if (("heat".equals(type1) && "electricity".equals(type2)) || 
            ("electricity".equals(type1) && "heat".equals(type2))) {
            return "radiation";
        }
        
        // 病毒（冰冻+毒素）
        if (("cold".equals(type1) && "toxin".equals(type2)) || 
            ("toxin".equals(type1) && "cold".equals(type2))) {
            return "viral";
        }
        
        return null; // 无法组合
    }
    
    /**
     * 应用计算修正
     */
    private static double applyModifier(double baseValue, ComputedEntry computedEntry) {
        String operation = computedEntry.getOperation();
        double value = computedEntry.getValue();
        
        switch (operation) {
            case "add":
                return baseValue + value;
            case "sub":
                return baseValue - value;
            case "mul":
                return baseValue * value;
            case "div":
                return value != 0 ? baseValue / value : baseValue;
            default:
                DebugLogger.log("未知的计算操作: %s", operation);
                return baseValue;
        }
    }
}