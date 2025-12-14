package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.handler.ElementModifierEventHandler;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import java.util.UUID;

/**
 * 元素系统计算API
 * 提供元素属性计算和应用的相关接口
 */
public class ElementComputedAPI {
    
    /**
     * 为物品应用元素属性修饰符
     * 这个方法封装了ElementModifierEventHandler的功能，使外部可以更容易地应用元素修饰符
     * 
     * @param event ItemAttributeModifierEvent事件
     * @param elementType 元素类型
     * @param value 元素值
     * @param index 元素索引（用于生成唯一UUID）
     */
    public static void applyElementModifier(ItemAttributeModifierEvent event, ElementType elementType, double value, int index) {
        // 使用ElementUUIDManager生成唯一的UUID
        UUID modifierId = ElementUUIDManager.getOrCreateUUID(event.getItemStack(), elementType, index);
        
        // 获取元素属性
        ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
        if (elementAttribute == null) {
            return; // 如果没有找到元素属性，则不应用修饰符
        }
        
        // 创建属性修饰符
        AttributeModifier modifier = new AttributeModifier(
            modifierId, 
            "hamstercore:" + elementType.getName(), 
            value, 
            AttributeModifier.Operation.ADDITION
        );
        
        // 应用修饰符到物品的攻击伤害属性上
        event.addModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, modifier);
    }
    
    /**
     * 从物品中移除指定的元素修饰符
     * 
     * @param stack 物品堆
     * @param elementType 元素类型
     * @param index 元素索引
     */
    public static void removeElementModifier(ItemStack stack, ElementType elementType, int index) {
        // 从物品中移除指定的UUID
        ElementUUIDManager.removeUUID(stack, elementType, index);
    }
    
    /**
     * 获取物品中指定元素类型和索引的UUID
     * 
     * @param stack 物品堆
     * @param elementType 元素类型
     * @param index 元素索引
     * @return 对应的UUID，如果找不到则返回null
     */
    public static UUID getElementUUID(ItemStack stack, ElementType elementType, int index) {
        return ElementUUIDManager.getUUID(stack, elementType, index);
    }
    
    /**
     * 添加通用属性修饰符到物品
     * 
     * @param stack 物品堆
     * @param attributeName 属性名称
     * @param modifier 属性修饰符
     * @param slot 装备槽位
     */
    public static void addAttributeModifier(ItemStack stack, String attributeName, AttributeModifier modifier, EquipmentSlot slot) {
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(attributeName));
        if (attribute != null) {
            stack.addAttributeModifier(attribute, modifier, slot);
        }
    }
}