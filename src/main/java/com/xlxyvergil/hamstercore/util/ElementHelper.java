package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementType.TypeCategory;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ElementHelper {
    public static class ElementCategoryData {
        private final Map<String, Double> basicAndComplexValues;
        private final Map<String, Double> specialAndFactionValues;
        private final Map<String, Double> physicalValues;

        public ElementCategoryData(Map<String, Double> basicAndComplexValues, Map<String, Double> specialAndFactionValues, Map<String, Double> physicalValues) {
            this.basicAndComplexValues = basicAndComplexValues;
            this.specialAndFactionValues = specialAndFactionValues;
            this.physicalValues = physicalValues;
        }

        public Map<String, Double> getBasicAndComplexValues() {
            return basicAndComplexValues;
        }

        public Map<String, Double> getSpecialAndFactionValues() {
            return specialAndFactionValues;
        }

        public Map<String, Double> getPhysicalValues() {
            return physicalValues;
        }
    }

    public static ElementCategoryData getAllElementValuesByCategory(ItemStack weapon) {
        Map<String, Double> basicAndComplexValues = new HashMap<>();
        Map<String, Double> specialAndFactionValues = new HashMap<>();
        Map<String, Double> physicalValues = new HashMap<>();

        WeaponData weaponData = WeaponDataManager.loadElementData(weapon);
        if (weaponData == null) {
            return new ElementCategoryData(basicAndComplexValues, specialAndFactionValues, physicalValues);
        }

        Map<String, Double> usageElements = weaponData.getUsageElements();

        // 按类别分类元素值
        for (Map.Entry<String, Double> entry : usageElements.entrySet()) {
            String typeName = entry.getKey();
            double value = entry.getValue();
            ElementType type = ElementType.byName(typeName);
            
            if (type == null) {
                continue;
            }

            if (type.isPhysical()) {
                physicalValues.put(typeName, value);
            } else if (type.getTypeCategory() == TypeCategory.SPECIAL || 
                       type.getTypeCategory() == TypeCategory.TRIGGER_CHANCE) {
                specialAndFactionValues.put(typeName, value);
            } else {
                basicAndComplexValues.put(typeName, value);
            }
        }

        return new ElementCategoryData(basicAndComplexValues, specialAndFactionValues, physicalValues);
    }

    public static Map<String, Double> getAllSpecialAndFactionValues(ItemStack weapon) {
        ElementCategoryData categoryData = getAllElementValuesByCategory(weapon);
        return categoryData.getSpecialAndFactionValues();
    }

    public static double getElementValueFromItem(ItemStack stack, ElementType elementType) {
        WeaponData weaponData = WeaponDataManager.loadElementData(stack);
        if (weaponData == null) {
            return 0.0;
        }

        return weaponData.getUsageValue(elementType.name());
    }

    public static Map<ElementType, Double> getElementValues(ItemStack weapon) {
        WeaponData weaponData = WeaponDataManager.loadElementData(weapon);
        if (weaponData == null) {
            return Collections.emptyMap();
        }

        Map<String, Double> usageElements = weaponData.getUsageElements();
        Map<ElementType, Double> result = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : usageElements.entrySet()) {
            ElementType type = ElementType.byName(entry.getKey());
            if (type != null) {
                result.put(type, entry.getValue());
            }
        }

        return result;
    }

    public static List<ElementType> getActivatedElements(ItemStack weapon) {
        Map<ElementType, Double> elementValues = getElementValues(weapon);
        return elementValues.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static boolean hasAnyElements(ItemStack weapon) {
        WeaponData weaponData = WeaponDataManager.loadElementData(weapon);
        if (weaponData == null) {
            return false;
        }

        return !weaponData.getUsageElements().isEmpty();
    }
}