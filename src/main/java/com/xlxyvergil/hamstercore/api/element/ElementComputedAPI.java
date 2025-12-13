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
        // 直接操作NBT数据来移除指定的属性修饰符
        if (stack.hasTag() && stack.getTag().contains("AttributeModifiers", 9)) {
            net.minecraft.nbt.ListTag attributeList = stack.getTag().getList("AttributeModifiers", 10);
            net.minecraft.nbt.ListTag newList = new net.minecraft.nbt.ListTag();
            
            // 遍历现有的属性修饰符
            for (int i = 0; i < attributeList.size(); i++) {
                net.minecraft.nbt.CompoundTag tag = attributeList.getCompound(i);
                
                // 检查槽位和UUID
                String slotName = tag.contains("Slot", 8) ? tag.getString("Slot") : "";
                boolean slotMatch = slot.getName().equals(slotName);
                
                // 获取修饰符UUID
                java.util.UUID modifierUUID = null;
                if (tag.contains("UUID", 11)) {
                    modifierUUID = tag.getUUID("UUID");
                } else if (tag.contains("Id", 8)) {
                    try {
                        modifierUUID = java.util.UUID.fromString(tag.getString("Id"));
                    } catch (IllegalArgumentException ignored) {}
                }
                
                // 如果槽位匹配且UUID匹配，则跳过（即移除）
                if (slotMatch && modifierUUID != null && modifierUUID.equals(modifierId)) {
                    continue;
                }
                
                // 否则保留该修饰符
                newList.add(tag);
            }
            
            // 更新NBT数据
            stack.getTag().put("AttributeModifiers", newList);
        }
    }
}