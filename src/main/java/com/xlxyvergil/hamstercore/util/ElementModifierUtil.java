package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 元素修饰符工具类
 * 集中处理元素修饰符的应用逻辑
 * 适配新的数据结构
 */
public class ElementModifierUtil {
    
    /**
     * 应用元素修饰符到物品
     */
    public static void applyElementModifiers(ItemStack stack, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        // 遍历所有基础元素并应用对应的属性修饰符
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            String elementTypeName = entry.getKey();
            List<WeaponData.BasicEntry> elements = entry.getValue();
            
            // 根据元素类型名称获取元素类型
            ElementType elementType = ElementType.byName(elementTypeName);
            if (elementType == null) {
                continue; // 未知元素类型，跳过
            }
            
            // 获取注册的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                continue; // 未注册的元素属性，跳过
            }
            
            // 为每个元素实例创建修饰符
            for (int i = 0; i < elements.size(); i++) {
                WeaponData.BasicEntry basic = elements.get(i);
                // 修饰符系统会自己计算和保存值，我们只需要应用修饰符本身
                // 创建属性修饰符（使用默认值1.0，实际值由修饰符系统计算）
                AttributeModifier modifier = elementAttribute.createModifier(stack, 1.0);
                
                // 应用修饰符到物品的攻击伤害属性上
                // 使用元素类型和索引生成唯一的UUID，确保每个修饰符都是唯一的
                UUID modifierId = UUID.nameUUIDFromBytes((elementType.getName() + "_" + i).getBytes());
                stack.addAttributeModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(modifierId, modifier.getName(), modifier.getAmount(), modifier.getOperation()), stack.getEquipmentSlot());
            }
        }
    }
}