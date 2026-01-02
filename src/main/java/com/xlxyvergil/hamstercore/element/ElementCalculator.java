package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementCalculator {
    public static final ElementCalculator INSTANCE = new ElementCalculator();

    private ElementCalculator() {
    }

    /**
     * 计算所有元素值，包括基础元素、复合元素和其他通用属性
     * @param weaponData 武器数据
     * @return 所有元素的计算值（保持String键名以支持通用属性）
     */
    public Map<String, Double> calculateAllElementValues(WeaponData weaponData) {
        Map<String, Double> elementValues = new HashMap<>();

        if (weaponData == null) {
            return elementValues;
        }

        // 直接从InitialModifiers层计算元素值（支持通用属性）
        Map<String, Double> initialElementValues = calculateElementValuesFromInitialModifiers(weaponData);

        // 转换为通用映射（保持所有属性，包括非HamsterCore属性）
        for (Map.Entry<String, Double> entry : initialElementValues.entrySet()) {
            elementValues.put(entry.getKey(), entry.getValue());
        }

        return elementValues;
    }

    /**
     * 从InitialModifiers层计算元素值（按name进行分组计算，模拟Forge属性修饰符计算）
     * @param weaponData 武器数据
     * @return 元素值映射
     */
    public Map<String, Double> calculateElementValuesFromInitialModifiers(WeaponData weaponData) {
        Map<String, Double> elementValues = new HashMap<>();

        if (weaponData == null) {
            return elementValues;
        }

        List<InitialModifierEntry> initialModifiers = weaponData.getInitialModifiers();

        // 按elementType分组，然后根据operation类型进行计算（模拟Forge属性修饰符计算）
        Map<String, List<InitialModifierEntry>> groupedModifiers = initialModifiers.stream()
            .collect(Collectors.groupingBy(InitialModifierEntry::getElementType));

        for (Map.Entry<String, List<InitialModifierEntry>> entry : groupedModifiers.entrySet()) {
            String elementType = entry.getKey();
            List<InitialModifierEntry> modifiers = entry.getValue();
            
            // 对同类型修饰符进行合并计算（模拟Forge属性修饰符计算）
            double calculatedValue = calculateValueForName(modifiers);
            elementValues.put(elementType, calculatedValue);
        }

        return elementValues;
    }

    /**
     * 对同名修饰符进行合并计算（模拟Forge属性修饰符计算）
     * @param modifiers 同名的修饰符列表
     * @return 合并计算后的值
     */
    private double calculateValueForName(List<InitialModifierEntry> modifiers) {
        double additionValue = 0.0;
        double multiplyBaseValue = 0.0; // MULTIPLY_BASE
        double multiplyTotalValue = 0.0; // MULTIPLY_TOTAL

        for (InitialModifierEntry entry : modifiers) {
            double amount = entry.getAmount();
            String operation = entry.getOperation();

            switch (operation.toUpperCase()) {
                case "ADDITION":
                    additionValue += amount;
                    break;
                case "MULTIPLY_BASE":
                    multiplyBaseValue += amount;
                    break;
                case "MULTIPLY_TOTAL":
                    multiplyTotalValue += amount;
                    break;
                default:
                    additionValue += amount; // 默认为ADDITION
                    break;
            }
        }

        // 计算最终值：(baseValue + additionValue) * (1 + multiplyBaseValue) * (1 + multiplyTotalValue)
        // 对于初始值，baseValue为0，所以结果是: additionValue * (1 + multiplyBaseValue) * (1 + multiplyTotalValue)
        double result = additionValue * (1 + multiplyBaseValue);
        result = result * (1 + multiplyTotalValue);

        return result;
    }

    /**
     * 获取激活的元素列表（包括HamsterCore元素和其他通用属性）
     * @param weaponData 武器数据
     * @return 激活的元素列表
     */
    public List<Map.Entry<String, Double>> getActivatedElements(WeaponData weaponData) {
        Map<String, Double> elementValues = calculateAllElementValues(weaponData);
        return elementValues.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.0)
                .collect(Collectors.toList());
    }

    /**
     * 重新计算武器的元素数据
     * @param itemStack 物品栈
     */
    public void recalculateElementData(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }

        // 现在由ElementCalculationCoordinator负责协调计算流程
        WeaponData weaponData = WeaponDataManager.loadElementData(itemStack);
        if (weaponData != null) {
            ElementCalculationCoordinator.INSTANCE.calculateAndStoreElements(itemStack, weaponData);
        }
    }
}