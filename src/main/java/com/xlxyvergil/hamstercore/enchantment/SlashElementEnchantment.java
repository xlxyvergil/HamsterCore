package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementBasedAttribute;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class SlashElementEnchantment extends ElementEnchantment {
    public SlashElementEnchantment() {
        super(Rarity.COMMON, ElementType.SLASH, 5, "enchantment_slash");
    }
    
    @Override
    public java.util.Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> getEntityAttributes(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND) {
            // 获取元素属�?
            ElementBasedAttribute elementAttribute = ElementRegistry.Attributes.getAttributeValue(this.elementType);
            if (elementAttribute != null) {
                // 计算基于等级的数值：每级0.2
                double value = 0.2 * level;
                // 使用ElementUUIDManager生成UUID
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, this.elementType, level);
                net.minecraft.world.entity.ai.attributes.AttributeModifier modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    modifierId, 
                    "hamstercore:" + elementType.getName(), 
                    value, 
                    AttributeModifier.Operation.ADDITION
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
