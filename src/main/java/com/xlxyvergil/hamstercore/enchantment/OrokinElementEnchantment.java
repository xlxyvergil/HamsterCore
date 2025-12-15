package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class OrokinElementEnchantment extends ElementEnchantment {
    public OrokinElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.OROKIN, 5, "enchantment_orokin");
    }
    
    @Override
    public java.util.Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        // 获取元素属性注册对象
        var attributeRegistry = com.xlxyvergil.hamstercore.element.ElementRegistry.getAttribute(this.elementType);
        if (attributeRegistry != null && attributeRegistry.isPresent()) {
            // 计算基于等级的数值：每级0.3
            double value = 0.3 * level;
            // 使用ElementRegistry生成UUID
            java.util.UUID modifierId = com.xlxyvergil.hamstercore.element.ElementRegistry.getModifierUUID(this.elementType, level);
            AttributeModifier modifier = new AttributeModifier(
                modifierId, 
                "hamstercore:" + elementType.getName(), 
                value, 
                AttributeModifier.Operation.ADDITION
            );
            
            // 返回包含修饰符的集合
            java.util.Collection<AttributeModifier> modifiers = new java.util.ArrayList<>();
            modifiers.add(modifier);
            return modifiers;
        }
        
        return java.util.Collections.emptyList();
    }
}