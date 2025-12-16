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
     * 计算所有元素值，包括基础元素、复合元素和特殊元素
     * @param weaponData 武器数据
     * @return 所有元素的计算值
     */
    public Map<ElementType, Double> calculateAllElementValues(WeaponData weaponData) {
        Map<ElementType, Double> elementValues = new HashMap<>();

        if (weaponData == null) {
            return elementValues;
        }

        // 直接从InitialModifiers层计算元素值
        Map<String, Double> initialElementValues = calculateElementValuesFromInitialModifiers(weaponData);

        // 转换为ElementType映射
        for (Map.Entry<String, Double> entry : initialElementValues.entrySet()) {
            ElementType type = ElementType.byName(entry.getKey());
            if (type != null) {
                elementValues.put(type, entry.getValue());
            }
        }

        return elementValues;
    }

    /**
     * 从InitialModifiers层计算元素值
     * @param weaponData 武器数据
     * @return 元素值映射
     */
    public Map<String, Double> calculateElementValuesFromInitialModifiers(WeaponData weaponData) {
        Map<String, Double> elementValues = new HashMap<>();

        if (weaponData == null) {
            return elementValues;
        }

        List<InitialModifierEntry> initialModifiers = weaponData.getInitialModifiers();

        // 累加同类型元素的值
        for (InitialModifierEntry entry : initialModifiers) {
            String elementType = entry.getElementType();
            double amount = entry.getAmount();

            elementValues.put(elementType, elementValues.getOrDefault(elementType, 0.0) + amount);
        }

        return elementValues;
    }

    /**
     * 获取激活的元素列表
     * @param weaponData 武器数据
     * @return 激活的元素列表
     */
    public List<Map.Entry<ElementType, Double>> getActivatedElements(WeaponData weaponData) {
        Map<ElementType, Double> elementValues = calculateAllElementValues(weaponData);
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
            ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(itemStack, weaponData);
        }
    }
}