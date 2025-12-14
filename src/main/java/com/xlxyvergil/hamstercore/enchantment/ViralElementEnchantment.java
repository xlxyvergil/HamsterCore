package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;

public class ViralElementEnchantment extends ElementEnchantment {
    public ViralElementEnchantment() {
        super(Rarity.RARE, ElementType.VIRAL, 3, "enchantment_viral");
    }
    
    @Override
    public java.util.Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> getEntityAttributes(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND) {
            // 获取元素属性
            com.xlxyvergil.hamstercore.element.ElementAttribute elementAttribute = com.xlxyvergil.hamstercore.element.ElementRegistry.getAttribute(this.elementType);
            if (elementAttribute != null) {
                // 计算基于等级的数值：每级0.4（复合元素更强）
                double value = 0.4 * level;
                // 使用ElementUUIDManager生成UUID
                java.util.UUID modifierId = com.xlxyvergil.hamstercore.util.ElementUUIDManager.getOrCreateUUID(stack, this.elementType, level);
                net.minecraft.world.entity.ai.attributes.AttributeModifier modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    modifierId, 
                    "hamstercore:" + elementType.getName(), 
                    value, 
                    elementAttribute.getOperation()
                );
                
                // 返回包含修饰符的集合
                java.util.Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> modifiers = new java.util.ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return java.util.Collections.emptyList();
    }
}