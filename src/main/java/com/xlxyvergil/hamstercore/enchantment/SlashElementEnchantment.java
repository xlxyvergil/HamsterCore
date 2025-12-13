package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.UUID;

public class SlashElementEnchantment extends ElementEnchantment {
    private static final UUID SLASH_MODIFIER_UUID = UUID.fromString("656c6500-0000-0000-0000-000000000000");
    
    public SlashElementEnchantment() {
        super(Rarity.COMMON, ElementType.SLASH, 5, "enchantment_slash", SLASH_MODIFIER_UUID);
    }
    
    @Override
    public java.util.Collection<AttributeModifier> getEntityAttributes(EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND) {
            // 获取元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(this.elementType);
            if (elementAttribute != null) {
                // 计算基于等级的数值：每级0.3
                double value = 0.3 * level;
                AttributeModifier modifier = new AttributeModifier(
                    this.elementModifierId, 
                    "hamstercore:" + elementType.getName(), 
                    value, 
                    elementAttribute.getOperation()
                );
                
                // 返回包含修饰符的集合
                java.util.Collection<AttributeModifier> modifiers = new java.util.ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return super.getEntityAttributes(slot, level);
    }
}