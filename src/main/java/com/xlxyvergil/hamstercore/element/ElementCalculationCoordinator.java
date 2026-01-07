package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 添加ElementUsageData的导入，用于存储元素属性的NBT数据

/**
 * 元素计算协调器
 * 协调元素计算流程，负责：
 * 1. 调用ElementCalculator从InitialModifiers层获取并计算数据
 * 2. 调用ElementCombinationModifier处理元素复合
 * 3. 将计算结果存储到物品的NBT中
 */
public class ElementCalculationCoordinator {
    public static final ElementCalculationCoordinator INSTANCE = new ElementCalculationCoordinator();
    
    private ElementCalculationCoordinator() {
    }
    
    /**
     * 计算元素值并将其存储到物品的NBT中
     * 支持HamsterCore元素和其他通用属性
     * @param stack 物品栈
     * @param weaponData 武器数据
     */
    public void calculateAndStoreElements(ItemStack stack, WeaponData weaponData) {
        // 1. 调用ElementCalculator从InitialModifiers层计算所有元素值（包括通用属性）
        Map<String, Double> elementValues = ElementCalculator.INSTANCE.calculateElementValuesFromInitialModifiers(weaponData);
        
        // 2. 创建elementType到name的映射，用于获取每个元素类型对应的名称
        Map<String, String> elementTypeToNameMap = new HashMap<>();
        for (InitialModifierEntry modifierEntry : weaponData.getInitialModifiers()) {
            // 使用第一个遇到的name作为该elementType的名称
            if (!elementTypeToNameMap.containsKey(modifierEntry.getElementType())) {
                elementTypeToNameMap.put(modifierEntry.getElementType(), modifierEntry.getName());
            }
        }
        
        // 3. 分离需要复合计算的元素和其他通用属性
        Map<String, Double> elementsForCombination = new HashMap<>();
        Map<String, Double> otherAttributes = new HashMap<>(); // 存储其他通用属性（包括HamsterCore的非复合元素和其他mod属性）
        
        for (Map.Entry<String, Double> entry : elementValues.entrySet()) {
            String attributeKey = entry.getKey(); // 带命名空间的elementType
            double value = entry.getValue();
            
            // 从elementTypeToNameMap中获取对应的name
            String name = elementTypeToNameMap.getOrDefault(attributeKey, attributeKey);
            
            // 使用ElementType.byName直接判断是否为基础元素或复合元素
            ElementType type = ElementType.byName(name);
            if (type != null && (type.isBasic() || type.isComplex())) {
                // 需要复合计算的元素，使用name
                elementsForCombination.put(name, value);
            } else {
                // 其他所有属性（包括HamsterCore的非复合元素和其他mod属性）统一存储，使用原始键
                otherAttributes.put(attributeKey, value);
            }
        }
        
        // 3. 使用已分离的需要复合计算的元素
        
        // 4. 调用ElementCombinationModifier处理HamsterCore元素复合
        Map<String, Double> combinedElements = processElementCombinations(weaponData, elementsForCombination);
        
        // 5. 创建属性修饰符条目列表，准备存储到物品NBT中
        List<ElementUsageData.AttributeModifierEntry> modifierEntries = new ArrayList<>();
        
        // 6. 直接基于计算结果创建属性修饰符条目
        
        // 处理复合元素结果
        for (Map.Entry<String, Double> entry : combinedElements.entrySet()) {
            String elementName = entry.getKey();
            double value = entry.getValue();
            
            // 创建唯一的UUID
            UUID uuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementName + ":modifier").getBytes());
            
            // 创建属性修饰符条目
            ElementUsageData.AttributeModifierEntry modifierEntry = new ElementUsageData.AttributeModifierEntry(
                elementName,
                "hamstercore:" + elementName,
                value,
                "ADDITION",
                uuid,
                "user"
            );
            
            modifierEntries.add(modifierEntry);
        }
        
        // 处理其他属性
        for (Map.Entry<String, Double> entry : otherAttributes.entrySet()) {
            String attributeKey = entry.getKey();
            double value = entry.getValue();
            
            // 提取属性名称（不带命名空间）
            String attributeName = attributeKey;
            if (attributeName.contains(":")) {
                attributeName = attributeName.substring(attributeName.lastIndexOf(":") + 1);
            }
            
            // 创建唯一的UUID
            UUID uuid = UUID.nameUUIDFromBytes((attributeKey + ":modifier").getBytes());
            
            // 创建属性修饰符条目
            ElementUsageData.AttributeModifierEntry modifierEntry = new ElementUsageData.AttributeModifierEntry(
                attributeName,
                attributeKey,
                value,
                "ADDITION",
                uuid,
                "user"
            );
            
            modifierEntries.add(modifierEntry);
        }
        
        // 7. 将完整的属性修饰符数据存储到物品的NBT中
        ElementUsageData.writeElementDataToItem(stack, modifierEntries);
    }
    
    /**
     * 处理元素复合
     * @param weaponData 武器数据
     * @param elementValues 元素值映射
     * @return 复合后的元素值映射
     */
    private Map<String, Double> processElementCombinations(WeaponData weaponData, Map<String, Double> elementValues) {
        // 创建一个临时的Map来存储复合结果
        Map<String, Double> tempValues = new HashMap<>(elementValues);
        
        // 调用ElementCombinationModifier处理元素复合，并直接使用返回的结果
        return ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, tempValues);
    }
    

}