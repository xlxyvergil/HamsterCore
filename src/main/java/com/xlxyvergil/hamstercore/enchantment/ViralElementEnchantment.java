package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.xlxyvergil.hamstercore.element.ElementRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
public class ViralElementEnchantment extends ElementEnchantment {
    public ViralElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.VIRAL, 5, "enchantment_viral");
    }
    
    @Override
    public Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        // 获取元素属性注册对象
        var attributeRegistry = ElementRegistry.getAttribute(this.elementType);
        if (attributeRegistry != null && attributeRegistry.isPresent()) {
            // 计算基于等级的数值：每级0.3
            double value = 0.3 * level;
            // 使用ElementRegistry生成UUID
            UUID modifierId = ElementRegistry.getModifierUUID(this.elementType, level);
            AttributeModifier modifier = new AttributeModifier(
                modifierId, 
                "hamstercore:" + elementType.getName(), 
                value, 
                AttributeModifier.Operation.ADDITION
            );
            
            // 返回包含修饰符的集合
            Collection<AttributeModifier> modifiers = new ArrayList<>();
            modifiers.add(modifier);
            return modifiers;
        }
        
        return Collections.emptyList();
    }
}