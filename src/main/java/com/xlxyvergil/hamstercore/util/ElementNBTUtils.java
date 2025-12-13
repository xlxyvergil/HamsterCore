package com.xlxyvergil.hamstercore.util;

import net.minecraft.world.item.ItemStack;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.ElementType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 元素NBT工具类
 * 提供对物品元素数据的便捷访问方法
 * 只处理Usage层数据
 */
public class ElementNBTUtils {
    
    /**
     * 检查物品是否包含任何元素（只检查Usage层）
     */
    public static boolean hasAnyElements(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 尝试应用配置数据（如果尚未应用）
        WeaponDataManager.applyConfigDataIfNeeded(stack);
        
        // 从物品中加载元素数据
        WeaponData data = WeaponDataManager.loadElementData(stack);
        
        // 检查data是否为null
        if (data == null) {
            return false;
        }
        
        // 检查Usage层是否有数据
        // Usage层只包含基础元素和复合元素，不包含特殊属性和派系增伤
        for (Map.Entry<String, Double> entry : data.getUsageElements().entrySet()) {
            String elementTypeStr = entry.getKey();
            double value = entry.getValue();
            
            // 获取元素类型
            ElementType elementType = ElementType.byName(elementTypeStr);
            
            // 只检查基础元素和复合元素
            if (value > 0 && elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取Usage层指定元素类型的值
     */
    public static double getUsageElementValue(ItemStack stack, String elementType) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        
        // 尝试应用配置数据（如果尚未应用）
        WeaponDataManager.applyConfigDataIfNeeded(stack);
        
        // 从物品中加载元素数据
        WeaponData data = WeaponDataManager.loadElementData(stack);
        
        // 检查data是否为null
        if (data == null) {
            return 0.0;
        }
        
        // Usage层只包含基础元素和复合元素，不包含特殊属性和派系增伤
        // 所以不需要额外检查，直接返回值即可
        ElementType type = ElementType.byName(elementType);
        if (type != null && (type.isBasic() || type.isComplex())) {
            return data.getUsageElements().getOrDefault(elementType, 0.0);
        }
        
        return 0.0;
    }
    
    /**
     * 获取Usage层所有元素类型
     */
    public static Set<String> getAllUsageElementTypes(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HashSet<>();
        }
        
        // 尝试应用配置数据（如果尚未应用）
        WeaponDataManager.applyConfigDataIfNeeded(stack);
        
        // 从物品中加载元素数据
        WeaponData data = WeaponDataManager.loadElementData(stack);
        
        // 检查data是否为null
        if (data == null) {
            return new HashSet<>();
        }
        
        // 返回Usage层所有元素类型
        // Usage层只包含基础元素和复合元素，不包含特殊属性和派系增伤
        Set<String> elementTypes = new HashSet<>();
        for (Map.Entry<String, Double> entry : data.getUsageElements().entrySet()) {
            String elementTypeStr = entry.getKey();
            // 检查是否为基础元素或复合元素
            ElementType elementType = ElementType.byName(elementTypeStr);
            if (elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                elementTypes.add(elementTypeStr);
            }
        }
        
        return elementTypes;
    }
}