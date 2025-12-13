package com.xlxyvergil.hamstercore.api.element;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * 元素系统属性修饰符API
 * 提供添加和删除物品属性修饰符的接口
 */
public class ElementComputedAPI {
    
    /**
     * 向物品添加属性修饰符
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
    
    /**
     * 从物品移除指定UUID的属性修饰符
     * 
     * @param stack 物品堆
     * @param modifierId 属性修饰符UUID
     * @param slot 装备槽位
     */
    public static void removeAttributeModifier(ItemStack stack, java.util.UUID modifierId, EquipmentSlot slot) {
        stack.removeAttributeModifier(slot, modifierId);
    }
}