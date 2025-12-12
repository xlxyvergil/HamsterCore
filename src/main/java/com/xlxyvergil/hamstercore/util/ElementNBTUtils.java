package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.element.ExtraEntry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 元素NBT工具类
 * 提供对武器元素数据的读取和解析功能
 */
public class ElementNBTUtils {
    
    public static final String TRIGGER_CHANCE = "trigger_chance";
    
    /**
     * 检查武器是否有任何元素属性
     * 从Usage层获取数据
     */
    public static boolean hasAnyElements(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 从Usage层获取元素数据
        List<Map.Entry<ElementType, Double>> elements = ElementDamageManager.getActiveElements(stack);
        return !elements.isEmpty();
    }
    
    /**
     * 获取暴击率
     * 从Usage层获取数据
     */
    public static double getCriticalChance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Usage层获取暴击率数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        WeaponDataManager.computeUsageData(stack, data);
        
        // 检查Usage层是否有暴击率数据
        List<Double> criticalChances = data.getUsageValue("critical_chance");
        double sum = criticalChances.stream().mapToDouble(Double::doubleValue).sum();
        return sum;
    }
    
    /**
     * 获取暴击伤害
     * 从Usage层获取数据
     */
    public static double getCriticalDamage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Usage层获取暴击伤害数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        WeaponDataManager.computeUsageData(stack, data);
        
        // 检查Usage层是否有暴击伤害数据
        List<Double> criticalDamages = data.getUsageValue("critical_damage");
        double sum = criticalDamages.stream().mapToDouble(Double::doubleValue).sum();
        return sum;
    }
    
    /**
     * 获取触发率
     * 从Usage层获取数据
     */
    public static double getTriggerChance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Usage层获取触发率数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        WeaponDataManager.computeUsageData(stack, data);
        
        // 检查Usage层是否有触发率数据
        List<Double> triggerChances = data.getUsageValue("trigger_chance");
        double sum = triggerChances.stream().mapToDouble(Double::doubleValue).sum();
        return sum;
    }
    
    /**
     * 获取所有元素类型
     * 从Usage层获取数据
     */
    public static Set<ElementType> getAllElementTypes(ItemStack stack) {
        List<Map.Entry<ElementType, Double>> elements = ElementDamageManager.getActiveElements(stack);
        Set<ElementType> types = new HashSet<>();
        for (Map.Entry<ElementType, Double> element : elements) {
            types.add(element.getKey());
        }
        return types;
    }
    
    /**
     * 获取指定元素类型的值
     * 从Usage层获取数据
     */
    public static double getElementValue(ItemStack stack, ElementType type) {
        List<Map.Entry<ElementType, Double>> elements = ElementDamageManager.getActiveElements(stack);
        for (Map.Entry<ElementType, Double> element : elements) {
            if (element.getKey() == type) {
                return element.getValue();
            }
        }
        return 0.0; // 默认值
    }
    

    
    /**
     * 获取Basic层指定元素类型的值
     * 从Basic层获取数据
     */
    public static double getBasicElementValue(ItemStack stack, String elementType) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Basic层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 检查Basic层是否有指定元素类型的数据
        List<BasicEntry> entries = data.getBasicElement(elementType);
        return entries.stream().filter(e -> "user".equals(e.getSource()))
                     .mapToDouble(BasicEntry::getValue).sum();
    }
    
    /**
     * 获取Basic层所有元素类型
     * 从Basic层获取数据
     */
    public static Set<String> getAllBasicElementTypes(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HashSet<>();
        }
        
        // 从Basic层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 返回Basic层所有元素类型
        return data.getAllBasicElements().keySet();
    }
    
    /**
     * 获取Computed层指定元素类型的值
     * 从Computed层获取数据
     */
    public static double getComputedElementValue(ItemStack stack, String elementType) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Computed层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 检查Computed层是否有指定元素类型的数据
        List<ComputedEntry> entries = data.getComputedElement(elementType);
        return entries.stream().filter(e -> "user".equals(e.getSource()))
                     .mapToDouble(ComputedEntry::getValue).sum();
    }
    
    /**
     * 获取Computed层所有元素类型
     * 从Computed层获取数据
     */
    public static Set<String> getAllComputedElementTypes(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HashSet<>();
        }
        
        // 从Computed层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 返回Computed层所有元素类型
        return data.getAllComputedElements().keySet();
    }
    
    /**
     * 获取Usage层指定元素类型的值
     * 从Usage层获取数据
     */
    public static double getUsageElementValue(ItemStack stack, String elementType) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Usage层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        WeaponDataManager.computeUsageData(stack, data);
        
        // 检查Usage层是否有指定元素类型的数据
        List<Double> values = data.getUsageValue(elementType);
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * 获取Usage层所有元素类型
     * 从Usage层获取数据
     */
    public static Set<String> getAllUsageElementTypes(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HashSet<>();
        }
        
        // 从Usage层获取元素数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        WeaponDataManager.computeUsageData(stack, data);
        
        // 返回Usage层所有元素类型
        return data.getAllUsageValues().keySet();
    }
    
    /**
     * 获取Extra层指定派系的修饰值
     * 从Extra层获取数据
     */
    public static double getExtraFactionModifier(ItemStack stack, String faction) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 从Extra层获取派系修饰数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 检查Extra层是否有指定派系的数据
        List<ExtraEntry> entries = data.getExtraFaction(faction);
        return entries.stream().mapToDouble(ExtraEntry::getValue).sum();
    }
    
    /**
     * 获取Extra层所有派系类型
     * 从Extra层获取数据
     */
    public static Set<String> getAllExtraFactions(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HashSet<>();
        }
        
        // 从Extra层获取派系数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 返回Extra层所有派系类型
        return data.getAllExtraFactions().keySet();
    }
}